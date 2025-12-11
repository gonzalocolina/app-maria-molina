package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.local.ActiveGameSession
import com.example.mariamolina.data.local.GameRole
import com.example.mariamolina.data.model.GamePhase
import com.example.mariamolina.data.model.Jugador
import com.example.mariamolina.data.model.OpcionRespuesta
import com.example.mariamolina.data.model.Partida
import com.example.mariamolina.data.model.QuizQuestion
import com.example.mariamolina.data.repository.MultiplayerRepository
import com.example.mariamolina.domain.scoring.KahootScoringStrategy
import com.example.mariamolina.domain.scoring.ScoringStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para el alumno.
 */
data class StudentGameUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pin: String = "",
    val nickname: String = "",
    val partida: Partida? = null,
    val preguntaActual: QuizQuestion? = null,
    val opcionesAleatorias: List<OpcionRespuesta> = emptyList(),
    val respuestaEnviada: Boolean = false,
    val respuestaCorrecta: Boolean? = null,
    val puntosObtenidos: Int = 0,
    val miPuntuacionTotal: Int = 0,
    val ranking: List<Jugador> = emptyList(),
    val miPosicion: Int = 0,
    val gamePhase: GamePhase = GamePhase.LOBBY,
    val gameFinished: Boolean = false,
    val joinSuccess: Boolean = false,
    val totalJugadores: Int = 0,
    val jugadoresQueRespondieron: Int = 0,
    val allAnswered: Boolean = false,
    val tiempoAgotado: Boolean = false,
    val opcionSeleccionadaIndex: Int = -1,  // Índice de la opción seleccionada por el alumno
    val puntosContabilizados: Boolean = false  // Si los puntos de la pregunta actual ya se sumaron
)

/**
 * ViewModel para el alumno que participa en la partida.
 * Usa patrón Strategy para puntuación y Observer (Flow) para tiempo real.
 */
