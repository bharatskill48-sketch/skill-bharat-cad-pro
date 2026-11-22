
package com.skillbharat.cadpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CADProApp()
        }
    }
}

@Composable
fun CADProApp() {
    var tool by remember { mutableStateOf("SELECT") }

    val tools = listOf(
        "SELECT", "LINE", "RECT", "CIRCLE",
        "MOVE", "MEASURE"
    )

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101820))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF073B78))
                    .padding(16.dp)
            ) {
                Text(
                    "SKILL BHARAT CAD PRO",
                    color = Color.White,
                    fontSize = 21.sp
                )
                Text(
                    "Professional CAD Workspace",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("OPEN", "SAVE", "PDF").forEach {
                    Button(onClick = {}) {
                        Text(it)
                    }
                }
            }

            Text(
                "DRAWING TOOLS",
                color = Color.White,
                modifier = Modifier.padding(10.dp)
            )

            tools.chunked(3).forEach { group ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    group.forEach { item ->
                        Button(
                            onClick = { tool = item },
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (tool == item)
                                        Color(0xFF168C78)
                                    else Color(0xFF263746)
                            )
                        ) {
                            Text(item)
                        }
                    }
                }
            }

            Text(
                "WORKSPACE  |  $tool",
                color = Color(0xFFFFD166),
                modifier = Modifier.padding(12.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color(0xFF182633))
            ) {
                val step = 40f

                for (x in 0..(size.width / step).toInt()) {
                    drawLine(
                        Color(0xFF304453),
                        Offset(x * step, 0f),
                        Offset(x * step, size.height),
                        strokeWidth = 1f
                    )
                }

                for (y in 0..(size.height / step).toInt()) {
                    drawLine(
                        Color(0xFF304453),
                        Offset(0f, y * step),
                        Offset(size.width, y * step),
                        strokeWidth = 1f
                    )
                }

                drawLine(
                    Color.Cyan,
                    Offset(50f, 100f),
                    Offset(size.width - 50f, 100f),
                    strokeWidth = 3f
                )

                drawRect(
                    Color(0xFFFFD166),
                    topLeft = Offset(80f, 150f),
                    size = androidx.compose.ui.geometry.Size(180f, 120f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(3f)
                )
            }
        }
    }
}
