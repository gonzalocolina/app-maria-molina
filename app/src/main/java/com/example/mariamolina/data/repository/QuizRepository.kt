package com.example.mariamolina.data.repository

import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.QuizQuestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

sealed class DataState<out T> {
    data class Success<out T>(val data: T) : DataState<T>()
    data class Error(val message: String) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
    object Idle : DataState<Nothing>()
}

class QuizRepository(
    private val firestore: FirebaseFirestore
) {
    suspend fun getQuestionsByDifficulty(dificultad: Dificultad): DataState<List<QuizQuestion>> {
        return try {
            val dificultadString = dificultad.name.uppercase()

            val snapshot = firestore.collection("quizzes")
                .whereEqualTo("dificultad", dificultadString)
                // Mantenemos orderBy de Firestore por eficiencia
                .orderBy("id")
                .get()
                .await()

            val questions = snapshot.documents.mapNotNull { document ->
                document.toObject(QuizQuestion::class.java)?.copy(id = document.id)
            }

            // Ordenamos localmente también para garantizar sincronización total
            // Esto arregla el problema de que a los alumnos les salgan en distinto orden
            val questionsSorted = questions.sortedBy { it.id }

            DataState.Success(questionsSorted)

        } catch (e: Exception) {
            DataState.Error("Error: ${e.message}")
        }
    }
}