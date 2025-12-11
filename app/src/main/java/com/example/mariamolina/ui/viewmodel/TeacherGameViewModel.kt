package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.local.ActiveGameSession
import com.example.mariamolina.data.local.GameRole
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
    val tiempoPorPreguntaSeleccionado: Int = 20,  // Tiempo en segundos (por defecto 20)
    val jugadores: List<Jugador> = emptyList(),
    val partida: Partida? = null,
    val preguntaActual: QuizQuestion? = null,
    val preguntas: List<QuizQuestion> = emptyList(),
    val jugadoresQueRespondieron: Int = 0,
    val totalJugadores: Int = 0,
    val ranking: List<Jugador> = emptyList(),
    val gameFinished: Boolean = false,
    val tiempoRestanteSegundos: Int = 0,  // Tiempo restante para la pregunta actual
    val tiempoLimiteExpirado: Boolean = false  // True cuando el tiempo de pregunta ha expirado
)

/**
 * ViewModel para el profesor que controla la partida.
 */
@HiltViewModel
class TeacherGameViewModel @Inject constructor(
    private val repository: MultiplayerRepository,
    private val activeGameSession: ActiveGameSession
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
     * Establece el tiempo por pregunta.
     * Si ya existe una partida, actualiza el tiempo en Firebase.
     */
    fun setTiempoPorPregunta(segundos: Int) {
        val currentPin = _uiState.value.pin
        
        // Actualizar estado local inmediatamente
        _uiState.update { it.copy(tiempoPorPreguntaSeleccionado = segundos) }
        
        // Si ya hay una partida creada, actualizar el tiempo en Firebase
        if (currentPin != null) {
            viewModelScope.launch {
                try {
                    repository.updateGameTime(currentPin, segundos)
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(error = "Error al cambiar tiempo: ${e.message}") 
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
     * Reconecta al lobby de una partida existente (sin crear nueva).
     * Se usa cuando el profesor sale y vuelve a entrar.
     */
    fun reconnectToLobby(pin: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Cargar datos de la partida existente
                val partida =
                    repository.observeGame(pin).first() ?: throw Exception("La partida no existe")

                // Cargar preguntas de la partida existente
                val preguntas = repository.getQuestionsForGame(pin)
                
                // Obtener la dificultad de la partida
                val dificultad = try {
                    Dificultad.valueOf(partida.dificultad)
                } catch (_: Exception) {
                    Dificultad.FACIL
                }

                _uiState.update {
                    it.copy(
                        isLoading = false, 
                        pin = pin,
                        preguntas = preguntas,
                        dificultadSeleccionada = dificultad,
                        tiempoPorPreguntaSeleccionado = partida.tiempoPorPregunta
                    )
                }
                
                // Iniciar observers
                observeGame(pin)
                observePlayers(pin)
                
            } catch (_: Exception) {
                // Si falla (partida no existe), limpiar sesión y crear nueva
                activeGameSession.clearSession()
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = null
                    ) 
                }
                // Crear nueva partida como fallback
                crearPartida()
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
                
                // Guardar sesión activa para reconexión
                activeGameSession.saveSession(pin = pin, role = GameRole.TEACHER)
                
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
     * Abandona el lobby y limpia la sesión.
     * Se usa cuando el profesor decide salir del lobby con el botón Volver.
     */
    fun abandonarLobby() {
        // Cancelar observers
        gameObserverJob?.cancel()
        playersObserverJob?.cancel()
        answeredObserverJob?.cancel()
        rankingObserverJob?.cancel()

        // Limpiar sesión activa
        activeGameSession.clearSession()

        // Resetear estado
        _uiState.update { TeacherGameUiState() }
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
                // Resetear contadores y estado de tiempo
                _uiState.update { 
                    it.copy(
                        jugadoresQueRespondieron = 0,
                        tiempoLimiteExpirado = false,
                        tiempoRestanteSegundos = 0
                    ) 
                }
                repository.advanceToNextQuestion(pin)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Error al avanzar: ${e.message}") 
                }
            }
        }
    }

    /**
     * Actualiza el tiempo restante mostrado al profesor.
     */
    fun updateTiempoRestante(segundos: Int) {
        _uiState.update { it.copy(tiempoRestanteSegundos = segundos) }
    }

    /**
     * Marca que el tiempo límite ha expirado.
     */
    fun marcarTiempoExpirado() {
        _uiState.update { it.copy(tiempoLimiteExpirado = true) }
    }

    /**
     * Finaliza la partida manualmente.
     */
    fun finalizarPartida() {
        val pin = _uiState.value.pin ?: return
        
        viewModelScope.launch {
            try {
                repository.finishGame(pin)
                // Limpiar sesión activa
                activeGameSession.clearSession()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Error al finalizar: ${e.message}") 
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameObserverJob?.cancel()
        playersObserverJob?.cancel()
        answeredObserverJob?.cancel()
        rankingObserverJob?.cancel()
    }
}
