package com.example.mariamolina.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PointsOfInterestViewModel @Inject constructor(
    private val prefs: SharedPreferences
) : ViewModel() {

    // Estado inicial cargado desde SharedPreferences
    private val _visitados = MutableStateFlow(loadVisitedPoints())
    val visitados: StateFlow<Set<String>> = _visitados

    private fun loadVisitedPoints(): Set<String> {
        val stored = prefs.getString("visited", "") ?: ""
        return if (stored.isBlank()) emptySet() else stored.split(",").filter { it.isNotEmpty() }.toSet()
    }

    private fun saveVisitedPoints(visited: Set<String>) {
        val joined = visited.joinToString(",")
        prefs.edit().putString("visited", joined).apply()
    }

    // Función para alternar el estado de un punto
    fun toggleVisited(id: String) {
        viewModelScope.launch {
            val current = _visitados.value
            val newSet = if (id in current) current - id else current + id
            _visitados.value = newSet
            saveVisitedPoints(newSet)
        }
    }
}
