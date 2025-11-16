package com.example.webappdev.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTimerScreen(onBack: () -> Unit) {

    var topTime by remember { mutableStateOf(10 * 60) }
    var bottomTime by remember { mutableStateOf(10 * 60) }
    var activeTimer by remember { mutableStateOf<String?>(null) }

    // Timer countdown loop
    LaunchedEffect(activeTimer) {
        while (activeTimer != null) {
            delay(1000)
            when (activeTimer) {
                "top" -> if (topTime > 0) topTime--
                "bottom" -> if (bottomTime > 0) bottomTime--
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Timer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Rotated Top Timer
            TimerClock(
                time = topTime,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .rotate(180f),
                onClick = {
                    activeTimer = when (activeTimer) {
                        null -> "top"
                        "bottom" -> "top"
                        "top" -> "bottom"
                        else -> null
                    }
                }
            )

            // Control Panel
            ControlPanel(
                onStop = { activeTimer = null },
                onReset = {
                    activeTimer = null
                    topTime = 10 * 60
                    bottomTime = 10 * 60
                }
            )

            // Bottom Timer
            TimerClock(
                time = bottomTime,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                onClick = {
                    activeTimer = when (activeTimer) {
                        null -> "bottom"
                        "top" -> "bottom"
                        "bottom" -> "top"
                        else -> null
                    }
                }
            )
        }
    }
}

@Composable
fun TimerClock(time: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(Color(0xFF333333))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatTime(time),
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ControlPanel(onStop: () -> Unit, onReset: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onStop) {
            Text("Stop")
        }
        Button(onClick = onReset) {
            Text("Reset")
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
