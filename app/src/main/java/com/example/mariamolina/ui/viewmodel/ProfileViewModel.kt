package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val isSaved: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val usuario = repository.getOrCreateUser()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nickname = usuario.nickname
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateNickname(newNickname: String) {
        _uiState.value = _uiState.value.copy(nickname = newNickname, isSaved = false)
    }

    fun saveProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                repository.saveNickname(_uiState.value.nickname)
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}