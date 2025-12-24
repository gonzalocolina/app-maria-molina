package com.example.mariamolina.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TeacherMenuViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _canAddQuestions = MutableStateFlow(false)
    val canAddQuestions: StateFlow<Boolean> = _canAddQuestions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado para errores de login
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Estado para saber si es usuario anónimo
    private val _isAnonymous = MutableStateFlow(auth.currentUser?.isAnonymous ?: true)
    val isAnonymous: StateFlow<Boolean> = _isAnonymous.asStateFlow()

    init {
        checkPermissions()
    }

    private fun checkPermissions() {
        val user = auth.currentUser
        _isAnonymous.value = user?.isAnonymous ?: true
        val email = user?.email
        if (user != null && !email.isNullOrBlank()) {
            // Buscamos en la colección 'permisos_profesor' el documento con el ID del email
            firestore.collection("permisos_profesor")
                .document(email)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.getBoolean("canAddQuestions") == true) {
                        _canAddQuestions.value = true
                    } else {
                        _canAddQuestions.value = false
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener {
                    // Si falla (sin internet, etc), asumimos false por seguridad
                    _canAddQuestions.value = false
                    _isLoading.value = false
                }
        } else {
            // No hay usuario logueado
            _canAddQuestions.value = false
            _isLoading.value = false
        }
    }

    // Login con Email
    fun signInWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        _loginError.value = null
        if (email.isBlank() || pass.isBlank()) {
            _loginError.value = "Rellena todos los campos"
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                checkPermissions() // Recargar permisos tras login
                onSuccess()
            }
            .addOnFailureListener {
                // Si falla, intentamos registrar
                registerWithEmail(email, pass, onSuccess)
            }
    }

    private fun registerWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                checkPermissions()
                onSuccess()
            }
            .addOnFailureListener { e ->
                _loginError.value = "Error: ${e.localizedMessage}"
            }
    }

    fun clearError() {
        _loginError.value = null
    }
}