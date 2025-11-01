package com.coffeehub.data.repository

import com.coffeehub.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = getUserData(firebaseUser.uid)
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun register(email: String, password: String, name: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    name = name,
                    isAdmin = false
                )
                // Save user data to Firestore
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Registration failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getUserData(userId: String): User {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            
            // Debug: Log raw Firestore data
            android.util.Log.d("AuthRepository", "Raw Firestore data: ${doc.data}")
            android.util.Log.d("AuthRepository", "isAdmin field: ${doc.get("isAdmin")}")
            android.util.Log.d("AuthRepository", "isAdmin type: ${doc.get("isAdmin")?.javaClass?.name}")
            
            val user = doc.toObject(User::class.java) ?: User(id = userId)
            android.util.Log.d("AuthRepository", "Parsed User: $user")
            user
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error getting user data", e)
            User(id = userId)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }
}
