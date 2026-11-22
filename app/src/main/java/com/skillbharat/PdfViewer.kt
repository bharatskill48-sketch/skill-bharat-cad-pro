
package com.skillbharat.cadpro

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CadLine(
    val start: Offset,
    val end: Offset
)

@Composable
fun PdfDrawingWorkspace(
    bitmap: Bitmap?,
    onClose: () -> Unit
) {
    var lines by remember { mutableStateOf(listOf<CadLine>()) }
    var currentLine by remember { mutableStateOf<CadLine?>(null) }
    var tool by remember { mutableStateOf("Line") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F4F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("PDF CAD DRAWING")
            TextButton(onClick = onClose) {
                Text("Close")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(onClick = { tool = "Line" }) {
                Text("Line")
            }

            Button(onClick = { tool = "Circle" }) {
                Text("Circle")
            }

            Button(onClick = {
                lines = emptyList()
                currentLine = null
            }) {
                Text("Clear")
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(tool, lines) {
                        detectDragGestures(
                            onDragStart = { point ->
                                currentLine = CadLine(point, point)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentLine = currentLine?.copy(
                                    end = change.position
                                )
                            },
                            onDragEnd = {
                                currentLine?.let {
                                    lines = lines + it
                                }
                                currentLine = null
                            }
                        )
                    }
            ) {
                bitmap?.let {
                    drawImage(
                        it.asImageBitmap(),
                        dstSize = androidx.compose.ui.unit.IntSize(
                            size.width.toInt(),
                            size.height.toInt()
                        )
                    )
                }

                lines.forEach { line ->
                    if (tool == "Circle") {
                        val radius = (line.end - line.start).getDistance()
                        drawCircle(
                            color = Color.Red,
                            radius = radius,
                            center = line.start,
                            style = Stroke(width = 3f)
                        )
                    } else {
                        drawLine(
                            color = Color.Red,
                            start = line.start,
                            end = line.end,
                            strokeWidth = 3f
                        )
                    }
                }

                currentLine?.let { line ->
                    drawLine(
                        color = Color.Blue,
                        start = line.start,
                        end = line.end,
                        strokeWidth = 3f
                    )
                }
            }
        }

        Text(
            "Tool: $tool | Draw on PDF with your finger",
            modifier = Modifier.padding(12.dp)
        )
    }
}
