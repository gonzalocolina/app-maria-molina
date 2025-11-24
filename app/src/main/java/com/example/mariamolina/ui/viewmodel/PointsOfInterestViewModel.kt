package com.example.mariamolina.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class PointsOfInterestViewModel : ViewModel() {

    val visitados = mutableStateOf(setOf<String>())

    fun toggleVisited(id: String) {
        visitados.value = if (id in visitados.value) visitados.value - id else visitados.value + id
    }

}
