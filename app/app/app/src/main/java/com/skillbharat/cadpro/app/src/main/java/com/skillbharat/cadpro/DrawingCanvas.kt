
package com.skillbharat.cadpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun DrawingCanvas() {

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF182633))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.2f, 20f)
                    offset += pan
                }
            }
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {

            val step = 40f * scale

            for (x in 0..(size.width / step).toInt() + 1) {
                val px = x * step + offset.x % step

                drawLine(
                    Color(0xFF405566),
                    Offset(px, 0f),
                    Offset(px, size.height),
                    1f
                )
            }

            for (y in 0..(size.height / step).toInt() + 1) {
                val py = y * step + offset.y % step

                drawLine(
                    Color(0xFF405566),
                    Offset(0f, py),
                    Offset(size.width, py),
                    1f
                )
            }

            drawRect(
                color = Color(0xFFFFD166),
                topLeft = Offset(80f, 100f) * scale + offset,
                size = androidx.compose.ui.geometry.Size(
                    180f * scale,
                    120f * scale
                ),
                style = Stroke(3f)
            )
        }
    }
}
