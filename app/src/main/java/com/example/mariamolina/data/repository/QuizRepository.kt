package com.example.mariamolina.data.repository

import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.QuizQuestion
import com.google.firebase.firestore.FirebaseFirestore
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
        private const val COLLECTION_QUIZZES = "quizzes"
        private const val COLLECTION_QUIZZES_2 = "quizzes2"
    }
    
    // Función principal para obtener las preguntas de una dificultad específica
    // Combina preguntas de ambas colecciones (quizzes y quizzes2)
    suspend fun getQuestionsByDifficulty(dificultad: Dificultad): DataState<List<QuizQuestion>> {
        return try {
            val dificultadString = dificultad.name.uppercase()

            // 1. Consulta a la colección original (quizzes)
            val snapshot1 = firestore.collection(COLLECTION_QUIZZES)
                .whereEqualTo("dificultad", dificultadString)
                .get()
                .await()

            // 2. Consulta a la colección de preguntas nuevas (quizzes2)
            val snapshot2 = firestore.collection(COLLECTION_QUIZZES_2)
                .whereEqualTo("dificultad", dificultadString)
                .get()
                .await()

            // 3. Mapea los documentos de ambas colecciones a objetos QuizQuestion
            val questions1 = snapshot1.documents.mapNotNull { document ->
                document.toObject(QuizQuestion::class.java)?.copy(id = document.id)
            }
            
            val questions2 = snapshot2.documents.mapNotNull { document ->
                document.toObject(QuizQuestion::class.java)?.copy(id = document.id)
            }

            // 4. Combina y mezcla las preguntas de ambas colecciones
            val allQuestions = (questions1 + questions2).shuffled()

            // 5. Devuelve el éxito con la lista combinada de preguntas
            DataState.Success(allQuestions)

        } catch (e: Exception) {
            // 6. Devuelve el error si algo falla
            DataState.Error("Error al cargar las preguntas: ${e.message}")
        }
    }
}