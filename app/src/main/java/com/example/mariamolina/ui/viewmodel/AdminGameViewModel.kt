package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mariamolina.data.model.Jugador
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    override fun onCleared() {
        super.onCleared()
        jugadoresListener?.remove() // Limpiar memoria
    }
}