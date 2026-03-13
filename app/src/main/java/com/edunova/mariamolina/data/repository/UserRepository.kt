package com.edunova.mariamolina.data.repository

import com.edunova.mariamolina.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    // Obtiene el usuario actual (crea uno anónimo si no existe)
    suspend fun getOrCreateUser(): Usuario {
        var firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            val authResult = auth.signInAnonymously().await()
            firebaseUser = authResult.user
        }

        if (firebaseUser == null) throw Exception("No se pudo autenticar")

        // Buscamos si ya tiene datos guardados en la colección "users"
        val snapshot = db.collection("users").document(firebaseUser.uid).get().await()

        return if (snapshot.exists()) {
            snapshot.toObject(Usuario::class.java) ?: Usuario(uid = firebaseUser.uid)
        } else {
            Usuario(uid = firebaseUser.uid)
        }
    }

}