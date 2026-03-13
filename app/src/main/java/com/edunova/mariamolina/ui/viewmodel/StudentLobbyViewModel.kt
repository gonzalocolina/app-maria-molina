package com.edunova.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Estados posibles de la pantalla
sealed class StudentLobbyState {
    object Loading : StudentLobbyState()
    data class Error(val message: String) : StudentLobbyState()
}

@HiltViewModel
class StudentLobbyViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow<StudentLobbyState>(StudentLobbyState.Loading)
    val uiState = _uiState.asStateFlow()

    private var partidaListener: ListenerRegistration? = null

    override fun onCleared() {
        super.onCleared()
        partidaListener?.remove()
    }
}