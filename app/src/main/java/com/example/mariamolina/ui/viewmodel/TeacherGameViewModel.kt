package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.model.*
import com.example.mariamolina.data.repository.MultiplayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para el profesor.
 */
data class TeacherGameUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pin: String? = null,
    val dificultadSeleccionada: Dificultad = Dificultad.FACIL,
    val jugadores: List<Jugador> = emptyList(),
    val partida: Partida? = null,
    val preguntaActual: QuizQuestion? = null,
    val preguntas: List<QuizQuestion> = emptyList(),
    val jugadoresQueRespondieron: Int = 0,
    val totalJugadores: Int = 0,
    val ranking: List<Jugador> = emptyList(),
    val gameFinished: Boolean = false
)

/**
 * ViewModel para el profesor que controla la partida.
 */
@HiltViewModel
class TeacherGameViewModel @Inject constructor(
    private val repository: MultiplayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherGameUiState())
    val uiState: StateFlow<TeacherGameUiState> = _uiState.asStateFlow()

    private var gameObserverJob: Job? = null
    private var playersObserverJob: Job? = null
    private var answeredObserverJob: Job? = null
    private var rankingObserverJob: Job? = null

    /**
     * Selecciona la dificultad para la partida.
     * Si ya existe una partida, actualiza las preguntas en Firebase.
     */
    fun setDificultad(dificultad: Dificultad) {
        val currentPin = _uiState.value.pin
        
        // Actualizar estado local inmediatamente
        _uiState.update { it.copy(dificultadSeleccionada = dificultad) }
        
        // Si ya hay una partida creada, actualizar las preguntas en Firebase
        if (currentPin != null) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoading = true) }
                    
                    // Actualizar dificultad y obtener nuevas preguntas
                    val nuevasPreguntas = repository.updateGameDifficulty(currentPin, dificultad)
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            preguntas = nuevasPreguntas
                        ) 
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cambiar dificultad: ${e.message}"
                        ) 
                    }
                }
            }
        }
    }

    /**
     * Inicia la observación de una partida existente (llamado desde TeacherGameScreen).
     * Se usa cuando el profesor navega al juego después de crearlo.
     */
    fun startObservingGame(pin: String) {
        // Si ya estamos observando esta partida, no hacer nada
        if (_uiState.value.pin == pin && _uiState.value.partida != null) {
            return
        }
        
        _uiState.update { it.copy(pin = pin, isLoading = true) }
        
        viewModelScope.launch {
            try {
                // Cargar preguntas primero
                val preguntas = repository.getQuestionsForGame(pin)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        preguntas = preguntas
                    ) 
                }
                
                // Iniciar todos los observers
                observeGame(pin)
                observePlayers(pin)
                observeAnsweredCount(pin)
                observeRanking(pin)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al cargar partida: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Crea una nueva partida.
     */
    fun crearPartida() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val pin = repository.createGame(
                    dificultad = _uiState.value.dificultadSeleccionada,
                    totalPreguntas = 10
                )
                
                // Cargar preguntas
                val preguntas = repository.getQuestionsForGame(pin)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        pin = pin,
                        preguntas = preguntas
                    ) 
                }
                
                // Iniciar observers
                observeGame(pin)
                observePlayers(pin)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al crear partida: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Observa cambios en la partida.
     */
    private fun observeGame(pin: String) {
        gameObserverJob?.cancel()
        gameObserverJob = viewModelScope.launch {
            repository.observeGame(pin).collect { partida ->
                _uiState.update { state ->
                    val preguntaActual = partida?.let {
                        if (it.preguntaActualIndex < state.preguntas.size) {
                            state.preguntas[it.preguntaActualIndex]
                        } else null
                    }
                    
                    state.copy(
                        partida = partida,
                        preguntaActual = preguntaActual,
                        gameFinished = partida?.fase == GamePhase.FINISHED
                    )
                }
            }
        }
    }

    /**
     * Observa la lista de jugadores.
     */
    private fun observePlayers(pin: String) {
        playersObserverJob?.cancel()
        playersObserverJob = viewModelScope.launch {
            repository.observePlayers(pin).collect { jugadores ->
                _uiState.update { 
                    it.copy(
                        jugadores = jugadores,
                        totalJugadores = jugadores.size
                    ) 
                }
            }
        }
    }

    /**
     * Observa cuántos jugadores han respondido.
     */
    private fun observeAnsweredCount(pin: String) {
        answeredObserverJob?.cancel()
        answeredObserverJob = viewModelScope.launch {
            repository.observeAnsweredCount(pin).collect { count ->
                _uiState.update { state ->
                    // Solo actualizar el contador, el profesor decide cuándo avanzar
                    state.copy(jugadoresQueRespondieron = count)
                }
            }
        }
    }

    /**
     * Observa el ranking.
     */
    private fun observeRanking(pin: String) {
        rankingObserverJob?.cancel()
        rankingObserverJob = viewModelScope.launch {
            repository.observeRanking(pin).collect { ranking ->
                _uiState.update { it.copy(ranking = ranking) }
            }
        }
    }

    /**
     * Inicia la partida.
     */
    fun empezarJuego() {
        val pin = _uiState.value.pin ?: return
        
        viewModelScope.launch {
            try {
                repository.startGame(pin)
                
                // Iniciar observers adicionales
                observeAnsweredCount(pin)
                observeRanking(pin)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Error al iniciar: ${e.message}") 
                }
            }
        }
    }

    /**
     * Avanza a la siguiente pregunta.
     */
    fun siguientePregunta() {
        val pin = _uiState.value.pin ?: return
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(jugadoresQueRespondieron = 0) }
                repository.advanceToNextQuestion(pin)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Error al avanzar: ${e.message}") 
                }
            }
        }
    }

    /**
     * Finaliza la partida manualmente.
     */
    fun finalizarPartida() {
        val pin = _uiState.value.pin ?: return
        
        viewModelScope.launch {
            try {
                repository.finishGame(pin)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Error al finalizar: ${e.message}") 
                }
            }
        }
    }

    /**
     * Limpia el error actual.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        gameObserverJob?.cancel()
        playersObserverJob?.cancel()
        answeredObserverJob?.cancel()
        rankingObserverJob?.cancel()
    }
}
