package com.example.webappdev.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

object AuthManager {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var googleClient: GoogleSignInClient? = null

    fun getCurrentUser() = firebaseAuth.currentUser

    fun init(activity: Context) {
        if (googleClient != null) return

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(activity.getString(com.example.webappdev.R.string.default_web_client_id))
            .build()

        googleClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getSignInIntent(): Intent? {
        return googleClient?.signInIntent
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleClient?.signOut()
    }

    fun handleSignInResult(account: GoogleSignInAccount?, onComplete: (Boolean) -> Unit) {
        if (account == null) {
            onComplete(false)
            return
        }

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}
