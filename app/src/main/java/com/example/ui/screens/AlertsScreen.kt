package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PriceAlert
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketPulseViewModel
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: MarketPulseViewModel
) {
    val alerts by viewModel.alertsList.collectAsState()
    val rawTickers by viewModel.rawTickers.collectAsState()
    val softwareKeyboard = LocalSoftwareKeyboardController.current

    // Input States
    var symbolInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var isAboveSelection by remember { mutableStateOf(true) }
    var selectedExchange by remember { mutableStateOf("binance") }

    val exchanges = listOf(
        Pair("binance", "Binance"),
        Pair("hyperliquid", "Hyperliquid"),
        Pair("asterdex", "AsterDex"),
        Pair("lighter", "Lighter")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Card wrapper for 'Add Alert' Form input
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CREATE NEW PRICE ALERT",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Exchange dropdown selector button
                var showExDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showExDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Exchange: " + selectedExchange.uppercase())
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Select Exchange", modifier = Modifier.size(12.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = showExDropdown,
                        onDismissRequest = { showExDropdown = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        exchanges.forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = if (selectedExchange == id) CyanNeon else TextLight) },
                                onClick = {
                                    selectedExchange = id
                                    showExDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = symbolInput,
                        onValueChange = { symbolInput = it },
                        label = { Text("Token (e.g. BTC)", fontSize = 11.sp) },
                        placeholder = { Text("BTC", color = GrayMuted) },
                        singleLine = true,
                        textStyle = TextStyle(color = TextLight, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = CyanNeon,
                            cursorColor = CyanNeon
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Trigger Price ($)", fontSize = 11.sp) },
                        placeholder = { Text("98500", color = GrayMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = TextLight, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = CyanNeon,
                            cursorColor = CyanNeon
                        ),
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direction: Above Limit Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val activeAboveColor = CyanNeon.copy(alpha = 0.1f)
                    val activeBelowColor = RedSunset.copy(alpha = 0.1f)

                    Button(
                        onClick = { isAboveSelection = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAboveSelection) activeAboveColor else Color.Transparent,
                            contentColor = if (isAboveSelection) CyanNeon else GrayDim
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.dp,
                                if (isAboveSelection) CyanNeon else BorderColor,
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Above", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("When Above", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { isAboveSelection = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isAboveSelection) activeBelowColor else Color.Transparent,
                            contentColor = if (!isAboveSelection) RedSunset else GrayDim
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.dp,
                                if (!isAboveSelection) RedSunset else BorderColor,
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Below", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("When Below", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Form Action button
                val canSubmit = symbolInput.isNotBlank() && priceInput.toDoubleOrNull() != null
                Button(
                    onClick = {
                        val tarPrice = priceInput.toDoubleOrNull()
                        if (tarPrice != null && symbolInput.isNotBlank()) {
                            viewModel.addNewPriceAlert(
                                symbol = symbolInput,
                                exchange = selectedExchange,
                                targetPrice = tarPrice,
                                isAbove = isAboveSelection
                            )
                            symbolInput = ""
                            priceInput = ""
                            softwareKeyboard?.hide()
                        }
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanNeon,
                        contentColor = Color.Black,
                        disabledContainerColor = DarkSurface,
                        disabledContentColor = GrayMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ADD PRICE ALERT", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Saved alerts listing
        Text(
            text = "ACTIVE & HISTORICAL ALERTS (${alerts.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = GrayDim,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "No alerts",
                        tint = GrayMuted,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No configured price alerts.", color = GrayDim)
                    Text("Get notified when prices cross target limits.", color = GrayMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    AlertRowCard(
                        alert = alert,
                        onToggleActive = { viewModel.togglePriceAlertActive(alert) },
                        onDelete = { viewModel.deletePriceAlert(alert.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertRowCard(
    alert: PriceAlert,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val dirColor = if (alert.isAbove) CyanNeon else RedSunset
    val dirIcon = if (alert.isAbove) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circle arrow directional layout
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(dirColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = dirIcon,
                        contentDescription = null,
                        tint = dirColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = FormatUtils.cleanSymbol(alert.symbol),
                            fontWeight = FontWeight.Black,
                            color = TextLight,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = alert.exchange.uppercase(),
                                color = GrayDim,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = if (alert.isAbove) "Triggers above " else "Triggers below ",
                            color = GrayMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$" + FormatUtils.formatPrice(alert.targetPrice),
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Actions (switch on/off toggle and final delete button)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (alert.triggeredAt != null) {
                    // Alert triggered indicator pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GreenEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("TRIGGERED", color = GreenEmerald, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    Switch(
                        checked = alert.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanNeon,
                            checkedTrackColor = CyanNeon.copy(alpha = 0.2f),
                            uncheckedThumbColor = GrayMuted,
                            uncheckedTrackColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Alert",
                        tint = RedSunset.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
