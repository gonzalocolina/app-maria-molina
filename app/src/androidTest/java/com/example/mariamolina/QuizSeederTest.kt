package com.example.mariamolina

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mariamolina.data.seeder.QuizDataSeeder
import com.google.firebase.FirebaseApp
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class QuizSeederTest {

    @Test
    fun uploadQuestionsToFirebase() {
        // Creamos un bloqueo para esperar a que la operación asíncrona de Firebase termine
        val latch = CountDownLatch(1)

        // Inicializamos Firebase manualmente por si acaso no se ha iniciado aún
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        println(">>> INICIANDO SUBIDA DE PREGUNTAS A FIREBASE...")

        // Llamamos a tu objeto Seeder (que debe estar en 'main')
        QuizDataSeeder.uploadQuestions(
            onSuccess = {
                println(">>> ¡ÉXITO! Todas las preguntas se han subido correctamente a la colección 'quizzes'.")
                latch.countDown() // Liberamos el test para que termine en verde
            },
            onError = { error ->
                println(">>> ERROR FATAL AL SUBIR: $error")
                latch.countDown() // Liberamos el test aunque falle
                throw RuntimeException(error) // Hacemos que el test falle en rojo en Android Studio
            }
        )

        // Esperamos hasta 10 segundos a que Firebase responda.
        // Si tarda más, el test fallará por timeout.
        val completed = latch.await(10, TimeUnit.SECONDS)
        if (!completed) {
            throw RuntimeException("Timeout: Firebase tardó demasiado en responder.")
        }
    }
}