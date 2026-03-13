package com.edunova.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edunova.mariamolina.data.local.ActiveGameData
import com.edunova.mariamolina.data.local.ActiveGameSession
import com.edunova.mariamolina.data.local.GameRole
import com.edunova.mariamolina.data.local.TeacherAuthSession
import com.edunova.mariamolina.data.model.GamePhase
import com.edunova.mariamolina.data.repository.MultiplayerRepository
import com.google.firebase.auth.FirebaseAuth
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
    val shouldNavigateTo: String? = null,  // Ruta a la que debe navegar
    val isTeacherAuthenticated: Boolean = false  // True si el profesor ya introdujo la contraseña
)

/**
 * ViewModel para verificar si hay una sesión de juego activa
 * y determinar a qué pantalla reconectar al usuario.
 */
@HiltViewModel
class KidsSessionViewModel @Inject constructor(
    private val activeGameSession: ActiveGameSession,
    private val repository: MultiplayerRepository,
    private val teacherAuthSession: TeacherAuthSession,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(KidsSessionUiState())
    val uiState: StateFlow<KidsSessionUiState> = _uiState.asStateFlow()

    // Estado para errores de login
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // FUNCIÓN: Iniciar sesión con Email y Contraseña
    fun signInWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        _loginError.value = null
        if (email.isBlank() || pass.isBlank()) {
            _loginError.value = "Por favor, rellena todos los campos"
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                setTeacherAuthenticated() // Marcamos como profe
                onSuccess()
            }
            .addOnFailureListener { e ->
                // Si falla, probamos a registrarlo (por si es usuario nuevo)
                // Ojo: En una app real, lo ideal es tener botones separados,
                // pero para simplificar, si falla el login, intentamos el registro.
                registerWithEmail(email, pass, onSuccess)
            }
    }

    private fun registerWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                setTeacherAuthenticated()
                onSuccess()
            }
            .addOnFailureListener { e ->
                _loginError.value = "Error: ${e.localizedMessage}"
            }
    }

    fun clearError() {
        _loginError.value = null
    }

    init {
        checkActiveSession()
    }

    /**
     * Verifica si hay una sesión activa y si la partida sigue en curso.
     */
    fun checkActiveSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }

            // Verificar si el profesor está autenticado
            val isTeacherAuth = teacherAuthSession.isAuthenticated()

            val session = activeGameSession.getActiveSession()

            if (session == null) {
                // No hay sesión guardada - pero verificar si profesor está autenticado
                _uiState.update {
                    it.copy(
                        isChecking = false, 
                        activeSession = null,
                        sessionValid = false,
                        shouldNavigateTo = if (isTeacherAuth) "admin_lobby" else null,
                        isTeacherAuthenticated = isTeacherAuth
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
                            shouldNavigateTo = if (isTeacherAuth) "admin_lobby" else null,
                            isTeacherAuthenticated = isTeacherAuth
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
                            shouldNavigateTo = destination,
                            isTeacherAuthenticated = isTeacherAuth
                        )
                    }
                }
            } catch (_: Exception) {
                // Error al verificar - limpiar sesión por seguridad
                activeGameSession.clearSession()
                _uiState.update { 
                    it.copy(
                        isChecking = false,
                        activeSession = null,
                        sessionValid = false,
                        shouldNavigateTo = if (isTeacherAuth) "admin_lobby" else null,
                        isTeacherAuthenticated = isTeacherAuth
                    )
                }
            }
        }
    }

    /**
     * Marca al profesor como autenticado.
     */
    fun setTeacherAuthenticated() {
        teacherAuthSession.setAuthenticated(true)
        _uiState.update { it.copy(isTeacherAuthenticated = true) }
    }

    /**
     * Cierra la sesión del profesor (logout).
     */
    fun logoutTeacher() {
        teacherAuthSession.clearAuthentication()
        _uiState.update { it.copy(isTeacherAuthenticated = false) }
    }

    /**
     * Resetea el estado de navegación después de navegar.
     */
    fun onNavigated() {
        _uiState.update { it.copy(shouldNavigateTo = null) }
    }
}
