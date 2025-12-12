package es.uva.inf.mariamolina.ui.screens.panorama

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.max
import kotlin.math.min

/**
 * GLSurfaceView personalizado que maneja gestos táctiles para controlar
 * la rotación y el zoom de la vista panorámica 360°.
 */
class Panorama360GLSurfaceView(
    context: Context,
    private val renderer: SphericalRenderer
) : GLSurfaceView(context) {

    // Detector de gestos de escala (pinch-to-zoom)
    private val scaleGestureDetector: ScaleGestureDetector

    // Variables para el seguimiento del arrastre
    private var previousX = 0f
    private var previousY = 0f

    // Sensibilidad de rotación
    private val rotationSensitivity = 0.10f

    // Límites de zoom
    private val minZoom = 0.5f
    private val maxZoom = 3f

    // Límites de rotación vertical (para evitar voltear la cámara)
    private val maxPitch = 85f
    private val minPitch = -85f

    init {
        // Configurar OpenGL ES 2.0
        setEGLContextClientVersion(2)
        
        // Configurar para preservar el contexto EGL al pausar
        preserveEGLContextOnPause = true
        
        // Configurar el renderer
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY

        // Inicializar detector de escala
        scaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    // Actualizar zoom basado en el gesto de pellizco
                    renderer.zoom *= detector.scaleFactor
                    renderer.zoom = max(minZoom, min(maxZoom, renderer.zoom))
                    return true
                }
            }
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Procesar gestos de escala
        scaleGestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                // Solo procesar arrastre si no estamos haciendo zoom
                if (!scaleGestureDetector.isInProgress && event.pointerCount == 1) {
                    val deltaX = event.x - previousX
                    val deltaY = event.y - previousY

                    // Actualizar rotaciones
                    // Invertir deltaX para que el movimiento sea intuitivo (arrastrar derecha = girar izquierda)
                    renderer.rotationY -= deltaX * rotationSensitivity
                    // Invertir deltaY para que arrastrar hacia arriba mueva la vista hacia arriba
                    renderer.rotationX -= deltaY * rotationSensitivity

                    // Limitar rotación vertical
                    renderer.rotationX = max(minPitch, min(maxPitch, renderer.rotationX))

                    // Normalizar rotación horizontal
                    if (renderer.rotationY > 360f) renderer.rotationY -= 360f
                    if (renderer.rotationY < 0f) renderer.rotationY += 360f
                }
                previousX = event.x
                previousY = event.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Limpiar estado
            }
        }

        return true
    }
}
