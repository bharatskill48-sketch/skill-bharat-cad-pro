package com.skillbharat.cadpro

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

private val AppBg = Color(0xFF101820)
private val Panel = Color(0xFF1B2833)
private val Accent = Color(0xFF36C5D8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { CadProApp() } }
    }
}

@Composable
private fun CadProApp() {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf("কোনো ফাইল খোলা হয়নি") }
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("PDF / DWG / DXF ফাইল খুলুন") }
    var isPdf by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            selectedName = uri.lastPathSegment?.substringAfterLast('/') ?: "Drawing"
            isPdf = selectedName.endsWith(".pdf", ignoreCase = true)
            status = if (isPdf) {
                "PDF নির্বাচিত হয়েছে"
            } else {
                "ফাইল নির্বাচিত হয়েছে। এই সংস্করণে DWG/DXF রেন্ডারার এখনো যুক্ত নয়।"
            }
        }
    }

    val menuItems = listOf("PDF Viewer", "DWG Viewer", "DXF Viewer", "CAD Drawing")
    val filtered = menuItems.filter { it.contains(query, ignoreCase = true) }

    Surface(Modifier.fillMaxSize(), color = AppBg) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Text(
                "SKILL BHARAT CAD PRO",
                color = Accent,
                fontSize = 22.sp,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search viewer / drawing type") }
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    filePicker.launch(
                        arrayOf(
                            "application/pdf",
                            "application/acad",
                            "image/vnd.dwg",
                            "image/vnd.dxf",
                            "*/*"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("OPEN PDF / DWG / DXF") }

            Text(selectedName, color = Color.White, fontSize = 13.sp)
            Text(status, color = Color.LightGray, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))

            Text("QUICK SEARCH", color = Accent)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtered.forEach { item ->
                    AssistChip(onClick = { query = item }, label = { Text(item) })
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPanel("LAYERS", "CAD engine যুক্ত হলে Layer তালিকা দেখাবে", Modifier.weight(1f))
                InfoPanel("UNITS", "Feet • Inches • CM", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text("DRAWING VIEW", color = Accent)
            if (selectedUri != null && isPdf) {
                PdfDocumentView(selectedUri!!, Modifier.weight(1f).fillMaxWidth())
            } else {
                Box(
                    Modifier.weight(1f).fillMaxWidth().background(Panel).padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (selectedUri == null)
                            "আপনার PDF / DWG / DXF ফাইল খুলুন"
                        else
                            "DWG/DXF ফাইলটি নেওয়া হয়েছে, কিন্তু আসল CAD rendering engine এই build-এ নেই।",
                        color = Color.White
                    )
                }
            }
            Text(
                "VIEWER ONLY • Editing tools are not included",
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun InfoPanel(title: String, detail: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Panel).padding(10.dp)) {
        Text(title, color = Accent, fontSize = 12.sp)
        Text(detail, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun PdfDocumentView(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pageBitmaps by remember(uri) { mutableStateOf<List<Bitmap>>(emptyList()) }
    var error by remember(uri) { mutableStateOf<String?>(null) }
    var zoom by remember(uri) { mutableFloatStateOf(1f) }
    var pageCount by remember(uri) { mutableIntStateOf(0) }

    LaunchedEffect(uri) {
        pageBitmaps = emptyList()
        error = null
        val result = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        pageCount = renderer.pageCount
                        val pages = ArrayList<Bitmap>()
                        for (index in 0 until renderer.pageCount) {
                            renderer.openPage(index).use { page ->
                                // Render at a larger base resolution for clearer text.
                                val maxDimension = 2200f
                                val scale = min(
                                    maxDimension / page.width.toFloat(),
                                    maxDimension / page.height.toFloat()
                                ).coerceAtLeast(1f)
                                val bitmap = Bitmap.createBitmap(
                                    (page.width * scale).toInt().coerceAtLeast(1),
                                    (page.height * scale).toInt().coerceAtLeast(1),
                                    Bitmap.Config.ARGB_8888
                                )
                                page.render(
                                    bitmap, null, null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                                pages.add(bitmap)
                            }
                        }
                        pages
                    }
                } ?: throw IllegalStateException("ফাইলটি পড়া যায়নি")
            } catch (e: Exception) {
                error = e.message ?: "PDF খোলা যায়নি"
                emptyList()
            }
        }
        pageBitmaps = result
    }

    Column(modifier.background(Panel).padding(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Pages: $pageCount", color = Color.White)
            Button(onClick = { zoom = (zoom / 1.25f).coerceAtLeast(1f) }) { Text("−") }
            Text("${(zoom * 100).toInt()}%", color = Color.White)
            Button(onClick = { zoom = (zoom * 1.25f).coerceAtMost(8f) }) { Text("+") }
            TextButton(onClick = { zoom = 1f }) { Text("Fit") }
        }
        when {
            error != null -> Text(error!!, color = Color.Red)
            pageBitmaps.isEmpty() -> Text("PDF loading…", color = Color.White)
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pageBitmaps) { bitmap ->
                    var gestureZoom by remember(bitmap) { mutableFloatStateOf(1f) }
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "PDF page",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp)
                            .pointerInput(bitmap) {
                                detectTransformGestures { _, _, scale, _ ->
                                    gestureZoom = (gestureZoom * scale).coerceIn(1f, 4f)
                                }
                            }
                            .then(
                                Modifier
                                    .widthIn(min = 1.dp)
                                    .heightIn(max = (bitmap.height * zoom * gestureZoom).dp)
                            )
                    )
                }
            }
        }
    }
}
