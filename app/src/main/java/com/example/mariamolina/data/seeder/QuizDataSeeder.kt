package com.example.mariamolina.data.seeder

import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.OpcionRespuesta
import com.example.mariamolina.data.model.QuizQuestion
import com.google.firebase.firestore.FirebaseFirestore

object QuizDataSeeder {

    private val firestore = FirebaseFirestore.getInstance()

    // --- LISTA DE PREGUNTAS (AÑADE AQUÍ TUS DATOS) ---
    private val preguntasParaSubir = listOf(

        // 1. PREGUNTA FÁCIL
        QuizQuestion(
            id = "q_facil_1",
            dificultad = Dificultad.FACIL.name,
            pregunta = mapOf(
                "es" to "¿Cómo se llamaba la reina protagonista?",
                "en" to "What was the name of the protagonist queen?",
                "fr" to "Comment s'appelait la reine protagoniste ?",
                "de" to "Wie hieß die Protagonistenkönigin?"
            ),
            opciones = listOf(
                OpcionRespuesta(
                    texto = mapOf("es" to "María de Molina", "en" to "Maria de Molina", "fr" to "Maria de Molina", "de" to "Maria de Molina"),
                    esCorrecta = true
                ),
                OpcionRespuesta(
                    texto = mapOf("es" to "Isabel la Católica", "en" to "Isabella I", "fr" to "Isabelle la Catholique", "de" to "Isabella die Katholische"),
                    esCorrecta = false
                ),
                OpcionRespuesta(
                    texto = mapOf("es" to "Juana la Loca", "en" to "Joanna the Mad", "fr" to "Jeanne la Folle", "de" to "Johanna die Wahnsinnige"),
                    esCorrecta = false
                )
            )
        ),

        // 2. PREGUNTA FÁCIL
        QuizQuestion(
            id = "q_facil_2",
            dificultad = Dificultad.FACIL.name,
            pregunta = mapOf(
                "es" to "¿Dónde nació María de Molina?",
                "en" to "Where was Maria de Molina born?",
                "fr" to "Où est née Maria de Molina ?",
                "de" to "Wo wurde Maria de Molina geboren?"
            ),
            opciones = listOf(
                OpcionRespuesta(
                    texto = mapOf("es" to "En Burgos", "en" to "In Burgos", "fr" to "À Burgos", "de" to "In Burgos"),
                    esCorrecta = false
                ),
                OpcionRespuesta(
                    texto = mapOf("es" to "En Cigales", "en" to "In Cigales", "fr" to "À Cigales", "de" to "In Cigales"),
                    esCorrecta = true
                ),
                OpcionRespuesta(
                    texto = mapOf("es" to "En Madrid", "en" to "In Madrid", "fr" to "À Madrid", "de" to "In Madrid"),
                    esCorrecta = false
                )
            )
        ),

        // 3. PREGUNTA MEDIA
        QuizQuestion(
            id = "q_media_1",
            dificultad = Dificultad.MEDIA.name,
            pregunta = mapOf(
                "es" to "¿Con qué rey se casó María de Molina?",
                "en" to "Which king did Maria de Molina marry?",
                "fr" to "Quel roi Maria de Molina a-t-elle épousé ?",
                "de" to "Welchen König hat Maria de Molina geheiratet?"
            ),
            opciones = listOf(
                OpcionRespuesta(
                    texto = mapOf("es" to "Sancho IV", "en" to "Sancho IV", "fr" to "Sancho IV", "de" to "Sancho IV"),
                    esCorrecta = true
                ),
                OpcionRespuesta(
                    texto = mapOf("es" to "Alfonso X", "en" to "Alfonso X", "fr" to "Alphonse X", "de" to "Alfons X."),
                    esCorrecta = false
                ),
                OpcionRespuesta(
                    texto = mapOf("es" to "Fernando III", "en" to "Ferdinand III", "fr" to "Ferdinand III", "de" to "Ferdinand III."),
                    esCorrecta = false
                )
            )
        )

        // ... AÑADE MÁS PREGUNTAS COPIANDO Y PEGANDO LOS BLOQUES ANTERIORES ...
    )

    // Función para subir los datos (Batch Write para ahorrar recursos)
    fun uploadQuestions(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val batch = firestore.batch()
        val collection = firestore.collection("quizzesV2")

        preguntasParaSubir.forEach { pregunta ->
            // Usamos el ID manual (ej: q_facil_1) como nombre del documento
            val docRef = collection.document(pregunta.id)
            batch.set(docRef, pregunta)
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error desconocido") }
    }
}