package com.example.webappdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.webappdev.auth.AuthManager
import com.example.webappdev.model.User
import com.example.webappdev.ui.screens.GameScreen
import com.example.webappdev.ui.screens.HomeScreen
import com.example.webappdev.ui.screens.MatchTimerScreen
import com.example.webappdev.ui.theme.WebAppDevTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var authListener: FirebaseAuth.AuthStateListener? = null

    // Holding States
    private var firebaseUser by mutableStateOf(firebaseAuth.currentUser)

    // Navigation Flags
    private var isInGame by mutableStateOf(false)
    private var showTimer by mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initializing Google Sign-In
        AuthManager.init(this)

        // AuthListener
        authListener = FirebaseAuth.AuthStateListener {
            firebaseUser = it.currentUser
        }

        setContent {
            WebAppDevTheme {

                // Converting Firebase User to User model
                val user = firebaseUser?.let {
                    User(
                        name = it.displayName ?: "",
                        email = it.email ?: ""
                    )
                }

                // Screen-rendering Logic
                when {
                    showTimer -> {
                        MatchTimerScreen(
                            onBack = { showTimer = false }
                        )
                    }

                    isInGame && user != null -> {
                        GameScreen(
                            user = user,
                            onExitGame = { isInGame = false },
                            onRestart = {}
                        )
                    }

                    else -> {
                        HomeScreen(
                            user = user,
                            onStartGame = { isInGame = true },
                            onMatchTimer = { showTimer = true }
                        )
                    }
                    }
            }
        }
    }

    // Lifecycle Management

    override fun onStart() {
        super.onStart()
        authListener?.let { firebaseAuth.addAuthStateListener(it) }
    }

    override fun onResume() {
        super.onResume()
        // Re-initializing Auth
        AuthManager.init(this)
    }

    override fun onPause() {
        super.onPause()
        // Bypassing Cleanup
    }

    override fun onStop() {
        super.onStop()
        // Removing Listener to Avoid Data Leaks
        authListener?.let { firebaseAuth.removeAuthStateListener(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup
        authListener = null
    }
}
