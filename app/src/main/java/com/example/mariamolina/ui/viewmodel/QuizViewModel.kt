package com.example.mariamolina.ui.viewmodel

import android.util.Log // ¡Importación para Logs!
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

    // Etiqueta para filtrar en Logcat
    private val TAG = "QuizVM"

    // --- MODO SOLITARIO ---
    fun loadQuestions(dificultad: Dificultad) {
        _uiState.value = _uiState.value.copy(isLoading = true, esMultiplayer = false)
        viewModelScope.launch {
            when (val result = repository.getQuestionsByDifficulty(dificultad)) {
                is DataState.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        questions = result.data.shuffled(),
                        puntuacion = 0,
                        indicePreguntaActual = 0
                    )
                }
                is DataState.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                else -> {}
            }
        }
    }

    // --- MODO MULTIPLAYER ---
    fun conectarAPartida(pin: String, dificultad: Dificultad) {
        Log.d(TAG, "Conectando a partida PIN: $pin") // LOG

        viewModelScope.launch {
            when (val result = repository.getQuestionsByDifficulty(dificultad)) {
                is DataState.Success -> {
                    _uiState.value = _uiState.value.copy(
                        questions = result.data,
                        esMultiplayer = true,
                        puntuacion = 0
                    )
                    Log.d(TAG, "Preguntas descargadas (${result.data.size}). Iniciando listener...") // LOG
                    iniciarListener(pin)
                }
                is DataState.Error -> {
                    Log.e(TAG, "Error descargando preguntas: ${result.message}") // LOG ERROR
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
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
                    Log.d(TAG, "Cambio en partida detectado -> Estado: ${partida.estado}, Índice: ${partida.indicePreguntaActual}") // LOG

                    _uiState.value = _uiState.value.copy(
                        indicePreguntaActual = partida.indicePreguntaActual,
                        estadoPartida = partida.estado
                    )
                }
            }
    }

    // --- ACCIONES DE JUEGO ---
    fun procesarRespuesta(puntosGanados: Int, pinPartida: String?) {
        val nuevaPuntuacion = _uiState.value.puntuacion + puntosGanados

        if (pinPartida == null) {
            // Solitario
            _uiState.value = _uiState.value.copy(
                puntuacion = nuevaPuntuacion,
                indicePreguntaActual = _uiState.value.indicePreguntaActual + 1
            )
        } else {
            // Multiplayer
            Log.d(TAG, "Procesando respuesta. Puntos ganados: $puntosGanados. Total: $nuevaPuntuacion") // LOG
            _uiState.value = _uiState.value.copy(puntuacion = nuevaPuntuacion)
            enviarRespuestaNube(pinPartida, nuevaPuntuacion)
        }
    }

    private fun enviarRespuestaNube(pin: String, puntos: Int) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            Log.e(TAG, "ERROR CRÍTICO: No hay usuario logueado") // LOG ERROR
            _uiState.value = _uiState.value.copy(errorMessage = "Error de usuario")
            return
        }

        val updateData = mapOf(
            "puntuacion" to puntos,
            "haRespondido" to true
        )

        Log.d(TAG, "Enviando a Firestore... Jugador: $uid, Datos: $updateData") // LOG

        db.collection("partidas").document(pin)
            .collection("jugadores").document(uid)
            .update(updateData)
            .addOnSuccessListener {
                Log.d(TAG, "¡ÉXITO! Respuesta guardada en la nube.") // LOG ÉXITO
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "ERROR FATAL al escribir en Firestore: ${e.message}") // LOG ERROR
                _uiState.value = _uiState.value.copy(errorMessage = "Fallo al enviar: ${e.message}")
            }
    }

    // --- MANTENIMIENTO ---
    fun resetQuiz() {
        _uiState.value = QuizUiState()
        partidaListener?.remove()
    }

    fun guardarPuntuacion(pinPartida: String) {
        Log.d(TAG, "Guardando puntuación final: ${_uiState.value.puntuacion}") // LOG
        enviarRespuestaNube(pinPartida, _uiState.value.puntuacion)
    }

    override fun onCleared() {
        super.onCleared()
        partidaListener?.remove()
    }
}