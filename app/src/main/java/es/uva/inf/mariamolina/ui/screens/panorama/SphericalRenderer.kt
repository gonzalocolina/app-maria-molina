package es.uva.inf.mariamolina.ui.screens.panorama

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
    private val imageAssetName: String = "foto_monasterio360.jpg"
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
    private var textureUniformHandle: Int = 0

    // Parámetros de la cámara (controlados por gestos)
    var rotationX: Float = 20f  // Pitch (arriba/abajo)
    var rotationY: Float = 77f  // Yaw (izquierda/derecha) - Iniciar mirando hacia el lado opuesto
    var zoom: Float = 0.7f       // Nivel de zoom (campo de visión)
    
    // Ratio de aspecto guardado
    private var aspectRatio: Float = 1f

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
        android.util.Log.d("SphericalRenderer", "=== onSurfaceCreated ===")
        
        // Color de fondo rojo para debug (si vemos rojo, el GL funciona pero no se dibuja la esfera)
        GLES20.glClearColor(0.2f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        
        // DESACTIVAR culling temporalmente para debug
        GLES20.glDisable(GLES20.GL_CULL_FACE)

        // Crear geometría de la esfera
        createSphere()
        android.util.Log.d("SphericalRenderer", "Esfera creada: $indexCount índices")

        // Compilar y enlazar shaders
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        android.util.Log.d("SphericalRenderer", "Vertex shader: $vertexShader, Fragment shader: $fragmentShader")

        programHandle = GLES20.glCreateProgram()
        android.util.Log.d("SphericalRenderer", "Program handle: $programHandle")
        
        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vertexShader)
            GLES20.glAttachShader(programHandle, fragmentShader)
            GLES20.glLinkProgram(programHandle)
            
            // Verificar estado del link
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                val errorMsg = GLES20.glGetProgramInfoLog(programHandle)
                android.util.Log.e("SphericalRenderer", "Error linking program: $errorMsg")
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            } else {
                android.util.Log.d("SphericalRenderer", "Program linked successfully")
            }
        }

        // Obtener handles de los atributos y uniformes
        mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")
        positionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(programHandle, "aTexCoord")
        textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "uTexture")
        
        android.util.Log.d("SphericalRenderer", "Handles - MVP: $mvpMatrixHandle, Position: $positionHandle, TexCoord: $texCoordHandle, Texture: $textureUniformHandle")

        // Cargar textura
        loadTexture()
        
        android.util.Log.d("SphericalRenderer", "=== onSurfaceCreated completado ===")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        android.util.Log.d("SphericalRenderer", "onSurfaceChanged: ${width}x${height}")
        GLES20.glViewport(0, 0, width, height)

        aspectRatio = width.toFloat() / height.toFloat()

        // Matriz de proyección perspectiva
        Matrix.perspectiveM(projectionMatrix, 0, 60f / zoom, aspectRatio, 0.1f, 100f)
    }

    private var frameCount = 0
    
    override fun onDrawFrame(gl: GL10?) {
        frameCount++
        
        // Log solo cada 60 frames para no saturar
        val shouldLog = frameCount == 1 || frameCount % 300 == 0
        
        if (shouldLog) {
            android.util.Log.d("SphericalRenderer", "=== onDrawFrame #$frameCount ===")
        }
        
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (programHandle == 0) {
            if (shouldLog) android.util.Log.e("SphericalRenderer", "Program handle es 0!")
            return
        }

        GLES20.glUseProgram(programHandle)
        
        var error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR && shouldLog) {
            android.util.Log.e("SphericalRenderer", "Error después de glUseProgram: $error")
        }

        // Actualizar matriz de proyección según el zoom
        Matrix.perspectiveM(projectionMatrix, 0, 60f / zoom, aspectRatio, 0.1f, 100f)

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
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer
        )

        // Configurar buffer de coordenadas de textura
        textureBuffer.position(0)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(
            texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer
        )

        // Vincular textura
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        GLES20.glUniform1i(textureUniformHandle, 0)
        
        if (shouldLog) {
            android.util.Log.d("SphericalRenderer", "textureHandle: $textureHandle, indexCount: $indexCount")
        }

        // Dibujar la esfera
        indexBuffer.position(0)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )
        
        error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR && shouldLog) {
            android.util.Log.e("SphericalRenderer", "Error después de glDrawElements: $error")
        }

        // Deshabilitar atributos
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        
        if (shouldLog) {
            android.util.Log.d("SphericalRenderer", "Frame dibujado correctamente")
        }
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
        
        android.util.Log.d("SphericalRenderer", "Vértices: ${vertices.size / 3}, TexCoords: ${texCoords.size / 2}, Índices: $indexCount")
        
        // Log de algunos vértices para verificar
        if (vertices.size >= 9) {
            android.util.Log.d("SphericalRenderer", "Primeros 3 vértices: (${vertices[0]}, ${vertices[1]}, ${vertices[2]}), (${vertices[3]}, ${vertices[4]}, ${vertices[5]}), (${vertices[6]}, ${vertices[7]}, ${vertices[8]})")
        }

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
        
        android.util.Log.d("SphericalRenderer", "Buffers creados - vertex capacity: ${vertexBuffer.capacity()}, index capacity: ${indexBuffer.capacity()}")
    }

    /**
     * Carga la imagen equirectangular como textura OpenGL desde assets.
     */
    private fun loadTexture() {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        textureHandle = textureIds[0]

        if (textureHandle == 0) {
            android.util.Log.e("SphericalRenderer", "Error generando textura")
            return
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        // Configurar parámetros de filtrado y wrapping
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // Obtener el tamaño máximo de textura soportado
        val maxTextureSize = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0)
        android.util.Log.d("SphericalRenderer", "Max texture size: ${maxTextureSize[0]}")

        try {
            // Abrir el archivo desde assets
            val inputStream = context.assets.open(imageAssetName)
            
            // Primero, obtener las dimensiones de la imagen sin cargarla
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            // Necesitamos reabrir el stream para leer las dimensiones
            val boundsStream = context.assets.open(imageAssetName)
            BitmapFactory.decodeStream(boundsStream, null, boundsOptions)
            boundsStream.close()
            
            val imageWidth = boundsOptions.outWidth
            val imageHeight = boundsOptions.outHeight
            android.util.Log.d("SphericalRenderer", "Imagen original: ${imageWidth}x${imageHeight}")

            // Calcular el factor de escala si la imagen es muy grande
            var sampleSize = 1
            val maxSize = minOf(maxTextureSize[0], 4096) // Limitar a 4096 para seguridad
            
            while (imageWidth / sampleSize > maxSize || imageHeight / sampleSize > maxSize) {
                sampleSize *= 2
            }
            
            // Si la imagen es muy grande para memoria, aumentar el sampleSize
            if (imageWidth * imageHeight > 16000000) { // > 16 megapixels
                sampleSize = maxOf(sampleSize, 2)
            }
            
            android.util.Log.d("SphericalRenderer", "Sample size: $sampleSize")

            // Cargar bitmap con el sample size calculado
            val options = BitmapFactory.Options().apply {
                inScaled = false
                inSampleSize = sampleSize
                inPreferredConfig = android.graphics.Bitmap.Config.RGB_565 // Usa menos memoria
            }
            
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            if (bitmap == null) {
                android.util.Log.e("SphericalRenderer", "Error: No se pudo cargar el bitmap")
                return
            }
            
            android.util.Log.d("SphericalRenderer", "Bitmap cargado: ${bitmap.width}x${bitmap.height}")

            // Subir bitmap a la GPU
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            
            // Verificar errores de OpenGL
            val error = GLES20.glGetError()
            if (error != GLES20.GL_NO_ERROR) {
                android.util.Log.e("SphericalRenderer", "Error OpenGL al cargar textura: $error")
            } else {
                android.util.Log.d("SphericalRenderer", "Textura cargada exitosamente")
            }

            bitmap.recycle()
        } catch (e: Exception) {
            android.util.Log.e("SphericalRenderer", "Error cargando imagen: ${e.message}", e)
        }
    }

    /**
     * Compila un shader GLSL.
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        
        // Verificar errores de compilación
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val errorMsg = GLES20.glGetShaderInfoLog(shader)
            android.util.Log.e("SphericalRenderer", "Error compilando shader: $errorMsg")
            GLES20.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
}
