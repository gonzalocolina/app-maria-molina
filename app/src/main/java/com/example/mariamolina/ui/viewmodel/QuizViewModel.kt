package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.QuizQuestion
import com.example.mariamolina.data.repository.DataState
import com.example.mariamolina.data.repository.QuizRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado de la UI del Quiz:
data class QuizUiState(
    val isLoading: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val errorMessage: String? = null,
    val puntuacion: Int = 0,
    val indicePreguntaActual: Int = 0,
    val dificultadSeleccionada: Dificultad = Dificultad.FACIL
)

class QuizViewModel(
    // Inyectamos el repositorio. Para Compose, lo haremos simple por ahora.
    private val repository: QuizRepository = QuizRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // --- Lógica de Carga de Preguntas ---
    fun loadQuestions(dificultad: Dificultad) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            questions = emptyList(),
            dificultadSeleccionada = dificultad
        )

        viewModelScope.launch {
            when (val result = repository.getQuestionsByDifficulty(dificultad)) {
                is DataState.Success -> {
                    // ¡Aleatorizamos las preguntas aquí!
                    val shuffledQuestions = result.data.shuffled()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        questions = shuffledQuestions,
                        puntuacion = 0,
                        indicePreguntaActual = 0 // Reiniciar quiz
                    )
                }
                is DataState.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> { /* Ignorar Idle/Loading */ }
            }
        }
    }

    // --- Lógica del Juego ---
    fun scoreAndAdvance(puntosGanados: Int) {
        // Lógica de puntuación. Solo sumamos y avanzamos.
        val nuevaPuntuacion = _uiState.value.puntuacion + puntosGanados
        val nuevoIndice = _uiState.value.indicePreguntaActual + 1

        _uiState.value = _uiState.value.copy(
            puntuacion = nuevaPuntuacion,
            indicePreguntaActual = nuevoIndice
        )
    }

    // Usado cuando se sale del quiz para limpiar
    fun resetQuiz() {
        _uiState.value = _uiState.value.copy(
            questions = emptyList(),
            puntuacion = 0,
            indicePreguntaActual = 0
        )
    }

    //Para que el ranking se mueva
    // Añadir dentro de QuizViewModel

    fun guardarPuntuacionEnFirebase(pinPartida: String, puntuacionFinal: Int) {
        val currentUser = com.google.firebase.auth.ktx.auth.currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            // Actualizamos SOLO el campo puntuación de este jugador en esa partida
            db.collection("partidas").document(pinPartida)
                .collection("jugadores").document(currentUser.uid)
                .update("puntuacion", puntuacionFinal)
        }
    }

    Y luego, en tu `QuizGameScreen.kt`, cuando el juego termina (`onQuizFinished`), llamas a esta función: `viewModel.guardarPuntuacionEnFirebase("1234", puntuacion)`. (Recuerda que necesitarás pasarle el PIN real a la pantalla de juego para que sepa dónde guardar).
}

