package com.example.mariamolina.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona la persistencia de la autenticación del profesor en la sección infantil.
 * Permite que el profesor no tenga que volver a introducir la contraseña si navega
 * a otras secciones y regresa.
 */
@Singleton
class TeacherAuthSession @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "teacher_auth_session"
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Marca al profesor como autenticado.
     */
    fun setAuthenticated(authenticated: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_IS_AUTHENTICATED, authenticated)
            apply()
        }
    }

    /**
     * Verifica si el profesor está autenticado.
     */
    fun isAuthenticated(): Boolean {
        return prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
    }

    /**
     * Limpia la sesión de autenticación (logout).
     */
    fun clearAuthentication() {
        prefs.edit().apply {
            putBoolean(KEY_IS_AUTHENTICATED, false)
            apply()
        }
    }
}

