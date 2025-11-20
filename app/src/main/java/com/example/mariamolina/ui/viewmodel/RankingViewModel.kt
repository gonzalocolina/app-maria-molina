package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.model.Jugador
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RankingUiState(
    val isLoading: Boolean = true,
    val jugadores: List<Jugador> = emptyList(),
    val error: String? = null
)

class RankingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState = _uiState.asStateFlow()

    fun escucharRanking(pinPartida: String) {
        if (pinPartida.isBlank()) {
            _uiState.value = RankingUiState(isLoading = false, error = "PIN no válido")
            return
        }

        _uiState.value = RankingUiState(isLoading = true)

        // Escuchamos la subcolección de jugadores de esa partida
        db.collection("partidas").document(pinPartida).collection("jugadores")
            .orderBy("puntuacion", Query.Direction.DESCENDING) // ¡La magia! Ordenados por puntos
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listaRanking = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Jugador::class.java)
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        jugadores = listaRanking
                    )
                }
            }
    }
}