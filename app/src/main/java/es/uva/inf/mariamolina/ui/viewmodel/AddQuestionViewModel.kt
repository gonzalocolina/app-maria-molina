package es.uva.inf.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uva.inf.mariamolina.data.model.Dificultad
import es.uva.inf.mariamolina.data.model.IdiomasSoportados
import es.uva.inf.mariamolina.data.model.OpcionRespuesta
import es.uva.inf.mariamolina.data.model.QuizQuestion
import es.uva.inf.mariamolina.data.service.TranslatorService
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Estado de la UI para añadir preguntas.
 */
data class AddQuestionUiState(
    val pregunta: String = "",
    val opciones: List<String> = listOf("", "", ""),
    val opcionCorrectaIndex: Int = 0,
    val dificultad: Dificultad = Dificultad.FACIL,
    val isLoading: Boolean = false,
    val isTranslating: Boolean = false,
    val isDownloadingModels: Boolean = false,
    val modelsDownloaded: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val translationProgress: String = ""
)

/**
 * ViewModel para la pantalla de añadir preguntas.
 */
@HiltViewModel
class AddQuestionViewModel @Inject constructor(
    private val translatorService: TranslatorService,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddQuestionUiState())
    val uiState: StateFlow<AddQuestionUiState> = _uiState.asStateFlow()
    
    companion object {
        // Las nuevas preguntas se guardan en quizzes2 para no modificar la colección original
        private const val COLLECTION_QUIZZES = "quizzes2"
    }
    
    init {
        // Verificar si los modelos están descargados
        checkModelsStatus()
    }
    
    private fun checkModelsStatus() {
        _uiState.update { it.copy(modelsDownloaded = translatorService.areModelsDownloaded()) }
    }
    
    /**
     * Descarga los modelos de traducción necesarios.
     */
    fun downloadModels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingModels = true, error = null) }
            
            val result = translatorService.downloadAllModels()
            
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isDownloadingModels = false, 
                        modelsDownloaded = true,
                        successMessage = "Modelos de traducción descargados correctamente"
                    ) 
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isDownloadingModels = false,
                        error = "Error al descargar modelos: ${result.exceptionOrNull()?.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Actualiza el texto de la pregunta.
     */
    fun updatePregunta(text: String) {
        _uiState.update { it.copy(pregunta = text) }
    }
    
    /**
     * Actualiza el texto de una opción.
     */
    fun updateOpcion(index: Int, text: String) {
        val newOpciones = _uiState.value.opciones.toMutableList()
        if (index in newOpciones.indices) {
            newOpciones[index] = text
            _uiState.update { it.copy(opciones = newOpciones) }
        }
    }
    
    /**
     * Añade una nueva opción (máximo 6).
     */
    fun addOpcion() {
        val currentOpciones = _uiState.value.opciones
        if (currentOpciones.size < 6) {
            _uiState.update { it.copy(opciones = currentOpciones + "") }
        }
    }
    
    /**
     * Elimina una opción (mínimo 2).
     */
    fun removeOpcion(index: Int) {
        val currentOpciones = _uiState.value.opciones
        if (currentOpciones.size > 2 && index in currentOpciones.indices) {
            val newOpciones = currentOpciones.toMutableList().apply { removeAt(index) }
            // Ajustar el índice de la opción correcta si es necesario
            val newCorrectIndex = when {
                _uiState.value.opcionCorrectaIndex == index -> 0
                _uiState.value.opcionCorrectaIndex > index -> _uiState.value.opcionCorrectaIndex - 1
                else -> _uiState.value.opcionCorrectaIndex
            }
            _uiState.update { it.copy(opciones = newOpciones, opcionCorrectaIndex = newCorrectIndex) }
        }
    }
    
    /**
     * Establece la opción correcta.
     */
    fun setOpcionCorrecta(index: Int) {
        _uiState.update { it.copy(opcionCorrectaIndex = index) }
    }
    
    /**
     * Establece la dificultad.
     */
    fun setDificultad(dificultad: Dificultad) {
        _uiState.update { it.copy(dificultad = dificultad) }
    }
    
    /**
     * Limpia mensajes de error y éxito.
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
    
    /**
     * Valida los campos del formulario.
     */
    private fun validateForm(): String? {
        val state = _uiState.value
        
        if (state.pregunta.isBlank()) {
            return "La pregunta no puede estar vacía"
        }
        
        if (state.opciones.any { it.isBlank() }) {
            return "Todas las opciones deben tener texto"
        }
        
        if (state.opciones.distinct().size != state.opciones.size) {
            return "Las opciones no pueden repetirse"
        }
        
        return null
    }
    
    /**
     * Genera un ID único para la pregunta con formato "q_{dificultad}_{numero}".
     * Busca el último número usado para esa dificultad y lo incrementa.
     */
    private suspend fun generateQuestionId(dificultad: Dificultad): String {
        val dificultadLower = dificultad.name.lowercase()
        val prefix = "q_${dificultadLower}_"
        
        // Buscar todas las preguntas de esta dificultad para encontrar el mayor número
        val snapshot = firestore.collection(COLLECTION_QUIZZES)
            .whereEqualTo("dificultad", dificultad.name)
            .get()
            .await()
        
        // Extraer los números de los IDs existentes
        val existingNumbers = snapshot.documents.mapNotNull { doc ->
            val docId = doc.id
            if (docId.startsWith(prefix)) {
                docId.removePrefix(prefix).toIntOrNull()
            } else {
                null
            }
        }
        
        // Encontrar el siguiente número disponible
        val nextNumber = if (existingNumbers.isEmpty()) 1 else existingNumbers.max() + 1
        
        return "$prefix$nextNumber"
    }
    
    /**
     * Guarda la pregunta con traducciones automáticas.
     */
    fun saveQuestion() {
        // Validar formulario
        val validationError = validateForm()
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    isTranslating = true,
                    error = null,
                    translationProgress = "Traduciendo pregunta..."
                ) 
            }
            
            try {
                val state = _uiState.value
                
                // 1. Traducir la pregunta a todos los idiomas
                _uiState.update { it.copy(translationProgress = "Traduciendo pregunta...") }
                val preguntaMultilingue = translatorService.translateToAllLanguages(state.pregunta)
                    .getOrThrow()
                
                // 2. Traducir cada opción
                val opcionesMultilingue = mutableListOf<OpcionRespuesta>()
                state.opciones.forEachIndexed { index, opcionTexto ->
                    _uiState.update { 
                        it.copy(translationProgress = "Traduciendo opción ${index + 1} de ${state.opciones.size}...") 
                    }
                    
                    val textoMultilingue = translatorService.translateToAllLanguages(opcionTexto)
                        .getOrThrow()
                    
                    opcionesMultilingue.add(
                        OpcionRespuesta(
                            texto = opcionTexto, // Texto original en español para retrocompatibilidad
                            textoMultilingue = textoMultilingue,
                            esCorrecta = index == state.opcionCorrectaIndex
                        )
                    )
                }
                
                // 3. Generar ID único para la pregunta
                _uiState.update { it.copy(translationProgress = "Generando ID...") }
                val questionId = generateQuestionId(state.dificultad)
                
                // 4. Crear la pregunta con el ID generado
                _uiState.update { it.copy(translationProgress = "Guardando en base de datos...") }
                
                val question = QuizQuestion(
                    id = questionId,
                    pregunta = state.pregunta, // Texto original para retrocompatibilidad
                    preguntaMultilingue = preguntaMultilingue,
                    dificultad = state.dificultad.name,
                    opciones = opcionesMultilingue
                )
                
                // 5. Guardar en Firestore con el ID personalizado
                firestore.collection(COLLECTION_QUIZZES)
                    .document(questionId)
                    .set(question)
                    .await()
                
                // 6. Limpiar formulario y mostrar éxito
                _uiState.update { 
                    AddQuestionUiState(
                        modelsDownloaded = true,
                        successMessage = "¡Pregunta guardada correctamente! ID: $questionId"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isTranslating = false,
                        translationProgress = "",
                        error = "Error: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        translatorService.close()
    }
}
