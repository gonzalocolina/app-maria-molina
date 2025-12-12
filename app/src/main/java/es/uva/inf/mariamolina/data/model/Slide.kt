package es.uva.inf.mariamolina.data.model

/**
 * Modelo de datos para una diapositiva (slide) del módulo Kids/Slides.
 *
 * Ahora usamos resource IDs para título/descripcion para poder extraer textos a strings.xml
 */
data class Slide(
    val id: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val imageUrl: String,
    val hasSpeechBubble: Boolean = false
)
