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
    // Función principal para obtener las preguntas de una dificultad específica
    suspend fun getQuestionsByDifficulty(dificultad: Dificultad): DataState<List<QuizQuestion>> {
        return try {
            val dificultadString = dificultad.name.uppercase()

            // 1. Consulta a Firestore
            val snapshot = firestore.collection("quizzes")
                .whereEqualTo("dificultad", dificultadString) // Filtra por dificultad
                .get()
                .await() // Espera a que la tarea de Firebase termine

            // 2. Mapea los documentos a objetos QuizQuestion
            val questions = snapshot.documents.mapNotNull { document ->
                // Firestore no incluye el ID del documento en el objeto, lo añadimos
                document.toObject(QuizQuestion::class.java)?.copy(id = document.id)
            }

            // 3. Devuelve el éxito con la lista de preguntas
            DataState.Success(questions)

        } catch (e: Exception) {
            // 4. Devuelve el error si algo falla
            DataState.Error("Error al cargar las preguntas: ${e.message}")
        }
    }
}