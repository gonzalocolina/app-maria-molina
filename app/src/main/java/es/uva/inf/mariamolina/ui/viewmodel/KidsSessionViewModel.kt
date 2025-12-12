package es.uva.inf.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uva.inf.mariamolina.data.local.ActiveGameData
import es.uva.inf.mariamolina.data.local.ActiveGameSession
import es.uva.inf.mariamolina.data.local.GameRole
import es.uva.inf.mariamolina.data.model.GamePhase
import es.uva.inf.mariamolina.data.repository.MultiplayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado para la verificación de sesión activa en Kids.
 */
data class KidsSessionUiState(
    val isChecking: Boolean = true,
    val activeSession: ActiveGameData? = null,
    val sessionValid: Boolean = false,  // True si la partida sigue activa en Firebase
    val shouldNavigateTo: String? = null  // Ruta a la que debe navegar
)

/**
 * ViewModel para verificar si hay una sesión de juego activa
 * y determinar a qué pantalla reconectar al usuario.
 */
@HiltViewModel
class KidsSessionViewModel @Inject constructor(
    private val activeGameSession: ActiveGameSession,
    private val repository: MultiplayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KidsSessionUiState())
    val uiState: StateFlow<KidsSessionUiState> = _uiState.asStateFlow()

    init {
        checkActiveSession()
    }

    /**
     * Verifica si hay una sesión activa y si la partida sigue en curso.
     */
    fun checkActiveSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }

            val session = activeGameSession.getActiveSession()

            if (session == null) {
                // No hay sesión guardada
                _uiState.update { 
                    it.copy(
                        isChecking = false, 
                        activeSession = null,
                        sessionValid = false,
                        shouldNavigateTo = null
                    ) 
                }
                return@launch
            }

            // Verificar si la partida sigue activa en Firebase
            try {
                val partida = repository.observeGame(session.pin).first()
                
                if (partida == null || partida.fase == GamePhase.FINISHED) {
                    // La partida ya no existe o terminó
                    activeGameSession.clearSession()
                    _uiState.update { 
                        it.copy(
                            isChecking = false, 
                            activeSession = null,
                            sessionValid = false,
                            shouldNavigateTo = null
                        ) 
                    }
                } else {
                    // La partida sigue activa - determinar a dónde navegar
                    val destination = when (session.role) {
                        GameRole.TEACHER -> {
                            when (partida.fase) {
                                GamePhase.LOBBY -> "teacher_lobby?pin=${session.pin}"  // Pasar PIN para reconectar
                                else -> "teacher_game/${session.pin}"
                            }
                        }
                        GameRole.STUDENT -> {
                            when (partida.fase) {
                                GamePhase.LOBBY -> "student_lobby/${session.pin}"
                                else -> "student_game/${session.pin}"
                            }
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isChecking = false, 
                            activeSession = session,
                            sessionValid = true,
                            shouldNavigateTo = destination
                        ) 
                    }
                }
            } catch (e: Exception) {
                // Error al verificar - limpiar sesión por seguridad
                activeGameSession.clearSession()
                _uiState.update { 
                    it.copy(
                        isChecking = false, 
                        activeSession = null,
                        sessionValid = false,
                        shouldNavigateTo = null
                    ) 
                }
            }
        }
    }

    /**
     * El usuario decidió no reconectar - limpiar sesión.
     */
    fun declineReconnect() {
        activeGameSession.clearSession()
        _uiState.update { 
            it.copy(
                activeSession = null,
                sessionValid = false,
                shouldNavigateTo = null
            ) 
        }
    }

    /**
     * Resetea el estado de navegación después de navegar.
     */
    fun onNavigated() {
        _uiState.update { it.copy(shouldNavigateTo = null) }
    }
}
