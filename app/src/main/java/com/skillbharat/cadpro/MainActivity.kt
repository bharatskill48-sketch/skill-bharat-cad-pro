package com.skillbharat.cadpro

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.geometry.Offset

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CadApp()
            }
        }
    }
}
@Composable
fun CadApp() {
    var screen by remember { mutableStateOf("Home") }
    var pdfUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var cadName by remember { mutableStateOf("") }

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pdfUri = uri
            screen = "PDF"
        }
    }

    val cadPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            cadName = uri.lastPathSegment ?: "CAD File"
            screen = "CAD"
        }
    }

    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFF101820))
    ) {
        Text(
            "SKILL BHARAT CAD PRO",
            color = Color.White,
            fontSize = 21.sp,
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFF073B78))
                .padding(16.dp)
        )

        when (screen) {
            "Home" -> Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        pdfPicker.launch(arrayOf("application/pdf"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OPEN PDF FILE")
                }

                Button(
                    onClick = {
                        cadPicker.launch(arrayOf("*/*"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OPEN DWG / DXF FILE")
                }

                Button(
                    onClick = { screen = "Drawing" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("2D CAD DRAWING")
                }

                Button(
                    onClick = { screen = "Measure" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("MEASUREMENT TOOLS")
                }
            }

            "PDF" -> PdfViewer(pdfUri) {
                screen = "Home"
            }

            "CAD" -> CadFileScreen(cadName) {
                screen = "Home"
            }

            "Drawing" -> DrawingScreen {
                screen = "Home"
            }

            "Measure" -> MeasureScreen {
                screen = "Home"
            }
        }
    }
}
@Composable
fun CadFileScreen(
    fileName: String,
    back: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(fileName, color = Color.White)
        Spacer(Modifier.height(16.dp))
        Text(
            "DWG / DXF ফাইল নির্বাচন করা হয়েছে।",
            color = Color.White
        )
        Text(
            "CAD Engine এখনো যুক্ত হয়নি।",
            color = Color.Yellow
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = back) {
            Text("BACK")
        }
    }
}

@Composable
fun MeasureScreen(back: () -> Unit) {
    var unit by remember { mutableStateOf("CM") }
    var length by remember { mutableStateOf("100") }

    Column(
        Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("MEASUREMENT", color = Color.White)

        OutlinedTextField(
            value = length,
            onValueChange = { length = it },
            label = { Text("Length") }
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = unit == "CM",
                onClick = { unit = "CM" }
            )
            Text("CM", color = Color.White)

            RadioButton(
                selected = unit == "Feet/Inches",
                onClick = { unit = "Feet/Inches" }
            )
            Text("Feet/Inches", color = Color.White)
        }

        Text(
            "Unit: $unit",
            color = Color.Green
        )

        Button(onClick = back) {
            Text("BACK")
        }
    }
}
@Composable
fun PdfViewer(
    uri: android.net.Uri?,
    back: () -> Unit
) {
    val context = LocalContext.current
    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(uri) {
        if (uri != null) {
            try {
                pages = withContext(Dispatchers.IO) {
                    val result = mutableListOf<Bitmap>()
                    val descriptor =
                        context.contentResolver.openFileDescriptor(uri, "r")
                            ?: throw Exception("PDF খুলতে পারছি না")

                    val renderer = PdfRenderer(descriptor)
                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        val bitmap = Bitmap.createBitmap(
                            page.width.coerceAtMost(1600),
                            page.height.coerceAtMost(1600),
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(
                            bitmap, null, null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )
                        result.add(bitmap)
                        page.close()
                    }
                    renderer.close()
                    descriptor.close()
                    result
                }
            } catch (e: Exception) {
                error = e.message ?: "PDF খুলতে সমস্যা হয়েছে"
            }
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF202A35))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = back) { Text("BACK") }
            Text("PDF VIEWER", color = Color.White)
        }

        Text("Zoom: ${(zoom * 100).toInt()}%", color = Color.White)

        if (error.isNotEmpty()) {
            Text(error, color = Color.Red)
        }

        Box(
            Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, panChange, zoomChange, _ ->
                        zoom = (zoom * zoomChange).coerceIn(1f, 100f)
                        pan += panChange
                    }
                }
        ) {
            Column(
                Modifier.graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = pan.x
                    translationY = pan.y
                }
            ) {
                pages.forEach { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "PDF Page",
                        modifier = Modifier.fillMaxWidth()
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingScreen(back: () -> Unit) {
    var points by remember { mutableStateOf(listOf<Offset>()) }

    Column(Modifier.fillMaxSize().background(Color(0xFF202A35))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = back) { Text("BACK") }
            Text("2D CAD DRAWING", color = Color.White)
            Button(onClick = { points = emptyList() }) {
                Text("CLEAR")
            }
        }

        Text("স্ক্রিনে আঙুল দিয়ে আঁকুন", color = Color.White)

        Canvas(
            Modifier.fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, panChange, _, _ ->
                        points = points + centroid
                    }
                }
        ) {
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = Color.Blue,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 4f
                )
            }
        }
    }
}
