package com.example.mariamolina.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mariamolina.data.model.puntosDeInteres
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PointsOfInterestViewModel(private val context: Context) : ViewModel() {

    private val prefs = context.getSharedPreferences("visited_points", Context.MODE_PRIVATE)

    // Estado inicial cargado desde SharedPreferences
    private val _visitados = MutableStateFlow(loadVisitedPoints())
    val visitados: StateFlow<Set<String>> = _visitados

    private fun loadVisitedPoints(): Set<String> {
        val json = prefs.getString("visited", "[]") ?: "[]"
        return try {
            // Usar Gson para deserializar, pero como no está importado, usar una lista simple
            // Para simplicidad, asumir que guardamos como string separado por comas
            json.split(",").filter { it.isNotEmpty() }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun saveVisitedPoints(visited: Set<String>) {
        val json = visited.joinToString(",")
        prefs.edit().putString("visited", json).apply()
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
