package com.skillbharat.cadpro

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    var screen by remember { mutableStateOf("HOME") }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
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

    when (screen) {
        "HOME" -> Column(
            Modifier.fillMaxSize()
                .background(Color(0xFF101820))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "SKILL BHARAT CAD PRO",
                color = Color.White,
                fontSize = 23.sp
            )

            Button(
                onClick = {
                    pdfPicker.launch(arrayOf("application/pdf"))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("OPEN PDF") }

            Button(
                onClick = {
                    cadPicker.launch(arrayOf("*/*"))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("OPEN DWG / DXF") }

            Button(
                onClick = { screen = "DRAW" },
                modifier = Modifier.fillMaxWidth()
            ) { Text("2D CAD WORKSPACE") }

            Button(
                onClick = { screen = "MEASURE" },
                modifier = Modifier.fillMaxWidth()
            ) { Text("MEASUREMENT") }
        }

        "PDF" -> PdfViewer(pdfUri) {
            screen = "HOME"
        }

        "CAD" -> CadFileScreen(cadName) {
            screen = "HOME"
        }

        "DRAW" -> DrawingScreen {
            screen = "HOME"
        }

        "MEASURE" -> MeasureScreen {
            screen = "HOME"
        }
    }
}
@Composable
fun DrawingScreen(back: () -> Unit) {
    var tool by remember { mutableStateOf("LINE") }
    var command by remember { mutableStateOf("") }
    var zoom by remember { mutableFloatStateOf(1f) }
    var unit by remember { mutableStateOf("CM") }

    val shapes = remember {
        mutableStateListOf<Pair<Offset, Offset>>()
    }

    var start by remember { mutableStateOf<Offset?>(null) }
    var end by remember { mutableStateOf<Offset?>(null) }

    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFF151B24))
    ) {
        Row(
            Modifier.fillMaxWidth()
                .background(Color(0xFF073B78))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SKILL BHARAT CAD PRO",
                color = Color.White,
                modifier = Modifier.weight(1f))
            TextButton(onClick = back) {
                Text("BACK", color = Color.White)
            }
        }

        Row(
            Modifier.horizontalScroll(rememberScrollState())
        ) {
            listOf("LINE", "CIRCLE", "RECT").forEach {
                Button(onClick = { tool = it }) {
                    Text(it)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(tool, color = Color.Green)
            Text("${(zoom * 100).toInt()}%",
                color = Color.White)
            TextButton(onClick = {
                if (shapes.isNotEmpty()) {
                    shapes.removeAt(shapes.lastIndex)
                }
            }) { Text("UNDO") }
        }

        Canvas(
            Modifier.weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF101820))
                .pointerInput(tool, zoom) {
                    detectDragGestures(
                        onDragStart = {
                            start = Offset(
                                it.x / zoom,
                                it.y / zoom
                            )
                        },
                        onDrag = { change, _ ->
                            end = Offset(
                                change.position.x / zoom,
                                change.position.y / zoom
                            )
                        },
                        onDragEnd = {
                            val a = start
                            val b = end
                            if (a != null && b != null) {
                                shapes.add(a to b)
                            }
                            start = null
                            end = null
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures {
                        _, _, z, _ ->
                        zoom = (zoom * z).coerceIn(0.1f, 100f)
                    }
                }
        ) {
            val step = 40f
            var x = 0f
            while (x < size.width) {
                drawLine(
                    Color(0xFF293746),
                    Offset(x, 0f),
                    Offset(x, size.height),
                    1f
                )
                x += step
            }

            var y = 0f
            while (y < size.height) {
                drawLine(
                    Color(0xFF293746),
                    Offset(0f, y),
                    Offset(size.width, y),
                    1f
                )
                y += step
            }

            shapes.forEach { (a, b) ->
                val p1 = Offset(a.x * zoom, a.y * zoom)
                val p2 = Offset(b.x * zoom, b.y * zoom)

                when (tool) {
                    "CIRCLE" -> drawCircle(
                        Color.Cyan,
                        radius = (p2 - p1).getDistance(),
                        center = p1,
                        style = Stroke(3f)
                    )
                    "RECT" -> drawRect(
                        Color.Cyan,
                        topLeft = p1,
                        size = androidx.compose.ui.geometry.Size(
                            p2.x - p1.x,
                            p2.y - p1.y
                        ),
                        style = Stroke(3f)
                    )
                    else -> drawLine(Color.Cyan, p1, p2, 3f)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth()
                .background(Color(0xFF263443))
                .padding(5.dp)
        ) {
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Command...") },
                singleLine = true
            )
            Button(onClick = {
                when (command.uppercase()) {
                    "LINE", "CIRCLE", "RECT" ->
                        tool = command.uppercase()
                    "UNDO" -> if (shapes.isNotEmpty()) {
                        shapes.removeAt(shapes.lastIndex)
                    }
                    "CLEAR" -> shapes.clear()
                    "ZOOM IN" -> zoom = (zoom * 1.5f).coerceAtMost(100f)
                    "ZOOM OUT" -> zoom = (zoom / 1.5f).coerceAtLeast(0.1f)
                }
                command = ""
            }) { Text("GO") }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("UNIT:", color = Color.White)
            listOf("CM", "Feet", "Inches").forEach {
                TextButton(onClick = { unit = it }) {
                    Text(
                        it,
                        color = if (unit == it) Color.Green
                        else Color.White
                    )
                }
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
        Modifier.fillMaxSize()
            .background(Color(0xFF151B24))
            .padding(16.dp)
    ) {
        Text("CAD FILE", color = Color.White)
        Text(fileName, color = Color.Cyan)
        Text(
            "DWG/DXF Viewer Engine এখনো যুক্ত হয়নি।",
            color = Color.Yellow
        )
        Button(onClick = back) {
            Text("BACK")
        }
    }
}

@Composable
fun MeasureScreen(back: () -> Unit) {
    var length by remember { mutableStateOf("100") }
    var unit by remember { mutableStateOf("CM") }

    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFF151B24))
            .padding(16.dp)
    ) {
        Text("MEASUREMENT", color = Color.White)

        OutlinedTextField(
            value = length,
            onValueChange = { length = it },
            label = { Text("Length") }
        )

        listOf("CM", "Feet", "Inches").forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = unit == it,
                    onClick = { unit = it }
                )
                Text(it, color = Color.White)
            }
        }

        Text("Selected Unit: $unit", color = Color.Green)

        Button(onClick = back) {
            Text("BACK")
        }
    }
}
@Composable
fun PdfViewer(
    uri: Uri?,
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
                        context.contentResolver
                            .openFileDescriptor(uri, "r")
                            ?: throw Exception("PDF খুলতে পারছি না")

                    val renderer = PdfRenderer(descriptor)

                    try {
                        for (i in 0 until renderer.pageCount) {
                            val page = renderer.openPage(i)
                            try {
                                val scale = minOf(
                                    1600f / page.width,
                                    1600f / page.height,
                                    2f
                                )
                                val bitmap = Bitmap.createBitmap(
                                    (page.width * scale).toInt(),
                                    (page.height * scale).toInt(),
                                    Bitmap.Config.ARGB_8888
                                )
                                page.render(
                                    bitmap,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                                result.add(bitmap)
                            } finally {
                                page.close()
                            }
                        }
                    } finally {
                        renderer.close()
                        descriptor.close()
                    }
                    result
                }
            } catch (e: Exception) {
                error = e.message ?: "PDF Error"
            }
        }
    }

    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFF151B24))
    ) {
        Row(
            Modifier.fillMaxWidth()
                .background(Color(0xFF073B78)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = back) {
                Text("BACK", color = Color.White)
            }
            Text("PDF VIEWER", color = Color.White)
        }

        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "ZOOM ${(zoom * 100).toInt()}%",
                color = Color.White
            )
            TextButton(onClick = {
                zoom = 1f
                pan = Offset.Zero
            }) {
                Text("RESET")
            }
        }

        if (error.isNotEmpty()) {
            Text(error, color = Color.Red)
        }

        Box(
            Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, movement, factor, _ ->
                        zoom = (zoom * factor).coerceIn(1f, 100f)
                        pan += movement
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
                pages.forEachIndexed { index, bitmap ->
                    Text(
                        "PAGE ${index + 1}",
                        color = Color.Cyan
                    )
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "PDF Page ${index + 1}",
                        modifier = Modifier.fillMaxWidth()
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
