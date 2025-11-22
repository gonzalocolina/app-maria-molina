package com.example.mariamolina.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class PointsOfInterestViewModel : ViewModel() {
    private val _visitedIds = mutableStateOf<Set<String>>(emptySet())
    val visitedIds: Set<String> get() = _visitedIds.value

    fun markAsVisited(id: String) {
        _visitedIds.value = _visitedIds.value + id
    }

    fun isVisited(id: String): Boolean = id in visitedIds

    fun getVisitedCount(total: Int): Int = visitedIds.size

    fun getRemainingCount(total: Int): Int = total - visitedIds.size
}
