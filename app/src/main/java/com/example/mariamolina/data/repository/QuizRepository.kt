package com.example.mariamolina.data.repository

import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.QuizQuestion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Definimos los posibles estados de carga
sealed class DataState<out T> {
    data class Success<out T>(val data: T) : DataState<T>()
    data class Error(val message: String) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
    object Idle : DataState<Nothing>()
}

@Singleton
class QuizRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_QUIZZES = "quizzes2"
        private const val QUIZ_SIZE = 10
    }
    
    // Función principal para obtener las preguntas de una dificultad específica
    suspend fun getQuestionsByDifficulty(dificultad: Dificultad): DataState<List<QuizQuestion>> {
        return try {
            val dificultadString = dificultad.name.uppercase()

            val snapshot = firestore.collection(COLLECTION_QUIZZES)
                .whereEqualTo("dificultad", dificultadString)
                .get()
                .await()

            val questions = snapshot.documents.mapNotNull { document ->
                document.toObject<QuizQuestion>()?.copy(id = document.id)
            }

            val allQuestions = questions.shuffled().take(QUIZ_SIZE)

            // 4. Devuelve el éxito con la lista de preguntas
            DataState.Success(allQuestions)

        } catch (e: Exception) {
            // 5. Devuelve el error si algo falla
            DataState.Error("Error al cargar las preguntas: ${e.message}")
        }
    }
}