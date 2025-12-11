package com.example.mariamolina.data.model

/**
 * Modelo de datos para una diapositiva (slide) del módulo Kids/Slides.
 */
data class Slide(
    val id: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val imageUrl: String,
    val hasSpeechBubble: Boolean = false
)
