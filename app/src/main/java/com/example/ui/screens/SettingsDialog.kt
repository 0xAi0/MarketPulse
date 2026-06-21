package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketPulseViewModel

@Composable
fun SettingsDialog(
    viewModel: MarketPulseViewModel,
    onDismiss: () -> Unit
) {
    val activeInterval by viewModel.refreshIntervalSeconds.collectAsState()
    var selectedInterval by remember { mutableStateOf(activeInterval) }

    val options = listOf(
        Pair(5, "5 Seconds"),
        Pair(10, "10 Seconds"),
        Pair(30, "30 Seconds"),
        Pair(60, "1 Minute"),
        Pair(300, "5 Minutes")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "SETTINGS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextLight,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Refresh Interval",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayDim
                )

                Spacer(modifier = Modifier.height(8.dp))

                options.forEach { (seconds, label) ->
                    val active = seconds == selectedInterval
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) CyanNeon.copy(alpha = 0.08f) else Color.Transparent)
                            .clickable { selectedInterval = seconds }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = if (active) CyanNeon else TextLight,
                            fontSize = 14.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                        )
                        RadioButton(
                            selected = active,
                            onClick = { selectedInterval = seconds },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CyanNeon,
                                unselectedColor = GrayMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = GrayDim)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            viewModel.updateRefreshInterval(selectedInterval)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
