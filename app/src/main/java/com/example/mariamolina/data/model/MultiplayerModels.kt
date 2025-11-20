package com.example.mariamolina.data.model

// Representa al usuario que se une
data class Jugador(
    val uid: String = "",       // ID interno de Firebase (secreto)
    val nickname: String = "",  // Nombre visible (ej. "Roberto")
    val puntuacion: Int = 0
)

// Representa la sala de juego (Kahoot session)
data class Partida(
    val pin: String = "",          // El código para unirse (ej. "1234")
    val estado: EstadoPartida = EstadoPartida.ESPERANDO, // En qué fase estamos
    val preguntaActualIndex: Int = 0,
    // En Firestore, los jugadores irán en una SUB-COLECCIÓN, no aquí dentro
)

enum class EstadoPartida {
    ESPERANDO,  // En el lobby
    JUGANDO,    // Respondiendo preguntas
    RESULTADOS, // Viendo ranking final
    FINALIZADO
}