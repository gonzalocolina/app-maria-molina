package com.example.mariamolina.data.model

/**
 * Modelo de datos para una diapositiva (slide) del módulo Kids/Slides.
 *
 * Usamos Strings para título/descripcion para facilitar pruebas rápidas.
 */
data class Slide(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String
)

