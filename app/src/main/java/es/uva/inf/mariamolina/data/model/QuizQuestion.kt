package es.uva.inf.mariamolina.data.model

// Define los niveles de dificultad
enum class Dificultad {
    FACIL, MEDIA, DIFICIL
}

// Idiomas soportados
object IdiomasSoportados {
    const val ESPANOL = "es"
    const val INGLES = "en"
    const val FRANCES = "fr"
    const val ALEMAN = "de"
    
    val todos = listOf(ESPANOL, INGLES, FRANCES, ALEMAN)
}

/**
 * Define una sola opción de respuesta con soporte multilingüe.
 * Mantiene retrocompatibilidad con el formato antiguo (texto como String).
 */
data class OpcionRespuesta(
    val texto: String = "",  // Retrocompatibilidad: texto en español o único idioma
    val textoMultilingue: Map<String, String> = emptyMap(), // Nuevo: {"es": "...", "en": "...", "fr": "...", "de": "..."}
    val esCorrecta: Boolean = false
) {
    // Constructor sin argumentos requerido por Firestore para deserialización
    @Suppress("unused")
    constructor() : this("", emptyMap(), false)
    
    /**
     * Obtiene el texto en el idioma especificado.
     * Si no existe traducción, usa el texto base (retrocompatibilidad).
     */
    fun getTexto(idioma: String): String {
        return textoMultilingue[idioma] 
            ?: textoMultilingue[IdiomasSoportados.ESPANOL] 
            ?: texto
    }
}

/**
 * Define la pregunta completa con soporte multilingüe.
 * Mantiene retrocompatibilidad con el formato antiguo.
 */
data class QuizQuestion(
    val id: String = "",
    val pregunta: String = "",  // Retrocompatibilidad: pregunta en español o único idioma
    val preguntaMultilingue: Map<String, String> = emptyMap(), // Nuevo: {"es": "...", "en": "...", "fr": "...", "de": "..."}
    val dificultad: String = Dificultad.FACIL.name,
    val opciones: List<OpcionRespuesta> = emptyList()
) {
    // Constructor sin argumentos requerido por Firestore
    @Suppress("unused")
    constructor() : this("", "", emptyMap(), Dificultad.FACIL.name, emptyList())

    /**
     * Obtiene la pregunta en el idioma especificado.
     * Si no existe traducción, usa el texto base (retrocompatibilidad).
     */
    fun getPregunta(idioma: String): String {
        return preguntaMultilingue[idioma] 
            ?: preguntaMultilingue[IdiomasSoportados.ESPANOL] 
            ?: pregunta
    }

    // Función de utilidad para convertir la dificultad de String a Enum
    fun getDificultadEnum(): Dificultad {
        return Dificultad.valueOf(dificultad.uppercase())
    }
}
