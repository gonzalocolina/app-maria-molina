package com.edunova.mariamolina.data.service

import com.edunova.mariamolina.data.model.IdiomasSoportados
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio de traducción usando ML Kit.
 * Traduce texto de español a inglés, francés y alemán.
 * Utilizado para la traducción auntomática de las preguntas añadidas por el profesor
 * en español a los otros idiomas.
 */
@Singleton
class TranslatorService @Inject constructor(
) {
    
    // Caché de traductores para evitar recrearlos
    private val translators = mutableMapOf<String, Translator>()
    
    // Estado de descarga de modelos
    private val downloadedModels = mutableSetOf<String>()
    
    /**
     * Mapeo de códigos de idioma a códigos de ML Kit
     */
    private fun getMLKitLanguage(idioma: String): String {
        return when (idioma) {
            IdiomasSoportados.ESPANOL -> TranslateLanguage.SPANISH
            IdiomasSoportados.INGLES -> TranslateLanguage.ENGLISH
            IdiomasSoportados.FRANCES -> TranslateLanguage.FRENCH
            IdiomasSoportados.ALEMAN -> TranslateLanguage.GERMAN
            else -> TranslateLanguage.SPANISH
        }
    }
    
    /**
     * Obtiene o crea un traductor para el idioma destino especificado.
     */
    private fun getTranslator(targetLanguage: String): Translator {
        return translators.getOrPut(targetLanguage) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.SPANISH)
                .setTargetLanguage(getMLKitLanguage(targetLanguage))
                .build()
            Translation.getClient(options)
        }
    }
    
    /**
     * Descarga el modelo de traducción si no está disponible.
     */
    suspend fun downloadModelIfNeeded(targetLanguage: String): Result<Unit> {
        if (targetLanguage == IdiomasSoportados.ESPANOL) {
            return Result.success(Unit) // No necesita traducción
        }
        
        if (downloadedModels.contains(targetLanguage)) {
            return Result.success(Unit) // Ya descargado
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val translator = getTranslator(targetLanguage)
                val conditions = DownloadConditions.Builder()
                  //  .requireWifi() // Solo descarga con WiFi para no consumir datos móviles
                    .build()
                
                translator.downloadModelIfNeeded(conditions).await()
                downloadedModels.add(targetLanguage)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Descarga todos los modelos necesarios.
     */
    suspend fun downloadAllModels(): Result<Unit> {
        val idiomas = listOf(
            IdiomasSoportados.INGLES,
            IdiomasSoportados.FRANCES,
            IdiomasSoportados.ALEMAN
        )
        
        for (idioma in idiomas) {
            val result = downloadModelIfNeeded(idioma)
            if (result.isFailure) {
                return result
            }
        }
        return Result.success(Unit)
    }
    
    /**
     * Traduce un texto de español al idioma especificado.
     */
    suspend fun translate(text: String, targetLanguage: String): Result<String> {
        // Si el idioma destino es español, devolver el mismo texto
        if (targetLanguage == IdiomasSoportados.ESPANOL) {
            return Result.success(text)
        }
        
        // Si el texto está vacío, devolver vacío
        if (text.isBlank()) {
            return Result.success(text)
        }
        
        return withContext(Dispatchers.IO) {
            try {
                // Asegurarse de que el modelo está descargado
                downloadModelIfNeeded(targetLanguage).getOrThrow()
                
                val translator = getTranslator(targetLanguage)
                val translatedText = translator.translate(text).await()
                Result.success(translatedText)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Traduce un texto a todos los idiomas soportados.
     * Devuelve un mapa con el texto en cada idioma.
     */
    suspend fun translateToAllLanguages(spanishText: String): Result<Map<String, String>> {
        val translations = mutableMapOf<String, String>()
        
        // El texto original en español
        translations[IdiomasSoportados.ESPANOL] = spanishText
        
        // Traducir a los demás idiomas
        val targetLanguages = listOf(
            IdiomasSoportados.INGLES,
            IdiomasSoportados.FRANCES,
            IdiomasSoportados.ALEMAN
        )
        
        for (language in targetLanguages) {
            val result = translate(spanishText, language)
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Error traduciendo a $language"))
            }
            translations[language] = result.getOrThrow()
        }
        
        return Result.success(translations)
    }
    
    /**
     * Verifica si los modelos están descargados.
     */
    fun areModelsDownloaded(): Boolean {
        return downloadedModels.containsAll(
            listOf(
                IdiomasSoportados.INGLES,
                IdiomasSoportados.FRANCES,
                IdiomasSoportados.ALEMAN
            )
        )
    }
    
    /**
     * Limpia los recursos cuando ya no se necesitan.
     */
    fun close() {
        translators.values.forEach { it.close() }
        translators.clear()
    }
}
