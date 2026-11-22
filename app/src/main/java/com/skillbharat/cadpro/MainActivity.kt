
package com.skillbharat.cadpro

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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
                Text("OPEN PDF FILE")
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
                        ),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(tool.second, fontSize = 30.sp)
                            Spacer(modifier = Modifier.height(8.dp))
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PDF Viewer",
                    color = Navy,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = {
                    pdfUri = null
                    pdfBitmap = null
                    pageNumber = 0
                    pageCount = 0
                }) {
                    Text("Close PDF")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = {
                    zoom = (zoom - 0.25f).coerceAtLeast(0.5f)
                }) {
                    Text("−")
                }

                Text(
                    "  ${(zoom * 100).toInt()}%  ",
                    color = Navy
                )

                OutlinedButton(onClick = {
                    zoom = (zoom + 0.25f).coerceAtMost(3f)
                }) {
                    Text("+")
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                when {
                    loading -> CircularProgressIndicator()

                    error.isNotEmpty() ->
                        Text(error, color = Color.Red)

                    pdfBitmap != null ->
                        Image(
                            bitmap = pdfBitmap!!.asImageBitmap(),
                            contentDescription = "PDF Page",
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(
                                    scaleX = zoom,
                                    scaleY = zoom
                                ),
                            contentScale = ContentScale.Fit
                        )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    enabled = pageNumber > 0 && !loading,
                    onClick = { pageNumber-- }
                ) {
                    Text("Previous")
                }

                Text(
                    "${pageNumber + 1} / $pageCount",
                    color = Navy
                )

                Button(
                    enabled = pageNumber < pageCount - 1 && !loading,
                    onClick = { pageNumber++ }
                ) {
                    Text("Next")
                }
            }
        }
    }
}
