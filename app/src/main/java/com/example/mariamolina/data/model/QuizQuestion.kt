package com.example.mariamolina.data.model

// Define los niveles de dificultad
enum class Dificultad {
    FACIL, MEDIA, DIFICIL
}

// Define una sola opción de respuesta
data class OpcionRespuesta(
    val texto: String, // ¡CAMBIO! De Int a String
    val esCorrecta: Boolean
) {
    // Constructor sin argumentos requerido por Firestore para deserialización
    @Suppress("unused")
    constructor() : this("", false)
}

// Define la pregunta completa
data class QuizQuestion(
    val id: String = "", // Firestore usará el ID del documento, pero lo mantenemos
    val pregunta: String = "",
    val dificultad: String = Dificultad.FACIL.name, // Guardamos la dificultad como String
    val opciones: List<OpcionRespuesta> = emptyList()
) {
    // Constructor sin argumentos requerido por Firestore
    @Suppress("unused")
    constructor() : this("", "", Dificultad.FACIL.name, emptyList())

    // Función de utilidad para convertir la dificultad de String a Enum
    fun getDificultadEnum(): Dificultad {
        return Dificultad.valueOf(dificultad.uppercase())
    }
}
