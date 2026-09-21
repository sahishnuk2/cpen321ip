package com.example.cpen321application.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cpen321application.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

@Composable
fun PixelArtPage(onBackClick: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val pixels = remember {
        mutableStateListOf<Color>().apply {
            repeat(16 * 16) {
                add(Color.White)
            }
        }
    }

    var connectionStatus by remember { mutableStateOf("Connecting...") }
    var clearCanvasJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(BuildConfig.WEBSOCKET_URL)
            .build()

        val socket = client.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    coroutineScope.launch {
                        connectionStatus = "Connected"
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    coroutineScope.launch {
                        try {
                            val pixel = JSONObject(text)
                            val x = pixel.getInt("x")
                            val y = pixel.getInt("y")
                            val colorText = pixel.getString("color")

                            if (x in 0..15 && y in 0..15) {
                                val index = y * 16 + x
                                pixels[index] = Color(
                                    android.graphics.Color.parseColor(colorText)
                                )
                            }

                            clearCanvasJob?.cancel()
                            clearCanvasJob = coroutineScope.launch {
                                delay(4_000)
                                for (index in pixels.indices) {
                                    pixels[index] = Color.White
                                }
                            }
                        } catch (_: Exception) {
                            connectionStatus = "Invalid pixel received"
                        }
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    coroutineScope.launch {
                        connectionStatus = "Disconnected"
                    }
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    throwable: Throwable,
                    response: Response?
                ) {
                    coroutineScope.launch {
                        connectionStatus = "Connection failed: ${throwable.message ?: "Unknown error"}"
                    }
                }
            }
        )

        onDispose {
            clearCanvasJob?.cancel()
            socket.close(1000, "Pixel art page closed")
            client.dispatcher.executorService.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Live Pixel Art", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = connectionStatus)

        Spacer(modifier = Modifier.height(24.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(1.dp, Color.Black)
        ) {
            val cellWidth = size.width / 16
            val cellHeight = size.height / 16

            for (index in pixels.indices) {
                val x = index % 16
                val y = index / 16

                drawRect(
                    color = pixels[index],
                    topLeft = Offset(x * cellWidth, y * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }

            for (line in 0..16) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(line * cellWidth, 0f),
                    end = Offset(line * cellWidth, size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, line * cellHeight),
                    end = Offset(size.width, line * cellHeight),
                    strokeWidth = 1f
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBackClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            )
        ) {
            Text("Back")
        }
    }
}
