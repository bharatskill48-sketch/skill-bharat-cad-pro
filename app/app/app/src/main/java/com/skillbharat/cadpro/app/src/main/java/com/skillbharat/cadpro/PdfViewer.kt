
package com.skillbharat.cadpro

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PdfViewer() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var message by remember {
        mutableStateOf("Select a PDF Drawing")
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->

        uri?.let {
            message = "Loading PDF..."

            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        context.contentResolver
                            .openFileDescriptor(it, "r")
                            ?.use { descriptor ->
                                PdfRenderer(descriptor).use { renderer ->
                                    if (renderer.pageCount > 0) {
                                        renderer.openPage(0).use { page ->
                                            val width = 1800
                                            val height = (
                                                width.toFloat() *
                                                page.height / page.width
                                            ).toInt().coerceAtLeast(1)

                                            Bitmap.createBitmap(
                                                width,
                                                height,
                                                Bitmap.Config.ARGB_8888
                                            ).also { image ->
                                                page.render(
                                                    image,
                                                    null,
                                                    null,
                                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                                )
                                            }
                                        }
                                    } else null
                                }
                            }
                    }

                    bitmap?.recycle()
                    bitmap = result

                    message = if (result != null)
                        "PDF opened successfully"
                    else "PDF is empty or unavailable"

                } catch (e: Exception) {
                    message = "Unable to open PDF: ${e.message}"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Button(
            onClick = {
                launcher.launch(arrayOf("application/pdf"))
            }
        ) {
            Text("OPEN PDF DRAWING")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(message)

        Spacer(modifier = Modifier.height(8.dp))

        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "PDF Drawing",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
