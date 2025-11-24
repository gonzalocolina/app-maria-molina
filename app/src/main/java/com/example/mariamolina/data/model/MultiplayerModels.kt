package com.example.mariamolina.data.model

// IMPORTANTE: Todos los campos tienen valores por defecto para que Firestore no falle
data class Jugador(
    val uid: String = "",
    val nickname: String = "",
    val puntuacion: Int = 0,
    val haRespondido: Boolean = false // Por defecto NO ha respondido
)

data class Partida(
    val pin: String = "",
    val estado: EstadoPartida = EstadoPartida.ESPERANDO,
    val indicePreguntaActual: Int = 0,
    val timestampInicioPregunta: Long = 0,
    val tiempoLimite: Long = 20000,
    val dificultad: String = "FACIL"
)

enum class EstadoPartida {
    ESPERANDO, JUGANDO, RESULTADOS, FINALIZADO
}