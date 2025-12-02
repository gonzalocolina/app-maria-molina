package com.example.mariamolina.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rol del usuario en la partida activa.
 */
enum class GameRole {
    TEACHER,  // Profesor que creó la sala
    STUDENT   // Alumno que se unió
}

/**
 * Datos de la sesión de juego activa.
 */
data class ActiveGameData(
    val pin: String,
    val role: GameRole,
    val nickname: String? = null  // Solo para estudiantes
)

/**
 * Gestiona la persistencia de la sesión de juego activa.
 * Permite reconectar al usuario si sale de la app durante una partida.
 */
@Singleton
class ActiveGameSession @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "active_game_session"
        private const val KEY_PIN = "game_pin"
        private const val KEY_ROLE = "game_role"
        private const val KEY_NICKNAME = "game_nickname"
        private const val KEY_IS_ACTIVE = "is_active"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Guarda la sesión de juego activa.
     */
    fun saveSession(pin: String, role: GameRole, nickname: String? = null) {
        prefs.edit().apply {
            putString(KEY_PIN, pin)
            putString(KEY_ROLE, role.name)
            putString(KEY_NICKNAME, nickname)
            putBoolean(KEY_IS_ACTIVE, true)
            apply()
        }
    }

    /**
     * Obtiene la sesión activa si existe.
     */
    fun getActiveSession(): ActiveGameData? {
        if (!prefs.getBoolean(KEY_IS_ACTIVE, false)) {
            return null
        }

        val pin = prefs.getString(KEY_PIN, null) ?: return null
        val roleStr = prefs.getString(KEY_ROLE, null) ?: return null
        val role = try { 
            GameRole.valueOf(roleStr) 
        } catch (e: Exception) { 
            return null 
        }
        val nickname = prefs.getString(KEY_NICKNAME, null)

        return ActiveGameData(pin = pin, role = role, nickname = nickname)
    }

    /**
     * Verifica si hay una sesión activa.
     */
    fun hasActiveSession(): Boolean {
        return prefs.getBoolean(KEY_IS_ACTIVE, false) && 
               prefs.getString(KEY_PIN, null) != null
    }

    /**
     * Limpia la sesión activa (cuando termina la partida).
     */
    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_PIN)
            remove(KEY_ROLE)
            remove(KEY_NICKNAME)
            putBoolean(KEY_IS_ACTIVE, false)
            apply()
        }
    }
}
