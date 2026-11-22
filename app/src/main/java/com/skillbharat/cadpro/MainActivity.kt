
package com.skillbharat.cadpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                SkillBharatCADApp()
            }
        }
    }
}

@Composable
fun SkillBharatCADApp() {
    var selectedTab by remember { mutableStateOf("DRAW") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("SKILL BHARAT CAD PRO")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedTab = "DRAW" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("DRAW")
                }

                Button(
                    onClick = { selectedTab = "PDF" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("PDF")
                }
            }

            when (selectedTab) {
                "DRAW" -> DrawingCanvas()
                "PDF" -> PdfViewer()
            }
        }
    }
}
