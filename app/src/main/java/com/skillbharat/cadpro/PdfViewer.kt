package com.skillbharat.cadpro

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PdfViewer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "SKILL BHARAT PDF VIEWER",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("PDF Viewer প্রস্তুত হচ্ছে।")
        Text("পরবর্তী ধাপে PDF ফাইল খোলার সুবিধা যোগ করব।")
    }
}
