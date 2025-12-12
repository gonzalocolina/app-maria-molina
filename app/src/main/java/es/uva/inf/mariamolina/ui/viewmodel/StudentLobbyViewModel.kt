package es.uva.inf.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uva.inf.mariamolina.data.model.EstadoPartida
import es.uva.inf.mariamolina.data.model.Partida
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estados posibles de la pantalla
sealed class StudentLobbyState {
    object Loading : StudentLobbyState()
    data class Waiting(val partida: Partida) : StudentLobbyState() // Esperando
    data class GameStarted(val dificultad: String) : StudentLobbyState() // ¡Empezó!
    data class Error(val message: String) : StudentLobbyState()
}

@HiltViewModel
class StudentLobbyViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow<StudentLobbyState>(StudentLobbyState.Loading)
    val uiState = _uiState.asStateFlow()

    private var partidaListener: ListenerRegistration? = null

    fun escucharPartida(pin: String) {
        _uiState.value = StudentLobbyState.Loading

        // Escuchamos el documento de la partida en tiempo real
        val docRef = db.collection("partidas").document(pin)

        partidaListener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                _uiState.value = StudentLobbyState.Error(e.message ?: "Error de conexión")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val partida = snapshot.toObject(Partida::class.java)
                if (partida != null) {
                    when (partida.estado) {
                        EstadoPartida.JUGANDO -> {
                            // Si el estado cambia a JUGANDO, avisamos a la UI
                            // (Por defecto asumimos FACIL si no se guardó dificultad en la partida)
                            _uiState.value = StudentLobbyState.GameStarted("FACIL")
                        }
                        else -> {
                            // Si no, seguimos esperando
                            _uiState.value = StudentLobbyState.Waiting(partida)
                        }
                    }
                }
            } else {
                _uiState.value = StudentLobbyState.Error("La partida se ha cerrado")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        partidaListener?.remove()
    }
}