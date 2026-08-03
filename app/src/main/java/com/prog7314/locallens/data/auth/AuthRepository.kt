package com.prog7314.locallens.data.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepository(
    private val context: Context,
    private val googleAuthClient: GoogleAuthClient = GoogleAuthClient(context)
) {
    val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return googleAuthClient.signIn()
    }

    suspend fun signOut() {
        googleAuthClient.signOut()
    }
}
