package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Estados de la pantalla de unirse
sealed class JoinGameState {
    object Idle : JoinGameState()
    data class Error(val message: String) : JoinGameState()
}

@HiltViewModel
class JoinGameViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinGameState>(JoinGameState.Idle)
    val uiState = _uiState.asStateFlow()

}