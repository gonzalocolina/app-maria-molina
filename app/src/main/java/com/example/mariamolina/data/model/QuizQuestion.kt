package com.example.mariamolina.data.model

// Define los niveles de dificultad
enum class Dificultad {
    FACIL, MEDIA, DIFICIL
}

data class OpcionRespuesta(
    // Mapa: "es" -> "Si", "en" -> "Yes"
    val texto: Map<String, String> = emptyMap(),
    val esCorrecta: Boolean = false
) {
    @Suppress("unused")
    constructor() : this(emptyMap(), false)

    // Función para obtener el texto según el idioma de la APP
    fun obtenerTexto(codigoIdiomaApp: String): String {
        // Intenta devolver el idioma seleccionado, si no existe, devuelve español (fallback)
        return texto[codigoIdiomaApp] ?: texto["es"] ?: ""
    }
}

data class QuizQuestion(
    val id: String = "",
    // Mapa: "es" -> "Pregunta...", "en" -> "Question..."
    val pregunta: Map<String, String> = emptyMap(),
    val dificultad: String = Dificultad.FACIL.name,
    val opciones: List<OpcionRespuesta> = emptyList()
) {
    @Suppress("unused")
    constructor() : this("", emptyMap(), Dificultad.FACIL.name, emptyList())

    fun obtenerPregunta(codigoIdiomaApp: String): String {
        return pregunta[codigoIdiomaApp] ?: pregunta["es"] ?: ""
    }

    fun getDificultadEnum(): Dificultad {
        return Dificultad.valueOf(dificultad.uppercase())
    }
}