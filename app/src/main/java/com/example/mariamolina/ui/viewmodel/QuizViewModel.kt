package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.model.*
import com.example.mariamolina.data.repository.DataState
import com.example.mariamolina.data.repository.QuizRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizUiState(
    val isLoading: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val errorMessage: String? = null,
    val puntuacion: Int = 0,
    val indicePreguntaActual: Int = 0,
    val estadoPartida: EstadoPartida = EstadoPartida.JUGANDO,
    val esMultiplayer: Boolean = false
)

class QuizViewModel(
    private val repository: QuizRepository = QuizRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private var partidaListener: ListenerRegistration? = null

    // Carga Solitaria
    fun loadQuestions(dificultad: Dificultad) {
        _uiState.value = _uiState.value.copy(isLoading = true, esMultiplayer = false)
        viewModelScope.launch {
            when (val result = repository.getQuestionsByDifficulty(dificultad)) {
                is DataState.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        questions = result.data.shuffled(), // Mezclamos solo en solitario
                        puntuacion = 0,
                        indicePreguntaActual = 0
                    )
                }
                is DataState.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                else -> {}
            }
        }
    }

    // Conexión Multiplayer
    fun conectarAPartida(pin: String, dificultad: Dificultad) {
        viewModelScope.launch {
            when (val result = repository.getQuestionsByDifficulty(dificultad)) {
                is DataState.Success -> {
                    // NO mezclamos en multiplayer para mantener el orden sincronizado con el profe
                    _uiState.value = _uiState.value.copy(
                        questions = result.data,
                        esMultiplayer = true,
                        puntuacion = 0
                    )
                    iniciarListener(pin)
                }
                is DataState.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
                else -> {}
            }
        }
    }

    private fun iniciarListener(pin: String) {
        partidaListener?.remove()
        partidaListener = db.collection("partidas").document(pin)
            .addSnapshotListener { snapshot, _ ->
                val partida = snapshot?.toObject(Partida::class.java)
                if (partida != null) {
                    // Actualizamos el estado local con lo que dice la nube
                    _uiState.value = _uiState.value.copy(
                        indicePreguntaActual = partida.indicePreguntaActual,
                        estadoPartida = partida.estado
                    )
                }
            }
    }

    // --- PROCESAR RESPUESTA ---
    fun procesarRespuesta(puntosGanados: Int, pinPartida: String?) {
        val nuevaPuntuacion = _uiState.value.puntuacion + puntosGanados

        if (pinPartida == null) {
            // Solitario: Avanzamos localmente
            _uiState.value = _uiState.value.copy(
                puntuacion = nuevaPuntuacion,
                indicePreguntaActual = _uiState.value.indicePreguntaActual + 1
            )
        } else {
            // Multiplayer: Actualizamos puntos y ENVIAMOS a la nube
            _uiState.value = _uiState.value.copy(puntuacion = nuevaPuntuacion)
            enviarRespuestaNube(pinPartida, nuevaPuntuacion)
        }
    }

    private fun enviarRespuestaNube(pin: String, puntos: Int) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            println("ERROR: Usuario no identificado al enviar respuesta")
            return
        }

        val updateData = mapOf(
            "puntuacion" to puntos,
            "haRespondido" to true // ¡ESTO ES LO QUE VE EL PROFESOR!
        )

        db.collection("partidas").document(pin)
            .collection("jugadores").document(uid)
            .update(updateData)
            .addOnFailureListener { e ->
                println("ERROR FATAL enviando respuesta a Firebase: ${e.message}")
            }
    }

    fun resetQuiz() {
        _uiState.value = QuizUiState()
        partidaListener?.remove()
    }

    fun guardarPuntuacion(pinPartida: String) {
        enviarRespuestaNube(pinPartida, _uiState.value.puntuacion)
    }

    override fun onCleared() {
        super.onCleared()
        partidaListener?.remove()
    }
}