
package com.skillbharat.cadpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CadShape(
    val type: String,
    val start: Offset,
    val end: Offset,
    val layer: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CadApp() }
    }
}

@Composable
fun CadApp() {
    var page by remember { mutableStateOf("Home") }

    MaterialTheme {
        Column(
            Modifier.fillMaxSize()
                .background(Color(0xFF101820))
        ) {
            Text(
                "SKILL BHARAT CAD PRO",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF073B78))
                    .padding(16.dp)
            )

            when (page) {
                "Home" -> HomeScreen { page = it }
                "Drawing" -> DrawingScreen { page = "Home" }
                "3D" -> ThreeDScreen()
                "Measurement" -> MeasurementScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(open: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Professional CAD Workspace",
            color = Color.White,
            fontSize = 24.sp
        )

        Button(
            onClick = { open("Drawing") },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("2D CAD Drawing")
        }

        Button(
            onClick = { open("3D") },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("3D Design")
        }

        Button(
            onClick = { open("Measurement") },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("Measurement Tools")
        }
    }
}

@Composable
fun DrawingScreen(back: () -> Unit) {
    var selected by remember { mutableStateOf("Line") }
    var shapes by remember { mutableStateOf(listOf<CadShape>()) }
    var redoShapes by remember { mutableStateOf(listOf<CadShape>()) }
    var start by remember { mutableStateOf<Offset?>(null) }
    var current by remember { mutableStateOf(Offset.Zero) }
    var showGrid by remember { mutableStateOf(true) }
    var snap by remember { mutableStateOf(false) }
    var layer by remember { mutableStateOf("Layer 1") }
    var layerMenu by remember { mutableStateOf(false) }

    val tools = listOf(
        "Line", "Circle", "Rectangle",
        "Polyline", "Arc", "Polygon"
    )

    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFF18232F))
    ) {
        Button(onClick = back) {
            Text("Back")
        }

        Row(
            Modifier.fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            tools.forEach { tool ->
                Button(
                    onClick = { selected = tool },
                    modifier = Modifier.padding(3.dp)
                ) {
                    Text(tool)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Button(onClick = {
                if (shapes.isNotEmpty()) {
                    redoShapes = redoShapes + shapes.last()
                    shapes = shapes.dropLast(1)
                }
            }) { Text("Undo") }

            Button(onClick = {
                if (redoShapes.isNotEmpty()) {
                    shapes = shapes + redoShapes.last()
                    redoShapes = redoShapes.dropLast(1)
                }
            }) { Text("Redo") }

            Button(onClick = {
                shapes = emptyList()
                redoShapes = emptyList()
            }) { Text("Clear") }

            Button(onClick = {
                showGrid = !showGrid
            }) {
                Text(if (showGrid) "Grid ON" else "Grid OFF")
            }

            Button(onClick = {
                snap = !snap
            }) {
                Text(if (snap) "Snap ON" else "Snap OFF")
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Layer:", color = Color.White)

            Button(onClick = { layerMenu = true }) {
                Text(layer)
            }

            DropdownMenu(
                expanded = layerMenu,
                onDismissRequest = { layerMenu = false }
            ) {
                listOf("Layer 1", "Layer 2", "Layer 3").forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            layer = it
                            layerMenu = false
                        }
                    )
                }
            }
        }

        // Drawing Canvas will be added in Part 2
    }
}

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0B1118))
                .pointerInput(selected, snap, layer) {
                    detectDragGestures(
                        onDragStart = { position ->
                            val point = if (snap) {
                                Offset(
                                    (position.x / 20).toInt() * 20f,
                                    (position.y / 20).toInt() * 20f
                                )
                            } else position
                            start = point
                            current = point
                        },
                        onDrag = { change, _ ->
                            current = change.position
                        },
                        onDragEnd = {
                            start?.let { first ->
                                shapes = shapes + CadShape(
                                    selected,
                                    first,
                                    current,
                                    layer
                                )
                                redoShapes = emptyList()
                            }
                            start = null
                        }
                    )
                }
        ) {
            if (showGrid) {
                val step = 40f
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        Color(0xFF263747),
                        Offset(x, 0f),
                        Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += step
                }

                var y = 0f
                while (y < size.height) {
                    drawLine(
                        Color(0xFF263747),
                        Offset(0f, y),
                        Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += step
                }
            }

            val allShapes = if (start != null) {
                shapes + CadShape(
                    selected,
                    start!!,
                    current,
                    layer
                )
            } else shapes

            allShapes.forEach { shape ->
                val a = shape.start
                val b = shape.end
                val color = when (shape.layer) {
                    "Layer 1" -> Color.Cyan
                    "Layer 2" -> Color.Yellow
                    else -> Color.Green
                }

                when (shape.type) {
                    "Line" -> drawLine(
                        color, a, b, strokeWidth = 3f
                    )

                    "Circle" -> {
                        val radius = (b - a).getDistance()
                        drawCircle(
                            color,
                            radius,
                            a,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(3f)
                        )
                    }

                    "Rectangle" -> {
                        drawRect(
                            color,
                            topLeft = Offset(
                                minOf(a.x, b.x),
                                minOf(a.y, b.y)
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                kotlin.math.abs(b.x - a.x),
                                kotlin.math.abs(b.y - a.y)
                            ),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(3f)
                        )
                    }

                    "Polyline" -> {
                        val middle = Offset(
                            (a.x + b.x) / 2,
                            minOf(a.y, b.y) - 35f
                        )
                        drawLine(color, a, middle, 3f)
                        drawLine(color, middle, b, 3f)
                    }

                    "Arc" -> {
                        drawArc(
                            color = color,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(
                                minOf(a.x, b.x),
                                minOf(a.y, b.y)
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                kotlin.math.abs(b.x - a.x),
                                kotlin.math.abs(b.y - a.y)
                            ),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(3f)
                        )
                    }

                    "Polygon" -> {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(a.x, a.y)
                            lineTo(b.x, a.y)
                            lineTo((a.x + b.x) / 2, b.y)
                            close()
                        }
                        drawPath(path, color, style =
                            androidx.compose.ui.graphics.drawscope.Stroke(3f)
                        )
                    }
                }
            }
        }

        Text(
            "Tool: $selected | Objects: ${shapes.size}",
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun ThreeDScreen() {
    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFF18232F))
            .padding(16.dp)
    ) {
        Text("3D Design Workspace", color = Color.White, fontSize = 22.sp)
        Spacer(Modifier.height(20.dp))
        Text(
            "3D modelling tools will be added in the next development stage.",
            color = Color.LightGray
        )
    }
}

@Composable
fun MeasurementScreen() {
    var length by remember { mutableStateOf("10") }
    var width by remember { mutableStateOf("10") }
    var unit by remember { mutableStateOf("Feet") }

    val area = (length.toDoubleOrNull() ?: 0.0) *
            (width.toDoubleOrNull() ?: 0.0)

    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFF18232F))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Measurement Tools", color = Color.White, fontSize = 22.sp)

        OutlinedTextField(
            value = length,
            onValueChange = { length = it },
            label = { Text("Length") }
        )

        OutlinedTextField(
            value = width,
            onValueChange = { width = it },
            label = { Text("Width") }
        )

        Text("Measurement Unit", color = Color.White)

        Row {
            listOf("Feet", "Inches", "CM").forEach {
                Button(onClick = { unit = it }) {
                    Text(it)
                }
            }
        }

        Text(
            "Area: ${"%.2f".format(area)} $unit²",
            color = Color.Green,
            fontSize = 20.sp
        )
    }
}
