package com.example.mariamolina.ui.screens.panorama

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renderer OpenGL ES 2.0 para visualizar imágenes equirectangulares (360°) 
 * proyectadas en el interior de una esfera.
 */
class SphericalRenderer(
    private val context: Context,
    private val imageResId: Int
) : GLSurfaceView.Renderer {

    // Matrices de transformación
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    // Buffers para la geometría de la esfera
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer

    // Programa de shaders y handles
    private var programHandle: Int = 0
    private var textureHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var positionHandle: Int = 0
    private var texCoordHandle: Int = 0

    // Parámetros de la cámara (controlados por gestos)
    var rotationX: Float = 0f  // Pitch (arriba/abajo)
    var rotationY: Float = 0f  // Yaw (izquierda/derecha)
    var zoom: Float = 1f       // Nivel de zoom (campo de visión)

    // Constantes de la esfera
    private val SPHERE_RADIUS = 10f
    private val SPHERE_SLICES = 60  // Divisiones horizontales
    private val SPHERE_STACKS = 60  // Divisiones verticales

    private var indexCount = 0

    // Shaders GLSL
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        uniform sampler2D uTexture;
        varying vec2 vTexCoord;
        
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Color de fondo negro
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_FRONT) // Renderizar interior de la esfera

        // Crear geometría de la esfera
        createSphere()

        // Compilar y enlazar shaders
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programHandle = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        // Obtener handles de los atributos y uniformes
        mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")
        positionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(programHandle, "aTexCoord")

        // Cargar textura
        loadTexture()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        // Matriz de proyección perspectiva
        Matrix.perspectiveM(projectionMatrix, 0, 60f / zoom, ratio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(programHandle)

        // Actualizar matriz de proyección según el zoom
        val ratio = 1f // Se recalcula en onSurfaceChanged pero usamos el valor guardado
        Matrix.perspectiveM(projectionMatrix, 0, 60f / zoom, ratio, 0.1f, 100f)

        // Matriz de vista: cámara en el centro de la esfera
        Matrix.setIdentityM(viewMatrix, 0)
        
        // Aplicar rotaciones de la cámara
        Matrix.rotateM(viewMatrix, 0, rotationX, 1f, 0f, 0f) // Pitch
        Matrix.rotateM(viewMatrix, 0, rotationY, 0f, 1f, 0f) // Yaw

        // Matriz del modelo: esfera centrada en el origen
        Matrix.setIdentityM(modelMatrix, 0)

        // Calcular MVP = Projection * View * Model
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

        // Pasar la matriz MVP al shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Configurar buffer de vértices
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer
        )

        // Configurar buffer de coordenadas de textura
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(
            texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer
        )

        // Vincular textura
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        // Dibujar la esfera
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )

        // Deshabilitar atributos
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    /**
     * Crea la geometría de una esfera UV con coordenadas de textura para
     * mapear correctamente una imagen equirectangular.
     */
    private fun createSphere() {
        val vertices = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        // Generar vértices y coordenadas de textura
        for (stack in 0..SPHERE_STACKS) {
            val stackAngle = Math.PI / 2 - stack * Math.PI / SPHERE_STACKS
            val xy = SPHERE_RADIUS * cos(stackAngle).toFloat()
            val z = SPHERE_RADIUS * sin(stackAngle).toFloat()

            for (slice in 0..SPHERE_SLICES) {
                val sliceAngle = slice * 2 * Math.PI / SPHERE_SLICES

                val x = xy * cos(sliceAngle).toFloat()
                val y = xy * sin(sliceAngle).toFloat()

                vertices.add(x)
                vertices.add(z)  // Z es arriba en nuestra convención
                vertices.add(y)

                // Coordenadas de textura (U, V)
                val u = slice.toFloat() / SPHERE_SLICES
                val v = stack.toFloat() / SPHERE_STACKS
                texCoords.add(u)
                texCoords.add(v)
            }
        }

        // Generar índices para los triángulos
        for (stack in 0 until SPHERE_STACKS) {
            for (slice in 0 until SPHERE_SLICES) {
                val first = (stack * (SPHERE_SLICES + 1) + slice).toShort()
                val second = (first + SPHERE_SLICES + 1).toShort()

                // Primer triángulo
                indices.add(first)
                indices.add(second)
                indices.add((first + 1).toShort())

                // Segundo triángulo
                indices.add((first + 1).toShort())
                indices.add(second)
                indices.add((second + 1).toShort())
            }
        }

        indexCount = indices.size

        // Crear buffers nativos
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices.toFloatArray())
                position(0)
            }

        textureBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCoords.toFloatArray())
                position(0)
            }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indices.toShortArray())
                position(0)
            }
    }

    /**
     * Carga la imagen equirectangular como textura OpenGL.
     */
    private fun loadTexture() {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        textureHandle = textureIds[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        // Configurar parámetros de filtrado y wrapping
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // Cargar bitmap desde recursos
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }
        val bitmap = BitmapFactory.decodeResource(context.resources, imageResId, options)

        // Subir bitmap a la GPU
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()
    }

    /**
     * Compila un shader GLSL.
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
