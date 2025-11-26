package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.model.Jugador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Estados de la pantalla de unirse
sealed class JoinGameState {
    object Idle : JoinGameState()
    object Loading : JoinGameState()
    object Success : JoinGameState() // ¡Se unió con éxito!
    data class Error(val message: String) : JoinGameState()
}

@HiltViewModel
class JoinGameViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinGameState>(JoinGameState.Idle)
    val uiState = _uiState.asStateFlow()

    fun joinGame(pin: String, nickname: String) {
        if (pin.isBlank() || nickname.isBlank()) {
            _uiState.value = JoinGameState.Error("Introduce PIN y Nombre")
            return
        }

        _uiState.value = JoinGameState.Loading

        viewModelScope.launch {
            try {
                // 1. Autenticación Anónima (si no hay usuario actual)
                var user = auth.currentUser
                if (user == null) {
                    val authResult = auth.signInAnonymously().await()
                    user = authResult.user
                }

                if (user == null) throw Exception("Error de autenticación")

                // 2. Verificar si la partida existe
                // Buscamos en la colección "partidas" el documento con ID = PIN
                val partidaRef = db.collection("partidas").document(pin)
                val partidaSnapshot = partidaRef.get().await()

                if (!partidaSnapshot.exists()) {
                    _uiState.value = JoinGameState.Error("No existe una partida con el PIN $pin")
                    return@launch
                }

                // 3. Añadirse a la lista de jugadores
                // Creamos/Sobrescribimos el documento del jugador en la subcolección
                val nuevoJugador = Jugador(
                    uid = user.uid,
                    nickname = nickname,
                    puntuacion = 0
                )

                // Ruta: partidas/{PIN}/jugadores/{UID_DEL_USUARIO}
                partidaRef.collection("jugadores")
                    .document(user.uid)
                    .set(nuevoJugador)
                    .await()

                // 4. Éxito -> La UI deberá navegar a la "Sala de Espera"
                _uiState.value = JoinGameState.Success

            } catch (e: Exception) {
                _uiState.value = JoinGameState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}