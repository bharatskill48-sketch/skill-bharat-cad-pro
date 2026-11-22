
package com.skillbharat.cadpro

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF10213A)
private val Blue = Color(0xFF1769AA)
private val Background = Color(0xFFF2F5FA)

data class CadShape(
    val start: Offset,
    val end: Offset,
    val type: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CADHomeScreen()
            }
        }
    }
}

@Composable
fun CADHomeScreen() {
    val context = LocalContext.current

    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var pageNumber by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pdfUri = uri
            pageNumber = 0
            zoom = 1f
            error = ""
        }
    }

    LaunchedEffect(pdfUri, pageNumber) {
        val uri = pdfUri ?: return@LaunchedEffect
        loading = true
        error = ""

        try {
            val result = withContext(Dispatchers.IO) {
                val descriptor =
                    context.contentResolver.openFileDescriptor(uri, "r")
                        ?: throw Exception("PDF খুলতে সমস্যা হয়েছে")

                descriptor.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        if (pageNumber !in 0 until renderer.pageCount) {
                            throw Exception("পৃষ্ঠা পাওয়া যায়নি")
                        }

                        val page = renderer.openPage(pageNumber)
                        try {
                            val bitmap = Bitmap.createBitmap(
                                page.width * 2,
                                page.height * 2,
                                Bitmap.Config.ARGB_8888
                            )
                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                            Pair(bitmap, renderer.pageCount)
                        } finally {
                            page.close()
                        }
                    }
                }
            }

            pdfBitmap = result.first
            pageCount = result.second
        } catch (e: Exception) {
            error = e.message ?: "PDF খোলা যায়নি"
            pdfBitmap = null
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy)
                .padding(22.dp)
        ) {
            Text(
                "SKILL BHARAT",
                color = Color(0xFFFFC857),
                fontWeight = FontWeight.Bold
            )
            Text(
                "CAD PRO",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Professional Design Workspace",
                color = Color.LightGray
            )
        }

        if (pdfUri == null) {
            Text(
                "Welcome to CAD PRO",
                modifier = Modifier.padding(20.dp),
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Button(
                onClick = {
                    pdfPicker.launch(arrayOf("application/pdf"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue
                )
            ) {
                Text("📄 OPEN PDF FILE")
            }

            val tools = listOf(
                "2D Drawing" to "✏️",
                "3D Design" to "🏠",
                "Floor Plan" to "📐",
                "Measurement" to "📏",
                "Projects" to "📁",
                "Settings" to "⚙️"
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tools) { tool ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(tool.second, fontSize = 30.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                tool.first,
                                color = Navy,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            PdfEditor(
                bitmap = pdfBitmap,
                loading = loading,
                error = error,
                pageNumber = pageNumber,
                pageCount = pageCount,
                zoom = zoom,
                onZoomChange = { zoom = it },
                onPrevious = { pageNumber-- },
                onNext = { pageNumber++ },
                onClose = {
                    pdfUri = null
                    pdfBitmap = null
                    pageNumber = 0
                    pageCount = 0
                }
            )
        }
    }
}

@Composable
fun PdfEditor(
    bitmap: Bitmap?,
    loading: Boolean,
    error: String,
    pageNumber: Int,
    pageCount: Int,
    zoom: Float,
    onZoomChange: (Float) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    var selectedTool by remember { mutableStateOf("Line") }
    var shapes by remember { mutableStateOf(listOf<CadShape>()) }
    var currentShape by remember { mutableStateOf<CadShape?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PDF CAD EDITOR", fontWeight = FontWeight.Bold)
            TextButton(onClick = onClose) {
                Text("Close")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Line", "Circle", "Rectangle").forEach { tool ->
                Button(
                    onClick = { selectedTool = tool },
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (selectedTool == tool) Blue else Navy
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 10.dp,
                        vertical = 8.dp
                    )
                ) {
                    Text(tool)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = {
                shapes = shapes.dropLast(1)
            }) {
                Text("Undo")
            }

            OutlinedButton(onClick = {
                shapes = emptyList()
                currentShape = null
            }) {
                Text("Clear")
            }

            OutlinedButton(onClick = {
                onZoomChange((zoom - 0.25f).coerceAtLeast(0.5f))
            }) {
                Text("−")
            }

            OutlinedButton(onClick = {
                onZoomChange((zoom + 0.25f).coerceAtMost(3f))
            }) {
                Text("+")
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator()
                error.isNotEmpty() -> Text(error, color = Color.Red)
                bitmap != null -> {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedTool, shapes) {
                                detectDragGestures(
                                    onDragStart = { point ->
                                        currentShape = CadShape(
                                            point,
                                            point,
                                            selectedTool
                                        )
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentShape =
                                            currentShape?.copy(
                                                end = change.position
                                            )
                                    },
                                    onDragEnd = {
                                        currentShape?.let {
                                            shapes = shapes + it
                                        }
                                        currentShape = null
                                    }
                                )
                            }
                    ) {
                        val scale = minOf(
                            size.width / bitmap.width,
                            size.height / bitmap.height
                        )

                        val drawWidth = bitmap.width * scale
                        val drawHeight = bitmap.height * scale
                        val left = (size.width - drawWidth) / 2
                        val top = (size.height - drawHeight) / 2

                        val imageTopLeft = Offset(left, top)

                        drawImage(
                            bitmap.asImageBitmap(),
                            dstOffset = androidx.compose.ui.unit.IntOffset(
                                left.toInt(),
                                top.toInt()
                            ),
                            dstSize = androidx.compose.ui.unit.IntSize(
                                drawWidth.toInt(),
                                drawHeight.toInt()
                            )
                        )

                        fun drawCadShape(shape: CadShape) {
                            val start = imageTopLeft + shape.start
                            val end = imageTopLeft + shape.end

                            when (shape.type) {
                                "Circle" -> {
                                    val radius =
                                        (end - start).getDistance()
                                    drawCircle(
                                        Color.Red,
                                        radius,
                                        start,
                                        style = Stroke(3f)
                                    )
                                }
                                "Rectangle" -> {
                                    drawRect(
                                        Color.Red,
                                        topLeft = Offset(
                                            minOf(start.x, end.x),
                                            minOf(start.y, end.y)
                                        ),
                                        size = androidx.compose.ui.geometry.Size(
                                            kotlin.math.abs(end.x - start.x),
                                            kotlin.math.abs(end.y - start.y)
                                        ),
                                        style = Stroke(3f)
                                    )
                                }
                                else -> drawLine(
                                    Color.Red,
                                    start,
                                    end,
                                    strokeWidth = 3f
                                )
                            }
                        }

                        shapes.forEach { drawCadShape(it) }
                        currentShape?.let { drawCadShape(it) }
                    }
                }
            }
        }

        Text(
            "Zoom: ${(zoom * 100).toInt()}%  |  Page ${pageNumber + 1} / $pageCount",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Navy
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                enabled = pageNumber > 0 && !loading,
                onClick = onPrevious
            ) {
                Text("Previous")
            }

            Button(
                enabled = pageNumber < pageCount - 1 && !loading,
                onClick = onNext
            ) {
                Text("Next")
            }
        }
    }
}
