package com.example.cpen321application.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TimerPage(onBackClick: () -> Unit) {
    Text("Timer Page")

    Button(onClick = onBackClick) {
        Text("Back")
    }
}