package com.example.mariamolina.data.repository

import com.example.mariamolina.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Repositorio centralizado para gestionar partidas multijugador.
 * Usa Firestore para sincronización en tiempo real.
 */
@Singleton
class MultiplayerRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    companion object {
        private const val COLLECTION_PARTIDAS = "partidas"
        private const val COLLECTION_JUGADORES = "jugadores"
        private const val COLLECTION_RESPUESTAS = "respuestas"
        private const val COLLECTION_HISTORIAL = "historial_partidas"
        private const val COLLECTION_QUIZZES = "quizzes"
    }

    // ==================== AUTENTICACIÓN ====================
    
    /**
     * Obtiene el UID del usuario actual, creando sesión anónima si no existe.
     */
    suspend fun getCurrentUserUid(): String {
        var user = auth.currentUser
        if (user == null) {
            val result = auth.signInAnonymously().await()
            user = result.user
        }
        return user?.uid ?: throw Exception("Error de autenticación")
    }

    // ==================== CREAR PARTIDA (PROFESOR) ====================
    
    /**
     * Crea una nueva partida con un PIN aleatorio.
     * @param dificultad Dificultad seleccionada para las preguntas
     * @param totalPreguntas Número de preguntas en la partida
     * @return PIN de la partida creada
     */
    suspend fun createGame(dificultad: Dificultad, totalPreguntas: Int = 10): String {
        val hostUid = getCurrentUserUid()
        val pin = generateUniquePin()
        
        // Obtener IDs de preguntas aleatorias para esta dificultad
        val preguntasIds = getRandomQuestionIds(dificultad, totalPreguntas)
        
        val partida = Partida(
            pin = pin,
            estado = EstadoPartida.ESPERANDO,
            fase = GamePhase.LOBBY,
            preguntaActualIndex = 0,
            totalPreguntas = preguntasIds.size,
            dificultad = dificultad.name,
            preguntasIds = preguntasIds,
            createdAt = Timestamp.now(),
            hostUid = hostUid
        )
        
        firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .set(partida)
            .await()
        
        return pin
    }
    
    /**
     * Genera un PIN único de 4 dígitos que no exista ya.
     */
    private suspend fun generateUniquePin(): String {
        var attempts = 0
        while (attempts < 10) {
            val pin = Random.nextInt(1000, 9999).toString()
            val exists = firestore.collection(COLLECTION_PARTIDAS)
                .document(pin)
                .get()
                .await()
                .exists()
            
            if (!exists) return pin
            attempts++
        }
        // Si después de 10 intentos no encontramos uno único, usamos timestamp
        return (System.currentTimeMillis() % 10000).toString().padStart(4, '0')
    }
    
    /**
     * Obtiene IDs de preguntas aleatorias de una dificultad.
     */
    private suspend fun getRandomQuestionIds(dificultad: Dificultad, count: Int): List<String> {
        val snapshot = firestore.collection(COLLECTION_QUIZZES)
            .whereEqualTo("dificultad", dificultad.name)
            .get()
            .await()
        
        return snapshot.documents
            .map { it.id }
            .shuffled()
            .take(count)
    }
    
    /**
     * Actualiza la dificultad y las preguntas de una partida existente.
     * Solo se puede hacer mientras la partida está en estado ESPERANDO.
     */
    suspend fun updateGameDifficulty(pin: String, dificultad: Dificultad, totalPreguntas: Int = 10): List<QuizQuestion> {
        // Obtener nuevas preguntas para la dificultad seleccionada
        val preguntasIds = getRandomQuestionIds(dificultad, totalPreguntas)
        
        // Actualizar la partida en Firestore
        firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .update(
                mapOf(
                    "dificultad" to dificultad.name,
                    "preguntasIds" to preguntasIds,
                    "totalPreguntas" to preguntasIds.size
                )
            )
            .await()
        
        // Retornar las preguntas completas
        return preguntasIds.mapNotNull { id ->
            getQuestionById(id)
        }
    }

    // ==================== UNIRSE A PARTIDA (ALUMNO) ====================
    
    /**
     * Permite a un alumno unirse a una partida existente.
     * @param pin PIN de la partida
     * @param nickname Nombre del jugador
     */
    suspend fun joinGame(pin: String, nickname: String): Result<Unit> {
        return try {
            val uid = getCurrentUserUid()
            
            // Verificar que la partida existe y está en estado ESPERANDO
            val partidaDoc = firestore.collection(COLLECTION_PARTIDAS)
                .document(pin)
                .get()
                .await()
            
            if (!partidaDoc.exists()) {
                return Result.failure(Exception("No existe una partida con el PIN $pin"))
            }
            
            val partida = partidaDoc.toObject(Partida::class.java)
            if (partida?.estado != EstadoPartida.ESPERANDO) {
                return Result.failure(Exception("La partida ya ha comenzado"))
            }
            
            // Añadir jugador a la subcolección
            val jugador = Jugador(
                uid = uid,
                nickname = nickname,
                puntuacion = 0,
                hasAnswered = false
            )
            
            firestore.collection(COLLECTION_PARTIDAS)
                .document(pin)
                .collection(COLLECTION_JUGADORES)
                .document(uid)
                .set(jugador)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== OBSERVAR PARTIDA (TIEMPO REAL) ====================
    
    /**
     * Observa cambios en una partida en tiempo real.
     */
    fun observeGame(pin: String): Flow<Partida?> = callbackFlow {
        val listener = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val partida = snapshot?.toObject(Partida::class.java)
                trySend(partida)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Observa la lista de jugadores en tiempo real.
     */
    fun observePlayers(pin: String): Flow<List<Jugador>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .collection(COLLECTION_JUGADORES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val jugadores = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Jugador::class.java)
                } ?: emptyList()
                
                trySend(jugadores)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Observa cuántos jugadores han respondido la pregunta actual.
     */
    fun observeAnsweredCount(pin: String): Flow<Int> = callbackFlow {
        val listener = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .collection(COLLECTION_JUGADORES)
            .whereEqualTo("hasAnswered", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                trySend(snapshot?.size() ?: 0)
            }
        
        awaitClose { listener.remove() }
    }

    // ==================== CONTROL DEL JUEGO (PROFESOR) ====================
    
    /**
     * Inicia la partida (cambia estado a JUGANDO y fase a SHOWING_QUESTION).
     */
    suspend fun startGame(pin: String) {
        firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .update(
                mapOf(
                    "estado" to EstadoPartida.JUGANDO.name,
                    "fase" to GamePhase.SHOWING_QUESTION.name,
                    "preguntaActualIndex" to 0
                )
            )
            .await()
        
        // Resetear hasAnswered de todos los jugadores
        resetPlayersAnsweredStatus(pin)
    }
    
    /**
     * Avanza a la siguiente pregunta.
     */
    suspend fun advanceToNextQuestion(pin: String) {
        val partidaDoc = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .get()
            .await()
        
        val partida = partidaDoc.toObject(Partida::class.java) ?: return
        val nextIndex = partida.preguntaActualIndex + 1
        
        if (nextIndex >= partida.totalPreguntas) {
            // Fin del juego
            finishGame(pin)
        } else {
            // Siguiente pregunta
            firestore.collection(COLLECTION_PARTIDAS)
                .document(pin)
                .update(
                    mapOf(
                        "preguntaActualIndex" to nextIndex,
                        "fase" to GamePhase.SHOWING_QUESTION.name
                    )
                )
                .await()
            
            // Resetear hasAnswered de todos los jugadores
            resetPlayersAnsweredStatus(pin)
        }
    }
    
    /**
     * Cambia la fase a WAITING_FOR_NEXT (todos respondieron).
     */
    suspend fun setWaitingForNext(pin: String) {
        firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .update("fase", GamePhase.WAITING_FOR_NEXT.name)
            .await()
    }
    
    /**
     * Finaliza la partida y guarda en historial.
     */
    suspend fun finishGame(pin: String) {
        // Cambiar estado
        firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .update(
                mapOf(
                    "estado" to EstadoPartida.FINALIZADO.name,
                    "fase" to GamePhase.FINISHED.name
                )
            )
            .await()
        
        // Guardar en historial
        saveGameHistory(pin)
    }
    
    /**
     * Resetea el estado hasAnswered de todos los jugadores.
     */
    private suspend fun resetPlayersAnsweredStatus(pin: String) {
        val jugadoresSnapshot = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .collection(COLLECTION_JUGADORES)
            .get()
            .await()
        
        val batch = firestore.batch()
        jugadoresSnapshot.documents.forEach { doc ->
            batch.update(doc.reference, "hasAnswered", false)
        }
        batch.commit().await()
    }

    // ==================== RESPUESTAS (ALUMNO) ====================
    
    /**
     * Envía la respuesta de un jugador.
     */
    suspend fun submitAnswer(
        pin: String,
        preguntaIndex: Int,
        opcionSeleccionada: Int,
        esCorrecta: Boolean,
        tiempoRespuestaMs: Long,
        puntosObtenidos: Int
    ) {
        val uid = getCurrentUserUid()
        
        // Guardar respuesta
        val respuesta = RespuestaJugador(
            preguntaIndex = preguntaIndex,
            opcionSeleccionada = opcionSeleccionada,
            esCorrecta = esCorrecta,
            tiempoRespuestaMs = tiempoRespuestaMs,
            puntosObtenidos = puntosObtenidos,
            timestamp = Timestamp.now()
        )
        
        firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .collection(COLLECTION_JUGADORES)
            .document(uid)
            .collection(COLLECTION_RESPUESTAS)
            .document(preguntaIndex.toString())
            .set(respuesta)
            .await()
        
        // Actualizar puntuación del jugador y marcar que respondió
        val jugadorRef = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .collection(COLLECTION_JUGADORES)
            .document(uid)
        
        firestore.runTransaction { transaction ->
            val jugadorDoc = transaction.get(jugadorRef)
            val puntuacionActual = jugadorDoc.getLong("puntuacion")?.toInt() ?: 0
            
            transaction.update(jugadorRef, mapOf(
                "puntuacion" to puntuacionActual + puntosObtenidos,
                "hasAnswered" to true
            ))
        }.await()
    }
    
    /**
     * Verifica si el jugador actual ya respondió la pregunta.
     */
    suspend fun hasPlayerAnswered(pin: String, preguntaIndex: Int): Boolean {
        val uid = getCurrentUserUid()
        
        val respuestaDoc = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .collection(COLLECTION_JUGADORES)
            .document(uid)
            .collection(COLLECTION_RESPUESTAS)
            .document(preguntaIndex.toString())
            .get()
            .await()
        
        return respuestaDoc.exists()
    }

    // ==================== PREGUNTAS ====================
    
    /**
     * Obtiene una pregunta por su ID.
     */
    suspend fun getQuestionById(questionId: String): QuizQuestion? {
        val doc = firestore.collection(COLLECTION_QUIZZES)
            .document(questionId)
            .get()
            .await()
        
        return doc.toObject(QuizQuestion::class.java)?.copy(id = doc.id)
    }
    
    /**
     * Obtiene todas las preguntas de una partida.
     */
    suspend fun getQuestionsForGame(pin: String): List<QuizQuestion> {
        val partidaDoc = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .get()
            .await()
        
        val partida = partidaDoc.toObject(Partida::class.java) ?: return emptyList()
        
        return partida.preguntasIds.mapNotNull { id ->
            getQuestionById(id)
        }
    }

    // ==================== RANKING ====================
    
    /**
     * Obtiene el ranking actual ordenado por puntuación.
     */
    suspend fun getRanking(pin: String): List<Jugador> {
        val snapshot = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .collection(COLLECTION_JUGADORES)
            .orderBy("puntuacion", Query.Direction.DESCENDING)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Jugador::class.java)
        }
    }
    
    /**
     * Observa el ranking en tiempo real.
     */
    fun observeRanking(pin: String): Flow<List<Jugador>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .collection(COLLECTION_JUGADORES)
            .orderBy("puntuacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val ranking = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Jugador::class.java)
                } ?: emptyList()
                
                trySend(ranking)
            }
        
        awaitClose { listener.remove() }
    }

    // ==================== HISTORIAL ====================
    
    /**
     * Guarda el historial de una partida finalizada.
     */
    private suspend fun saveGameHistory(pin: String) {
        val partidaDoc = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .get()
            .await()
        
        val partida = partidaDoc.toObject(Partida::class.java) ?: return
        
        // Obtener jugadores con ranking
        val jugadores = getRanking(pin)
        
        val jugadoresHistorial = jugadores.mapIndexed { index, jugador ->
            // Contar respuestas correctas
            val respuestasSnapshot = firestore.collection(COLLECTION_PARTIDAS)
                .document(pin)
                .collection(COLLECTION_JUGADORES)
                .document(jugador.uid)
                .collection(COLLECTION_RESPUESTAS)
                .whereEqualTo("esCorrecta", true)
                .get()
                .await()
            
            JugadorHistorial(
                nickname = jugador.nickname,
                puntuacionFinal = jugador.puntuacion,
                posicionFinal = index + 1,
                respuestasCorrectas = respuestasSnapshot.size()
            )
        }
        
        val duracion = partida.createdAt?.let {
            (Timestamp.now().seconds - it.seconds)
        } ?: 0
        
        val historial = HistorialPartida(
            id = pin,
            pin = pin,
            dificultad = partida.dificultad,
            totalPreguntas = partida.totalPreguntas,
            fechaPartida = partida.createdAt,
            duracionSegundos = duracion,
            jugadores = jugadoresHistorial
        )
        
        firestore.collection(COLLECTION_HISTORIAL)
            .document(pin)
            .set(historial)
            .await()
    }
    
    /**
     * Obtiene el historial de partidas.
     */
    suspend fun getGameHistory(limit: Int = 20): List<HistorialPartida> {
        val snapshot = firestore.collection(COLLECTION_HISTORIAL)
            .orderBy("fechaPartida", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(HistorialPartida::class.java)
        }
    }

    // ==================== LIMPIEZA ====================
    
    /**
     * Elimina una partida y todos sus datos.
     */
    suspend fun deleteGame(pin: String) {
        // Eliminar jugadores
        val jugadoresSnapshot = firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .collection(COLLECTION_JUGADORES)
            .get()
            .await()
        
        val batch = firestore.batch()
        jugadoresSnapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
        
        // Eliminar partida
        firestore.collection(COLLECTION_PARTIDAS)
            .document(pin)
            .delete()
            .await()
    }
}
