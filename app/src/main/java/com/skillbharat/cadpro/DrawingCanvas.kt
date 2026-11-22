package com.skillbharat.cadpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun DrawingCanvas() {
    var points by remember { mutableStateOf(listOf<Offset>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "SKILL BHARAT CAD PRO",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { points = points + it },
                        onDrag = { change, _ ->
                            points = points + change.position
                        }
                    )
                }
        ) {
            for (i in 1 until points.size) {
                drawLine(
                    color = Color.Blue,
                    start = points[i - 1],
                    end = points[i],
                    strokeWidth = 4f
                )
            }
        }
    }
}
