package com.example.webappdev.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.webappdev.auth.AuthManager
import com.example.webappdev.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navbar(user: User?, isInGame: Boolean) {

    val context = LocalContext.current
    val activity = context as? Activity

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = task.result
        AuthManager.handleSignInResult(account) {}
    }

    TopAppBar(
        title = {
            if (user == null) {
                Text("Chess App")
            } else {
                Column {
                    Text(user.name)
                    Text(user.email)
                }
            }
        },
        actions = {
            if (user == null) {
                Button(onClick = {
                    AuthManager.init(context)
                    AuthManager.getSignInIntent()?.let { launcher.launch(it) }
                }) {
                    Text("Sign In")
                }
            } else {
                Button(onClick = {
                    AuthManager.signOut()
                }) {
                    Text("Sign Out")
                }
            }
        }
    )
}
