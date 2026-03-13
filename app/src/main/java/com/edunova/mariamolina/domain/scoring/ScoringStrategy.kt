package com.edunova.mariamolina.domain.scoring

/**
 * Patrón Strategy para diferentes modos de puntuación.
 * Permite intercambiar fácilmente la lógica de cálculo de puntos.
 */
interface ScoringStrategy {
    /**
     * Calcula los puntos obtenidos por una respuesta.
     * 
     * @param isCorrect Si la respuesta es correcta
     * @param timeRemainingMs Tiempo restante en milisegundos cuando se respondió
     * @param totalTimeMs Tiempo total disponible para la pregunta
     * @return Puntos obtenidos
     */
    fun calculateScore(isCorrect: Boolean, timeRemainingMs: Long, totalTimeMs: Long): Int
}

/**
 * Estrategia de puntuación:
 * - Respuesta correcta: puntos base + bonus por velocidad
 * - Respuesta incorrecta: 0 puntos
 * - Cuanto más rápido responda, más puntos bonus
 */
class KahootScoringStrategy(
    private val basePoints: Int = 1000
) : ScoringStrategy {
    
    override fun calculateScore(isCorrect: Boolean, timeRemainingMs: Long, totalTimeMs: Long): Int {
        if (!isCorrect) return 0
        if (totalTimeMs <= 0) return basePoints
        
        // Factor de tiempo: 1.0 si respondió instantáneamente, 0.0 si se acabó el tiempo
        val timeFactor = (timeRemainingMs.toFloat() / totalTimeMs.toFloat()).coerceIn(0f, 1f)
        
        // Puntuación = base * factor de tiempo (mínimo 100 puntos si acierta)
        return (basePoints * timeFactor).toInt().coerceAtLeast(100)
    }
}

