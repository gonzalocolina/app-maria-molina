package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

// (AdminUiState se mantiene igual)
data class AdminUiState(
    val isLoading: Boolean = false,
    val pinGenerado: String? = null,
    val jugadoresUnidos: List<Jugador> = emptyList(),
    val partida: Partida? = null,
    val preguntas: List<QuizQuestion> = emptyList(),
    val error: String? = null
)

class AdminGameViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    private var listeners: MutableList<ListenerRegistration> = mutableListOf()
    private var isGameLoopActive = false

    // --- 1. CREACIÓN (Con ordenación segura) ---
    fun crearPartida(dificultad: Dificultad) {
        _uiState.value = AdminUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val nuevoPin = Random.nextInt(1000, 9999).toString()
                val nuevaPartida = Partida(
                    pin = nuevoPin,
                    estado = EstadoPartida.ESPERANDO,
                    dificultad = dificultad.name
                )

                // Cargamos preguntas
                val preguntasSnapshot = db.collection("quizzes")
                    .whereEqualTo("dificultad", dificultad.name)
                    .get().await()

                // ¡IMPORTANTE! Ordenamos por ID para que todos tengan el mismo orden
                val listaPreguntas = preguntasSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(QuizQuestion::class.java)?.copy(id = doc.id)
                }.sortedBy { it.id }

                if (listaPreguntas.isEmpty()) throw Exception("No hay preguntas")

                db.collection("partidas").document(nuevoPin).set(nuevaPartida).await()

                _uiState.value = AdminUiState(isLoading = false, pinGenerado = nuevoPin, preguntas = listaPreguntas)
                escucharPartidaEnTiempoReal(nuevoPin)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    // --- 2. RECONEXIÓN ---
    fun conectarAPartidaExistente(pin: String) {
        if (_uiState.value.pinGenerado == pin && _uiState.value.preguntas.isNotEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true, pinGenerado = pin)
        viewModelScope.launch {
            try {
                val partidaDoc = db.collection("partidas").document(pin).get().await()
                val partida = partidaDoc.toObject(Partida::class.java) ?: throw Exception("Partida no encontrada")

                val preguntasSnapshot = db.collection("quizzes")
                    .whereEqualTo("dificultad", partida.dificultad)
                    .get().await()

                val listaPreguntas = preguntasSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(QuizQuestion::class.java)?.copy(id = doc.id)
                }.sortedBy { it.id }

                _uiState.value = _uiState.value.copy(isLoading = false, preguntas = listaPreguntas, partida = partida)
                escucharPartidaEnTiempoReal(pin)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun escucharPartidaEnTiempoReal(pin: String) {
        listeners.forEach { it.remove() }
        listeners.clear()

        // Listener Estado
        listeners.add(db.collection("partidas").document(pin).addSnapshotListener { snapshot, _ ->
            val partida = snapshot?.toObject(Partida::class.java)
            if (partida != null) {
                _uiState.value = _uiState.value.copy(partida = partida)
                // Chequeamos si hay que cerrar la pregunta
                verificarAvanceAutomatico(partida)
            }
        })
        // Listener Jugadores
        listeners.add(db.collection("partidas").document(pin).collection("jugadores").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val jugadores = snapshot.documents.mapNotNull { it.toObject(Jugador::class.java) }
                _uiState.value = _uiState.value.copy(jugadoresUnidos = jugadores)
                verificarAvanceAutomatico(_uiState.value.partida)
            }
        })
    }

    // --- 3. CONTROL DEL JUEGO ---

    fun empezarJuego() {
        val pin = _uiState.value.pinGenerado ?: return
        lanzarPregunta(pin, 0)
    }

    // Función MANUAL para que el profesor pase a la siguiente
    fun avanzarSiguientePregunta() {
        val pin = _uiState.value.pinGenerado ?: return
        val indiceActual = _uiState.value.partida?.indicePreguntaActual ?: 0
        lanzarPregunta(pin, indiceActual + 1)
    }

    // Función MANUAL para cortar el tiempo si el profesor quiere
    fun forzarFinPregunta() {
        val pin = _uiState.value.pinGenerado ?: return
        if (_uiState.value.partida?.estado == EstadoPartida.JUGANDO) {
            isGameLoopActive = false
            finalizarPregunta(pin)
        }
    }

    private fun lanzarPregunta(pin: String, indice: Int) {
        val totalPreguntas = _uiState.value.preguntas.size

        // Si ya no hay más, terminamos del todo
        if (indice >= totalPreguntas && totalPreguntas > 0) {
            db.collection("partidas").document(pin).update("estado", EstadoPartida.FINALIZADO)
            return
        }

        viewModelScope.launch {
            // 1. Resetear respuestas (Batch para eficiencia)
            val jugadoresRef = db.collection("partidas").document(pin).collection("jugadores")
            val snapshot = jugadoresRef.get().await()
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "haRespondido", false)
            }
            batch.commit().await()

            // 2. Cambiar a estado JUGANDO
            db.collection("partidas").document(pin).update(
                mapOf(
                    "estado" to EstadoPartida.JUGANDO,
                    "indicePreguntaActual" to indice,
                    "timestampInicioPregunta" to System.currentTimeMillis(),
                    "tiempoLimite" to 20000
                )
            )

            // 3. Iniciar cuenta atrás de seguridad
            gestionarTemporizador(pin, indice)
        }
    }

    private fun gestionarTemporizador(pin: String, indicePregunta: Int) {
        isGameLoopActive = true
        viewModelScope.launch {
            delay(22000) // 22s (20s de juego + margen)

            val estadoActual = _uiState.value.partida
            // Si seguimos atascados en JUGANDO, cerramos automáticamente
            if (isGameLoopActive && estadoActual?.indicePreguntaActual == indicePregunta && estadoActual.estado == EstadoPartida.JUGANDO) {
                finalizarPregunta(pin)
            }
        }
    }

    // Cierra la pregunta automáticamente si todos responden
    private fun verificarAvanceAutomatico(partida: Partida?) {
        if (partida == null || partida.estado != EstadoPartida.JUGANDO) return

        val jugadores = _uiState.value.jugadoresUnidos
        if (jugadores.isEmpty()) return

        val todosRespondieron = jugadores.all { it.haRespondido }

        if (todosRespondieron) {
            isGameLoopActive = false
            finalizarPregunta(partida.pin)
        }
    }

    // Cambia a estado RESULTADOS y ESPERA al profesor
    private fun finalizarPregunta(pin: String) {
        viewModelScope.launch {
            db.collection("partidas").document(pin).update("estado", EstadoPartida.RESULTADOS)
            // ¡AQUÍ ESTÁ EL CAMBIO! Ya no hay un delay() que lance la siguiente.
            // El sistema se queda en PAUSA hasta que el profe llame a 'avanzarSiguientePregunta()'
        }
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
    }
}