
package com.skillbharat.cadpro

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ShapeItem(
    val type: String,
    val start: Offset,
    val end: Offset
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CadApp()
        }
    }
}

@Composable
fun CadApp() {
    var page by remember { mutableStateOf("Home") }

    MaterialTheme {
        Column(
            Modifier.fillMaxSize()
                .background(Color(0xFFF1F4F8))
        ) {
            Row(
                Modifier.fillMaxWidth()
                    .background(Color(0xFF073B78))
                    .padding(16.dp)
            ) {
                Text(
                    "SKILL BHARAT CAD PRO",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            when (page) {
                "Home" -> HomeScreen { page = it }
                "PDF" -> PdfScreen()
                "Drawing" -> DrawingScreen()
                "3D" -> ThreeDScreen()
                "Measurement" -> MeasurementScreen()
                "Projects" -> SimpleScreen("My Projects")
                "Settings" -> SimpleScreen("Settings")
            }

            Spacer(Modifier.weight(1f))

            Row(
                Modifier.fillMaxWidth()
                    .background(Color(0xFF052954))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "SKILL BHARAT EDUCATION",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun HomeScreen(open: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Welcome to CAD PRO", fontSize = 24.sp)
        Text("Select your workspace")

        listOf(
            "PDF" to "Open PDF Drawing",
            "Drawing" to "2D Drawing",
            "3D" to "3D Design",
            "Measurement" to "Measurement",
            "Projects" to "My Projects",
            "Settings" to "Settings"
        ).forEach { item ->
            Button(
                onClick = { open(item.first) },
                modifier = Modifier.fillMaxWidth()
                    .height(58.dp)
            ) {
                Text(item.second)
            }
        }
    }
}

@Composable
fun PdfScreen() {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var message by remember { mutableStateOf("Open a PDF file") }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver
                    .openFileDescriptor(uri, "r")
                    ?.use { descriptor ->
                        val renderer = PdfRenderer(descriptor)
                        val pdfPage = renderer.openPage(0)

                        val width = 1400
                        val height = (
                            width.toFloat() *
                                pdfPage.height / pdfPage.width
                        ).toInt()

                        val image = Bitmap.createBitmap(
                            width,
                            height,
                            Bitmap.Config.ARGB_8888
                        )

                        pdfPage.render(
                            image,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )

                        pdfPage.close()
                        renderer.close()
                        bitmap = image
                        zoom = 1f
                        pan = Offset.Zero
                        message = "PDF opened successfully"
                    }
            } catch (e: Exception) {
                message = "PDF open failed: ${e.message}"
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(10.dp)) {
        Button(onClick = {
            picker.launch(arrayOf("application/pdf"))
        }) {
            Text("Open PDF")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                zoom = (zoom - 0.2f).coerceAtLeast(0.4f)
            }) {
                Text("−")
            }

            Text(" ${(zoom * 100).toInt()}% ")

            Button(onClick = {
                zoom = (zoom + 0.2f).coerceAtMost(5f)
            }) {
                Text("+")
            }

            Button(onClick = {
                zoom = 1f
                pan = Offset.Zero
            }) {
                Text("Reset")
            }
        }

        Text(message)

        Box(
            Modifier.fillMaxSize()
                .background(Color.DarkGray)
                .pointerInput(Unit) {
                    detectTransformGestures {
                            _, panChange, zoomChange, _ ->
                        zoom = (zoom * zoomChange)
                            .coerceIn(0.4f, 5f)
                        pan += panChange
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let { image ->
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "PDF page",
                    modifier = Modifier.fillMaxWidth()
                        .graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                            translationX = pan.x
                            translationY = pan.y
                        }
                )
            }
        }
    }
}

@Composable
fun DrawingScreen() {
    var selected by remember { mutableStateOf("Line") }
    var shapes by remember { mutableStateOf(listOf<ShapeItem>()) }
    var start by remember { mutableStateOf<Offset?>(null) }
    var end by remember { mutableStateOf(Offset.Zero) }

    Column(Modifier.fillMaxSize().padding(10.dp)) {
        Text("2D Drawing", fontSize = 22.sp)

        Row {
            Button(onClick = { selected = "Line" }) {
                Text("Line")
            }
            Button(onClick = { selected = "Circle" }) {
                Text("Circle")
            }
            Button(onClick = { selected = "Rectangle" }) {
                Text("Rectangle")
            }
        }

        Row {
            Button(onClick = {
                shapes = shapes.dropLast(1)
            }) {
                Text("Undo")
            }
            Button(onClick = {
                shapes = emptyList()
            }) {
                Text("Clear")
            }
        }

        Canvas(
            Modifier.fillMaxSize()
                .background(Color.White)
                .pointerInput(selected) {
                    detectDragGestures(
                        onDragStart = {
                            start = it
                            end = it
                        },
                        onDrag = { change, _ ->
                            end = change.position
                        },
                        onDragEnd = {
                            start?.let {
                                shapes = shapes + ShapeItem(
                                    selected, it, end
                                )
                            }
                            start = null
                        }
                    )
                }
        ) {
            shapes.forEach {
                when (it.type) {
                    "Line" -> drawLine(
                        Color.Black, it.start, it.end, 3f
                    )

                    "Rectangle" -> drawRect(
                        Color.Black,
                        topLeft = Offset(
                            minOf(it.start.x, it.end.x),
                            minOf(it.start.y, it.end.y)
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            kotlin.math.abs(it.end.x - it.start.x),
                            kotlin.math.abs(it.end.y - it.start.y)
                        ),
                        style = Stroke(3f)
                    )

                    "Circle" -> drawCircle(
                        Color.Black,
                        radius = kotlin.math.hypot(
                            it.end.x - it.start.x,
                            it.end.y - it.start.y
                        ),
                        center = it.start,
                        style = Stroke(3f)
                    )
                }
            }

            start?.let {
                when (selected) {
                    "Line" -> drawLine(Color.Red, it, end, 3f)
                    "Rectangle" -> drawRect(
                        Color.Red,
                        topLeft = Offset(
                            minOf(it.x, end.x),
                            minOf(it.y, end.y)
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            kotlin.math.abs(end.x - it.x),
                            kotlin.math.abs(end.y - it.y)
                        ),
                        style = Stroke(3f)
                    )
                    "Circle" -> drawCircle(
                        Color.Red,
                        radius = kotlin.math.hypot(
                            end.x - it.x, end.y - it.y
                        ),
                        center = it,
                        style = Stroke(3f)
                    )
                }
            }
        }
    }
}

@Composable
fun ThreeDScreen() {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("3D Design Preview", fontSize = 22.sp)

        Canvas(
            Modifier.fillMaxWidth().height(300.dp)
                .background(Color.White)
        ) {
            val a = Offset(size.width * .25f, size.height * .35f)
            val b = Offset(size.width * .65f, size.height * .35f)
            val c = Offset(size.width * .65f, size.height * .75f)
            val d = Offset(size.width * .25f, size.height * .75f)
            val shift = Offset(70f, -60f)

            drawLine(Color.Blue, a, b, 4f)
            drawLine(Color.Blue, b, c, 4f)
            drawLine(Color.Blue, c, d, 4f)
            drawLine(Color.Blue, d, a, 4f)

            drawLine(Color.Red, a, a + shift, 4f)
            drawLine(Color.Red, b, b + shift, 4f)
            drawLine(Color.Red, c, c + shift, 4f)
            drawLine(Color.Red, d, d + shift, 4f)

            drawLine(Color.Green, a + shift, b + shift, 4f)
            drawLine(Color.Green, b + shift, c + shift, 4f)
            drawLine(Color.Green, c + shift, d + shift, 4f)
            drawLine(Color.Green, d + shift, a + shift, 4f)
        }

        Text("Basic 3D Preview")
    }
}

@Composable
fun MeasurementScreen() {
    var length by remember { mutableStateOf("10") }
    var width by remember { mutableStateOf("10") }
    var unit by remember { mutableStateOf("Feet") }

    val l = length.toDoubleOrNull() ?: 0.0
    val w = width.toDoubleOrNull() ?: 0.0

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Measurement Calculator", fontSize = 22.sp)

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

        Row {
            listOf("Feet", "Inches", "CM").forEach {
                TextButton(onClick = { unit = it }) {
                    Text(it)
                }
            }
        }

        Text("Area: ${l * w} $unit²")
    }
}

@Composable
fun SimpleScreen(title: String) {
    Column(
        Modifier.fillMaxSize().padding(20.dp)
    ) {
        Text(title, fontSize = 24.sp)
        Text("Coming soon")
    }
}
