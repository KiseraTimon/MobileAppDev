package com.example.webappdev.model

// Logged-in User Data
data class User(
    val name: String,
    val email: String,
    var points: Int = 0
)
