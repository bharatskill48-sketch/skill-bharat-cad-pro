
package com.skillbharat.cadpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                CADProApp()
            }
        }
    }
}

@Composable
fun CADProApp() {

    var currentScreen by remember {
        mutableStateOf("CAD")
    }

    val backgroundColor = Color(0xFF101820)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        // APP HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF073B78))
                .padding(16.dp)
        ) {

            Text(
                text = "SKILL BHARAT CAD PRO",
                color = Color.White,
                fontSize = 21.sp
            )

            Text(
                text = "Professional CAD Drawing Studio",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }

        // MAIN MENU
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    currentScreen = "CAD"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("CAD DRAW")
            }

            Button(
                onClick = {
                    currentScreen = "PDF"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("PDF VIEWER")
            }
        }

        // WORKSPACE
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {

            when (currentScreen) {

                "CAD" -> {
                    DrawingCanvas()
                }

                "PDF" -> {
                    PdfViewer()
                }
            }
        }
    }
}
