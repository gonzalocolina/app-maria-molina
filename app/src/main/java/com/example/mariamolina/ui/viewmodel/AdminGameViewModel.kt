package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.EstadoPartida
import com.example.mariamolina.data.model.Jugador
import com.example.mariamolina.data.model.Partida
import com.example.mariamolina.data.model.QuizQuestion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

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

    // --- CREACIÓN DE PARTIDA ---
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

                // Cargamos preguntas SIN ordenar en la query para evitar errores de índice compuesto
                // pero las ordenamos EN MEMORIA para garantizar sincronización
                val preguntasSnapshot = db.collection("quizzes")
                    .whereEqualTo("dificultad", dificultad.name)
                    .get().await()

                // ¡AQUÍ ESTÁ LA CLAVE! .sortedBy { it.id }
                val listaPreguntas = preguntasSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(QuizQuestion::class.java)?.copy(id = doc.id)
                }.sortedBy { it.id }

                if (listaPreguntas.isEmpty()) throw Exception("No hay preguntas disponibles")

                db.collection("partidas").document(nuevoPin).set(nuevaPartida).await()

                _uiState.value = AdminUiState(
                    isLoading = false,
                    pinGenerado = nuevoPin,
                    preguntas = listaPreguntas
                )

                escucharPartidaEnTiempoReal(nuevoPin)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    // --- RECONEXIÓN (Si el profesor sale y vuelve) ---
    fun conectarAPartidaExistente(pin: String) {
        // Si ya estamos conectados y tenemos preguntas, no recargamos
        if (_uiState.value.pinGenerado == pin && _uiState.value.preguntas.isNotEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true, pinGenerado = pin)

        viewModelScope.launch {
            try {
                val partidaDoc = db.collection("partidas").document(pin).get().await()
                val partida = partidaDoc.toObject(Partida::class.java) ?: throw Exception("Partida no encontrada")

                // Volvemos a cargar las preguntas con el MISMO ORDEN
                val preguntasSnapshot = db.collection("quizzes")
                    .whereEqualTo("dificultad", partida.dificultad)
                    .get().await()

                val listaPreguntas = preguntasSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(QuizQuestion::class.java)?.copy(id = doc.id)
                }.sortedBy { it.id } // ¡AQUÍ TAMBIÉN!

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    preguntas = listaPreguntas,
                    partida = partida
                )

                escucharPartidaEnTiempoReal(pin)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error reconexión: ${e.message}")
            }
        }
    }

    private fun escucharPartidaEnTiempoReal(pin: String) {
        listeners.forEach { it.remove() }
        listeners.clear()

        // Listener 1: Estado de la Partida
        listeners.add(db.collection("partidas").document(pin)
            .addSnapshotListener { snapshot, _ ->
                val partida = snapshot?.toObject(Partida::class.java)
                if (partida != null) {
                    _uiState.value = _uiState.value.copy(partida = partida)
                    // Comprobamos si hay que avanzar (si todos respondieron)
                    verificarAvanceAutomatico(partida)
                }
            }
        )

        // Listener 2: Lista de Jugadores
        listeners.add(db.collection("partidas").document(pin).collection("jugadores")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val jugadores = snapshot.documents.mapNotNull { it.toObject(Jugador::class.java) }
                    _uiState.value = _uiState.value.copy(jugadoresUnidos = jugadores)
                    // Comprobamos si hay que avanzar
                    verificarAvanceAutomatico(_uiState.value.partida)
                }
            }
        )
    }

    // --- LÓGICA DE JUEGO ---

    fun empezarJuego() {
        val pin = _uiState.value.pinGenerado ?: return
        lanzarPregunta(pin, 0)
    }

    private fun lanzarPregunta(pin: String, indice: Int) {
        val totalPreguntas = _uiState.value.preguntas.size

        // Si ya no hay más preguntas, terminamos
        if (indice >= totalPreguntas && totalPreguntas > 0) {
            db.collection("partidas").document(pin).update("estado", EstadoPartida.FINALIZADO)
            return
        }

        viewModelScope.launch {
            // 1. Resetear "haRespondido" de todos los alumnos
            val jugadoresRefs = db.collection("partidas").document(pin).collection("jugadores").get().await()
            val batch = db.batch()
            jugadoresRefs.documents.forEach { doc ->
                batch.update(doc.reference, "haRespondido", false)
            }
            batch.commit().await()

            // 2. Actualizar estado a JUGANDO con la nueva pregunta
            db.collection("partidas").document(pin).update(
                mapOf(
                    "estado" to EstadoPartida.JUGANDO,
                    "indicePreguntaActual" to indice,
                    "timestampInicioPregunta" to System.currentTimeMillis(),
                    "tiempoLimite" to 20000
                )
            )

            // 3. Iniciar temporizador de seguridad
            gestionarTemporizador(pin, indice)
        }
    }

    // Temporizador de seguridad: Si pasan 22s y nadie avanza, forzamos el fin de la pregunta
    private fun gestionarTemporizador(pin: String, indicePregunta: Int) {
        isGameLoopActive = true
        viewModelScope.launch {
            delay(22000)
            val estadoActual = _uiState.value.partida
            // Si seguimos en la misma pregunta y jugando, cortamos
            if (isGameLoopActive && estadoActual?.indicePreguntaActual == indicePregunta && estadoActual.estado == EstadoPartida.JUGANDO) {
                finalizarPregunta(pin)
            }
        }
    }

    // Comprueba si todos han respondido para avanzar rápido
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

    private fun finalizarPregunta(pin: String) {
        viewModelScope.launch {
            // 1. Mostrar RESULTADOS
            db.collection("partidas").document(pin).update("estado", EstadoPartida.RESULTADOS)

            // 2. Esperar 5 segundos
            delay(5000)

            // 3. Siguiente pregunta
            val siguienteIndice = (_uiState.value.partida?.indicePreguntaActual ?: 0) + 1
            lanzarPregunta(pin, siguienteIndice)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
        isGameLoopActive = false
    }
}