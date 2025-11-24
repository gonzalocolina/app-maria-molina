package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.EstadoPartida
import com.example.mariamolina.data.model.Partida
import com.example.mariamolina.data.model.QuizQuestion
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
    // Campos nuevos para Multiplayer
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

    // --- MODO SOLITARIO: Carga manual ---
    fun loadQuestions(dificultad: Dificultad) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            questions = emptyList(),
            esMultiplayer = false // Modo solitario
        )

        viewModelScope.launch {
            when (val result = repository.getQuestionsByDifficulty(dificultad)) {
                is DataState.Success -> {
                    // En solitario sí mezclamos las preguntas
                    val shuffledQuestions = result.data.shuffled()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        questions = shuffledQuestions,
                        puntuacion = 0,
                        indicePreguntaActual = 0
                    )
                }
                is DataState.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                else -> { }
            }
        }
    }

    // --- MODO MULTIPLAYER: Escuchar al Profesor ---
    fun conectarAPartida(pin: String, dificultad: Dificultad) {
        // 1. Cargamos las preguntas (sin mezclar, para que todos tengan el mismo orden)
        viewModelScope.launch {
            when (val result = repository.getQuestionsByDifficulty(dificultad)) {
                is DataState.Success -> {
                    _uiState.value = _uiState.value.copy(
                        questions = result.data, // Orden original de Firebase
                        esMultiplayer = true,
                        puntuacion = 0
                    )
                    // 2. Una vez tenemos las preguntas, escuchamos el estado
                    iniciarListener(pin)
                }
                is DataState.Error -> {
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
                    // Actualizamos la pregunta actual según lo que diga Firebase
                    _uiState.value = _uiState.value.copy(
                        indicePreguntaActual = partida.indicePreguntaActual,
                        estadoPartida = partida.estado
                    )
                }
            }
    }

    // --- ACCIONES DE JUEGO ---

    // Llamado cuando el usuario responde
    fun procesarRespuesta(puntosGanados: Int, pinPartida: String?) {
        // 1. Actualizamos puntuación local
        val nuevaPuntuacion = _uiState.value.puntuacion + puntosGanados

        // 2. Si es Solitario, avanzamos nosotros la pregunta
        if (pinPartida == null) {
            _uiState.value = _uiState.value.copy(
                puntuacion = nuevaPuntuacion,
                indicePreguntaActual = _uiState.value.indicePreguntaActual + 1
            )
        } else {
            // 3. Si es Multiplayer, SOLO actualizamos la puntuación local y enviamos a la nube.
            // NO avanzamos el índice (el listener lo hará cuando el profe cambie).
            _uiState.value = _uiState.value.copy(puntuacion = nuevaPuntuacion)
            enviarRespuestaNube(pinPartida, nuevaPuntuacion)
        }
    }

    private fun enviarRespuestaNube(pin: String, puntos: Int) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Error: No identificado. Reinicia la app.")
            return
        }

        val updateData = mapOf(
            "puntuacion" to puntos,
            "haRespondido" to true
        )

        // Actualizamos Firestore
        db.collection("partidas").document(pin)
            .collection("jugadores").document(uid)
            .update(updateData)
            .addOnSuccessListener {
                println("DEBUG: Respuesta enviada correctamente para $uid en sala $pin")
            }
            .addOnFailureListener { e ->
                println("DEBUG: Error enviando respuesta: ${e.message}")
                _uiState.value = _uiState.value.copy(errorMessage = "Fallo al enviar: ${e.message}")
            }
    }

    // --- MANTENIMIENTO ---
    fun resetQuiz() {
        _uiState.value = QuizUiState() // Reinicia todo
        partidaListener?.remove()
    }

    // Mantenemos este nombre por compatibilidad
    fun scoreAndAdvance(puntosGanados: Int) {
        procesarRespuesta(puntosGanados, null)
    }

    // Guardar puntuación final (para el ranking en modo solitario o fin de juego)
    fun guardarPuntuacion(pinPartida: String) {
        enviarRespuestaNube(pinPartida, _uiState.value.puntuacion)
    }

    override fun onCleared() {
        super.onCleared()
        partidaListener?.remove()
    }
}