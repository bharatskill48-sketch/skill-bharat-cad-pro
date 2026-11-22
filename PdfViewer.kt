
package com.skillbharat.cadpro

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun PdfViewer() {
    val context = LocalContext.current
    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val descriptor = context.contentResolver
                    .openFileDescriptor(it, "r")

                descriptor?.use { file ->
                    PdfRenderer(file).use { renderer ->
                        if (renderer.pageCount > 0) {
                            renderer.openPage(0).use { page ->
                                val bitmap = Bitmap.createBitmap(
                                    page.width,
                                    page.height,
                                    Bitmap.Config.ARGB_8888
                                )
                                page.render(
                                    bitmap,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                                pdfBitmap = bitmap
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                pdfBitmap = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Button(
            onClick = {
                launcher.launch(arrayOf("application/pdf"))
            }
        ) {
            Text("OPEN PDF DRAWING")
        }

        Spacer(modifier = Modifier.height(12.dp))

        pdfBitmap?.let { bitmap ->
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "PDF Drawing",
                modifier = Modifier.fillMaxWidth()
            )
        } ?: Text("Select a PDF drawing to open")
    }
}
