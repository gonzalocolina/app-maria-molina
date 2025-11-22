package com.example.mariamolina.data.model

// Representa al usuario que se une
data class Jugador(
    val uid: String = "",
    val nickname: String = "",
    val puntuacion: Int = 0,
    // Nuevo: para saber si ha respondido a la pregunta actual
    val haRespondido: Boolean = false
)

// Representa la sala de juego
data class Partida(
    val pin: String = "",
    val estado: EstadoPartida = EstadoPartida.ESPERANDO,

    // --- CAMPOS PARA SINCRONIZACIÓN ---
    val indicePreguntaActual: Int = 0, // ¿Qué pregunta toca?
    val timestampInicioPregunta: Long = 0, // ¿Cuándo empezó el tiempo? (Milisegundos)
    val tiempoLimite: Long = 20000, // Tiempo para esta pregunta (20s)
    val dificultad: String = "FACIL" // Guardamos la dificultad para saber qué preguntas cargar al reconectar
)

enum class EstadoPartida {
    ESPERANDO,  // Lobby
    JUGANDO,    // Mostrando pregunta (Timer corriendo)
    RESULTADOS, // Mostrando respuesta correcta (Pausa entre preguntas)
    FINALIZADO  // Fin del juego
}