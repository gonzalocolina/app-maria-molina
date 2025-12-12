package es.uva.inf.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uva.inf.mariamolina.data.model.EstadoPartida
import es.uva.inf.mariamolina.data.model.Jugador
import es.uva.inf.mariamolina.data.model.Partida
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random
import javax.inject.Inject

data class AdminUiState(
    val isLoading: Boolean = false,
    val pinGenerado: String? = null, // El PIN de la partida actual
    val jugadoresUnidos: List<Jugador> = emptyList(), // Lista en tiempo real
    val error: String? = null
)

@HiltViewModel
class AdminGameViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    private var jugadoresListener: ListenerRegistration? = null

    // 1. Generar PIN y Crear Partida
    fun crearPartida() {
        _uiState.value = AdminUiState(isLoading = true)

        viewModelScope.launch {
            try {
                // Generamos un PIN aleatorio de 4 dígitos
                val nuevoPin = generatePin()

                // Creamos el objeto Partida
                val nuevaPartida = Partida(
                    pin = nuevoPin,
                    estado = EstadoPartida.ESPERANDO,
                    preguntaActualIndex = 0
                )

                // Guardamos en Firestore: partidas/{PIN}
                db.collection("partidas").document(nuevoPin).set(nuevaPartida).await()

                // Si todo va bien, actualizamos estado y empezamos a escuchar jugadores
                _uiState.value = AdminUiState(isLoading = false, pinGenerado = nuevoPin)
                escucharJugadores(nuevoPin)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al crear: ${e.message}"
                )
            }
        }
    }

    // 2. Escuchar en Tiempo Real quién se une
    private fun escucharJugadores(pin: String) {
        // Limpiamos listener anterior si hubiera
        jugadoresListener?.remove()

        val coleccionJugadores = db.collection("partidas").document(pin).collection("jugadores")

        // ¡ESTO ES LA MAGIA! addSnapshotListener se ejecuta cada vez que alguien entra
        jugadoresListener = coleccionJugadores.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            if (snapshot != null) {
                val listaJugadores = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Jugador::class.java)
                }
                _uiState.value = _uiState.value.copy(jugadoresUnidos = listaJugadores)
            }
        }
    }

    // 3. Empezar el Juego (Lanzar la partida)
    fun empezarJuego() {
        val pin = _uiState.value.pinGenerado ?: return

        viewModelScope.launch {
            // Cambiamos el estado a JUGANDO.
            // ¡Esto disparará la navegación en los móviles de los alumnos!
            db.collection("partidas").document(pin)
                .update("estado", EstadoPartida.JUGANDO)
        }
    }

    // Utilidad: Generar PIN de 4 dígitos (1000 a 9999)
    private fun generatePin(): String {
        return Random.nextInt(1000, 9999).toString()
    }

    override fun onCleared() {
        super.onCleared()
        jugadoresListener?.remove() // Limpiar memoria
    }
}