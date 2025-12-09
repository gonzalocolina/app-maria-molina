package com.example.mariamolina.data.model

import com.google.firebase.Timestamp

/**
 * Representa un jugador en una partida multijugador.
 */
data class Jugador(
    val uid: String = "",         // ID de Firebase (secreto)
    val nickname: String = "",    // Nombre visible (ej. "Roberto")
    val puntuacion: Int = 0,
    val hasAnswered: Boolean = false  // Si ha respondido la pregunta actual
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this("", "", 0, false)
}

/**
 * Estados posibles de una partida multijugador.
 */
enum class EstadoPartida {
    ESPERANDO,   // En el lobby, esperando jugadores
    JUGANDO,     // Partida en curso
    RESULTADOS,  // Mostrando ranking final
    FINALIZADO   // Partida terminada
}

/**
 * Fases del juego dentro de una pregunta.
 * Controla qué se muestra a los alumnos en cada momento.
 */
enum class GamePhase {
    LOBBY,              // Esperando en la sala
    SHOWING_QUESTION,   // Mostrando pregunta, alumnos pueden responder
    WAITING_FOR_NEXT,   // Todos respondieron, esperando al profesor
    SHOWING_RESULTS,    // Mostrando ranking parcial
    FINISHED            // Partida finalizada
}

/**
 * Representa una partida/sala de juego multijugador.
 */
data class Partida(
    val pin: String = "",
    val estado: EstadoPartida = EstadoPartida.ESPERANDO,
    val fase: GamePhase = GamePhase.LOBBY,
    val preguntaActualIndex: Int = 0,
    val totalPreguntas: Int = 10,
    val dificultad: String = Dificultad.FACIL.name,
    val preguntasIds: List<String> = emptyList(),  // IDs de las preguntas seleccionadas
    val tiempoPorPregunta: Int = 20,  // Tiempo en segundos para responder cada pregunta
    val preguntaStartedAt: Timestamp? = null,  // Timestamp de cuando inició la pregunta actual
    val createdAt: Timestamp? = null,
    val hostUid: String = ""  // UID del profesor que creó la partida
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this("", EstadoPartida.ESPERANDO, GamePhase.LOBBY, 0, 10, Dificultad.FACIL.name, emptyList(), 20, null, null, "")
}

/**
 * Representa la respuesta de un jugador a una pregunta específica.
 */
data class RespuestaJugador(
    val preguntaIndex: Int = 0,
    val opcionSeleccionada: Int = -1,  // Índice de la opción elegida
    val esCorrecta: Boolean = false,
    val tiempoRespuestaMs: Long = 0,   // Tiempo en ms que tardó en responder
    val puntosObtenidos: Int = 0,
    val timestamp: Timestamp? = null,
    val puntosContabilizados: Boolean = false  // Si los puntos ya se sumaron al total
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(0, -1, false, 0, 0, null, false)
}

/**
 * Historial de una partida finalizada (para persistencia).
 */
data class HistorialPartida(
    val id: String = "",
    val pin: String = "",
    val dificultad: String = "",
    val totalPreguntas: Int = 0,
    val fechaPartida: Timestamp? = null,
    val duracionSegundos: Long = 0,
    val jugadores: List<JugadorHistorial> = emptyList()
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this("", "", "", 0, null, 0, emptyList())
}

/**
 * Datos resumidos de un jugador para el historial.
 */
data class JugadorHistorial(
    val nickname: String = "",
    val puntuacionFinal: Int = 0,
    val posicionFinal: Int = 0,
    val respuestasCorrectas: Int = 0
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this("", 0, 0, 0)
}

/**
 * Estado completo del juego multijugador para la UI.
 */
data class MultiplayerGameState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val partida: Partida? = null,
    val jugadores: List<Jugador> = emptyList(),
    val preguntaActual: QuizQuestion? = null,
    val respuestaEnviada: Boolean = false,
    val tiempoRestanteMs: Long = 20000L,
    val ranking: List<Jugador> = emptyList(),
    val miPuntuacion: Int = 0,
    val totalJugadores: Int = 0,
    val jugadoresQueRespondieron: Int = 0
)