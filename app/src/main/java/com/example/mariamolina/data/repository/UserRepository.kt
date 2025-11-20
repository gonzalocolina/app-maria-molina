package com.example.mariamolina.data.repository

import com.example.mariamolina.data.model.Usuario
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

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

    // Guarda el nickname
    suspend fun saveNickname(nickname: String) {
        val firebaseUser = auth.currentUser ?: throw Exception("Usuario no logueado")

        val usuario = Usuario(uid = firebaseUser.uid, nickname = nickname)
        db.collection("users").document(firebaseUser.uid).set(usuario).await()
    }
}