@HiltViewModel
class StudentGameViewModel @Inject constructor(
    private val repository: MultiplayerRepository,
    private val activeGameSession: ActiveGameSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentGameUiState())
    val uiState: StateFlow<StudentGameUiState> = _uiState.asStateFlow()

    // Patrón Strategy para puntuación
    private val scoringStrategy: ScoringStrategy = KahootScoringStrategy()
    
    // Jobs para observadores
    private var gameObserverJob: Job? = null
    private var rankingObserverJob: Job? = null
    private var answeredObserverJob: Job? = null
    private var playersObserverJob: Job? = null
    
    // Cache de preguntas
    private var preguntas: List<QuizQuestion> = emptyList()
    
    // Tiempo de inicio de pregunta (para calcular tiempo de respuesta)
    private var questionStartTime: Long = 0L
    private val totalTimeMs: Long = 20000L  // 20 segundos por pregunta

    /**
     * Intenta unirse a una partida.
     */
    fun joinGame(pin: String, nickname: String) {
        if (pin.isBlank() || nickname.isBlank()) {
            _uiState.update { it.copy(error = "Introduce PIN y nombre") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.joinGame(pin, nickname)
            
            result.fold(
                onSuccess = {
                    // Guardar sesión activa para reconexión
                    activeGameSession.saveSession(
                        pin = pin, 
                        role = GameRole.STUDENT, 
                        nickname = nickname
                    )
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            pin = pin, 
                            nickname = nickname,
                            joinSuccess = true
                        ) 
                    }
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = e.message ?: "Error al unirse"
                        ) 
                    }
                }
            )
        }
    }

    /**
     * Inicia la escucha de la partida (llamar desde StudentLobby).
     */
    fun startObservingGame(pin: String) {
        _uiState.update { it.copy(pin = pin, isLoading = true) }
        
        viewModelScope.launch {
            // IMPORTANTE: Cargar preguntas PRIMERO y esperar
            // Esto evita race condition donde el observer recibe
            // SHOWING_QUESTION antes de tener las preguntas cargadas
            try {
                preguntas = repository.getQuestionsForGame(pin)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error cargando preguntas: ${e.message}") }
            }
            
            // DESPUÉS de tener las preguntas, iniciar los observers
            observeGame(pin)
            observeRanking(pin)
            observeAnsweredCount(pin)
            observePlayersCount(pin)
        }
    }

    /**
     * Observa cuántos jugadores han respondido.
     * Cuando todos responden, contabiliza los puntos.
     */
    private fun observeAnsweredCount(pin: String) {
        answeredObserverJob?.cancel()
        answeredObserverJob = viewModelScope.launch {
            repository.observeAnsweredCount(pin).collect { count ->
                val state = _uiState.value
                val allAnswered = state.totalJugadores in 1..count

                // Si todos han respondido y los puntos no se han contabilizado aún
                if (allAnswered && state.respuestaEnviada && !state.puntosContabilizados && state.puntosObtenidos > 0) {
                    contabilizarPuntos()
                }

                _uiState.update { s ->
                    s.copy(
                        jugadoresQueRespondieron = count,
                        allAnswered = allAnswered
                    )
                }
            }
        }
    }

    /**
     * Observa el número de jugadores.
     */
    private fun observePlayersCount(pin: String) {
        playersObserverJob?.cancel()
        playersObserverJob = viewModelScope.launch {
            repository.observePlayers(pin).collect { jugadores ->
                _uiState.update { state ->
                    val allAnswered = state.jugadoresQueRespondieron >= jugadores.size && jugadores.isNotEmpty()
                    state.copy(
                        totalJugadores = jugadores.size,
                        allAnswered = allAnswered
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
                if (partida == null) {
                    _uiState.update { it.copy(error = "La partida se ha cerrado") }
                    return@collect
                }
                
                val previousPhase = _uiState.value.gamePhase
                val currentPhase = partida.fase
                
                // Detectar cambio de pregunta
                val previousIndex = _uiState.value.partida?.preguntaActualIndex ?: -1
                val currentIndex = partida.preguntaActualIndex
                
                // Si cambió la fase a SHOWING_QUESTION, resetear estado de respuesta
                val shouldResetAnswer = (currentPhase == GamePhase.SHOWING_QUESTION && 
                    (previousPhase != GamePhase.SHOWING_QUESTION || currentIndex != previousIndex))
                
                // Obtener pregunta actual
                val preguntaActual = if (currentIndex < preguntas.size && 
                    currentPhase == GamePhase.SHOWING_QUESTION) {
                    preguntas[currentIndex]
                } else null
                
                // Mezclar opciones cuando cambia la pregunta
                val opcionesAleatorias = if (shouldResetAnswer && preguntaActual != null) {
                    questionStartTime = System.currentTimeMillis()
                    preguntaActual.opciones.shuffled()
                } else if (preguntaActual != null && _uiState.value.opcionesAleatorias.isEmpty()) {
                    questionStartTime = System.currentTimeMillis()
                    preguntaActual.opciones.shuffled()
                } else {
                    _uiState.value.opcionesAleatorias
                }
                
                // Variables para el estado de respuesta (pueden ser sobrescritas por Firebase)
                var respuestaEnviada = if (shouldResetAnswer) false else _uiState.value.respuestaEnviada
                var respuestaCorrecta = if (shouldResetAnswer) null else _uiState.value.respuestaCorrecta
                var puntosObtenidos = if (shouldResetAnswer) 0 else _uiState.value.puntosObtenidos
                var opcionSeleccionadaIndex = if (shouldResetAnswer) -1 else _uiState.value.opcionSeleccionadaIndex
                var tiempoAgotado = if (shouldResetAnswer) false else _uiState.value.tiempoAgotado
                var puntosContabilizados = if (shouldResetAnswer) false else _uiState.value.puntosContabilizados

                // Verificar si ya respondió esta pregunta (cuando entra/vuelve a la pantalla)
                // Solo verificar si estamos en SHOWING_QUESTION y no hemos marcado respuestaEnviada localmente
                if (currentPhase == GamePhase.SHOWING_QUESTION && !respuestaEnviada) {
                    try {
                        val respuestaPrevia = repository.getPlayerAnswer(pin, currentIndex)
                        if (respuestaPrevia != null) {
                            // El jugador ya respondió esta pregunta, restaurar el estado
                            respuestaEnviada = true
                            respuestaCorrecta = respuestaPrevia.esCorrecta
                            puntosObtenidos = respuestaPrevia.puntosObtenidos
                            opcionSeleccionadaIndex = respuestaPrevia.opcionSeleccionada
                            tiempoAgotado = respuestaPrevia.opcionSeleccionada == -1
                            puntosContabilizados = respuestaPrevia.puntosContabilizados
                        }
                    } catch (_: Exception) {
                        // Ignorar error, si falla la verificación seguimos con el estado actual
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        partida = partida,
                        gamePhase = currentPhase,
                        preguntaActual = preguntaActual,
                        opcionesAleatorias = opcionesAleatorias,
                        respuestaEnviada = respuestaEnviada,
                        respuestaCorrecta = respuestaCorrecta,
                        puntosObtenidos = puntosObtenidos,
                        opcionSeleccionadaIndex = opcionSeleccionadaIndex,
                        tiempoAgotado = tiempoAgotado,
                        puntosContabilizados = puntosContabilizados,
                        gameFinished = currentPhase == GamePhase.FINISHED
                    )
                }
            }
        }
    }

    /**
     * Observa el ranking en tiempo real.
     */
    private fun observeRanking(pin: String) {
        rankingObserverJob?.cancel()
        rankingObserverJob = viewModelScope.launch {
            repository.observeRanking(pin).collect { ranking ->
                // Encontrar mi posición
                val uid = try { repository.getCurrentUserUid() } catch (_: Exception) { "" }
                val miPosicion = ranking.indexOfFirst { it.uid == uid } + 1
                val miPuntuacion = ranking.find { it.uid == uid }?.puntuacion ?: 0
                
                _uiState.update { 
                    it.copy(
                        ranking = ranking,
                        miPosicion = miPosicion,
                        miPuntuacionTotal = miPuntuacion
                    ) 
                }
            }
        }
    }

    /**
     * Envía la respuesta del jugador.
     */
    fun submitAnswer(opcionIndex: Int) {
        val state = _uiState.value
        if (state.respuestaEnviada) return

        val opcion = state.opcionesAleatorias.getOrNull(opcionIndex) ?: return
        
        // Calcular tiempo de respuesta
        val tiempoRespuesta = System.currentTimeMillis() - questionStartTime
        val tiempoRestante = (totalTimeMs - tiempoRespuesta).coerceAtLeast(0)
        
        // Calcular puntos usando Strategy
        val esCorrecta = opcion.esCorrecta
        val puntos = scoringStrategy.calculateScore(esCorrecta, tiempoRestante, totalTimeMs)
        
        // Actualizar UI inmediatamente
        _uiState.update { 
            it.copy(
                respuestaEnviada = true,
                respuestaCorrecta = esCorrecta,
                puntosObtenidos = puntos,
                opcionSeleccionadaIndex = opcionIndex  // Guardar índice de opción seleccionada
            )
        }
        
        // Enviar a Firebase
        viewModelScope.launch {
            try {
                repository.submitAnswer(
                    pin = state.pin,
                    preguntaIndex = state.partida?.preguntaActualIndex ?: 0,
                    opcionSeleccionada = opcionIndex,
                    esCorrecta = esCorrecta,
                    tiempoRespuestaMs = tiempoRespuesta,
                    puntosObtenidos = puntos
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al enviar respuesta: ${e.message}") }
            }
        }
    }

    /**
     * Maneja cuando se acaba el tiempo sin responder.
     */
    fun onTimeUp() {
        if (_uiState.value.respuestaEnviada) return
        
        val state = _uiState.value
        
        // Marcar como respondido (incorrectamente, 0 puntos) y tiempo agotado
        _uiState.update { 
            it.copy(
                respuestaEnviada = true,
                respuestaCorrecta = false,
                puntosObtenidos = 0,
                tiempoAgotado = true
            ) 
        }
        
        // Enviar respuesta vacía a Firebase
        viewModelScope.launch {
            try {
                repository.submitAnswer(
                    pin = state.pin,
                    preguntaIndex = state.partida?.preguntaActualIndex ?: 0,
                    opcionSeleccionada = -1,
                    esCorrecta = false,
                    tiempoRespuestaMs = totalTimeMs,
                    puntosObtenidos = 0
                )
            } catch (_: Exception) {
                // Ignorar error si no se pudo enviar
            }
        }
    }

    /**
     * Contabiliza los puntos de la respuesta actual.
     * Se llama solo cuando se muestra el feedback (todos respondieron o tiempo agotado).
     */
    private fun contabilizarPuntos() {
        val state = _uiState.value
        if (state.puntosContabilizados || state.puntosObtenidos <= 0) return

        // Marcar como contabilizados inmediatamente para evitar duplicados
        _uiState.update { it.copy(puntosContabilizados = true) }

        viewModelScope.launch {
            try {
                repository.addPointsForAnswer(
                    pin = state.pin,
                    preguntaIndex = state.partida?.preguntaActualIndex ?: 0,
                    puntos = state.puntosObtenidos
                )
            } catch (_: Exception) {
                // Si falla, desmarcar para reintentar después
                _uiState.update { it.copy(puntosContabilizados = false) }
            }
        }
    }

    /**
     * Abandona el lobby de la partida (elimina al jugador de la lista).
     * Se usa cuando el alumno pulsa "Volver" en la sala de espera.
     */
    fun leaveLobby() {
        val pin = _uiState.value.pin
        if (pin.isBlank()) return

        viewModelScope.launch {
            // Eliminar al jugador de Firebase
            repository.leaveGame(pin)

            // Cancelar observers
            gameObserverJob?.cancel()
            rankingObserverJob?.cancel()
            answeredObserverJob?.cancel()
            playersObserverJob?.cancel()

            // Limpiar sesión activa
            activeGameSession.clearSession()

            // Resetear estado
            _uiState.value = StudentGameUiState()
        }
    }

    /**
     * Limpia el error actual.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Resetea el estado para una nueva partida.
     */
    fun reset() {
        gameObserverJob?.cancel()
        rankingObserverJob?.cancel()
        answeredObserverJob?.cancel()
        playersObserverJob?.cancel()
        preguntas = emptyList()
        // Limpiar sesión activa
        activeGameSession.clearSession()
        _uiState.value = StudentGameUiState()
    }

    override fun onCleared() {
        super.onCleared()
        gameObserverJob?.cancel()
        rankingObserverJob?.cancel()
        answeredObserverJob?.cancel()
        playersObserverJob?.cancel()
    }
}
