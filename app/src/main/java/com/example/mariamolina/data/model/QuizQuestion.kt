package com.example.mariamolina.data.model

import androidx.annotation.StringRes
import com.example.mariamolina.R

// Define los niveles de dificultad
enum class Dificultad {
    FACIL, MEDIA, DIFICIL
}

// Define una sola opción de respuesta
data class OpcionRespuesta(
    @StringRes val textoResId: Int,
    val esCorrecta: Boolean
)

// Define la pregunta completa
data class QuizQuestion(
    val id: String,
    @StringRes val preguntaResId: Int,
    val dificultad: Dificultad,
    val opciones: List<OpcionRespuesta>
)

// --- DATOS DE PRUEBA (MOCK DATA) ---
// Aquí creamos la lista de preguntas usando los IDs de strings.xml

val quizMockData = listOf(
    // Fáciles
    QuizQuestion(
        id = "qf1",
        preguntaResId = R.string.q_facil_1_pregunta,
        dificultad = Dificultad.FACIL,
        opciones = listOf(
            OpcionRespuesta(R.string.q_facil_1_r1_correcta, true),
            OpcionRespuesta(R.string.q_facil_1_r2, false),
            OpcionRespuesta(R.string.q_facil_1_r3, false)
        )
    ),
    QuizQuestion(
        id = "qf2",
        preguntaResId = R.string.q_facil_2_pregunta,
        dificultad = Dificultad.FACIL,
        opciones = listOf(
            OpcionRespuesta(R.string.q_facil_2_r1, false),
            OpcionRespuesta(R.string.q_facil_2_r2_correcta, true),
            OpcionRespuesta(R.string.q_facil_2_r3, false),
            OpcionRespuesta(R.string.q_facil_2_r4, false)
        )
    ),
    // Medias
    QuizQuestion(
        id = "qm1",
        preguntaResId = R.string.q_media_1_pregunta,
        dificultad = Dificultad.MEDIA,
        opciones = listOf(
            OpcionRespuesta(R.string.q_media_1_r1_correcta, true),
            OpcionRespuesta(R.string.q_media_1_r2, false),
            OpcionRespuesta(R.string.q_media_1_r3, false)
        )
    ),
    QuizQuestion(
        id = "qm2",
        preguntaResId = R.string.q_media_2_pregunta,
        dificultad = Dificultad.MEDIA,
        opciones = listOf(
            OpcionRespuesta(R.string.q_media_2_r1, false),
            OpcionRespuesta(R.string.q_media_2_r2, false),
            OpcionRespuesta(R.string.q_media_2_r3_correcta, true)
        )
    )
)