package com.example.webappdev.engine.main

class Mouse {
    var x: Int = 0
    var y: Int = 0
    var pressed: Boolean = false

    fun press(px: Int, py: Int) {
        x = px; y = py; pressed = true
    }

    fun move(px: Int, py: Int) {
        x = px; y = py
    }

    fun release(px: Int, py: Int) {
        x = px; y = py; pressed = false
    }
}
