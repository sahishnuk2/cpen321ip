package com.example.cpen321application.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cpen321application.Screen
import com.example.cpen321application.ui.theme.CPEN321ApplicationTheme

@Composable
fun MainPage(
    onNavigate: (Screen) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(
            100.dp,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainMenuButton("Login", onClick = {
            onNavigate(Screen.LOGIN)
        })
        MainMenuButton("Pixel Art", onClick = {
            onNavigate(Screen.PIXEL_ART)
        })
        MainMenuButton("Timer", onClick = {
            onNavigate(Screen.TIMER)
        })
    }

}

@Composable
private fun MainMenuButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(
            width = 200.dp,
            height = 75.dp
        ),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue,
            contentColor = Color.White
        )

    ) {
        Text(
            text = text,
            fontSize = 30.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainPagePreview() {
    CPEN321ApplicationTheme {
        MainPage()
    }
}

