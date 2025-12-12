package es.uva.inf.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uva.inf.mariamolina.data.model.Dificultad
import es.uva.inf.mariamolina.data.model.QuizQuestion
import es.uva.inf.mariamolina.data.repository.DataState
import es.uva.inf.mariamolina.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI del Quiz:
data class QuizUiState(
    val isLoading: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val errorMessage: String? = null,
    val puntuacion: Int = 0,
    val aciertos: Int = 0,  // Número de respuestas correctas
    val indicePreguntaActual: Int = 0,
    val dificultadSeleccionada: Dificultad = Dificultad.FACIL
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository
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
    fun scoreAndAdvance(puntosGanados: Int, esAcierto: Boolean = false) {
        // Lógica de puntuación. Solo sumamos y avanzamos.
        val nuevaPuntuacion = _uiState.value.puntuacion + puntosGanados
        val nuevoIndice = _uiState.value.indicePreguntaActual + 1
        val nuevosAciertos = if (esAcierto) _uiState.value.aciertos + 1 else _uiState.value.aciertos

        _uiState.value = _uiState.value.copy(
            puntuacion = nuevaPuntuacion,
            indicePreguntaActual = nuevoIndice,
            aciertos = nuevosAciertos
        )
    }

    // Usado cuando se sale del quiz para limpiar
    fun resetQuiz() {
        _uiState.value = _uiState.value.copy(
            questions = emptyList(),
            puntuacion = 0,
            aciertos = 0,
            indicePreguntaActual = 0
        )
    }
}

