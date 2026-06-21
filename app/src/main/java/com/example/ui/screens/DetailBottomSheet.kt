package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TickerData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketPulseViewModel
import com.example.utils.FormatUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailBottomSheet(
    viewModel: MarketPulseViewModel,
    ticker: TickerData,
    onDismiss: () -> Unit
) {
    val klinesRange by viewModel.detailKlinesRange.collectAsState()
    val lookbackPeriod by viewModel.lookbackPeriod.collectAsState()
    val historicalLow by viewModel.historicalLow.collectAsState()
    val recoveryDays by viewModel.recoveryDays.collectAsState()

    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Calculator States
    var coinsInput by remember { mutableStateOf("") }
    var targetPctInput by remember { mutableStateOf("") }
    var basePriceCustomInput by remember { mutableStateOf("") }

    // Synchronize remote historical low to input field
    LaunchedEffect(historicalLow) {
        historicalLow?.let {
            basePriceCustomInput = String.format("%.4f", it)
        } ?: run {
            basePriceCustomInput = ""
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DarkSurface,
        contentColor = TextLight,
        dragHandle = { BottomSheetDefaults.DragHandle(color = GrayMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Exchange Badge
                    val badgeColor = when (ticker.exchange) {
                        "hyperliquid" -> CyanNeon
                        "lighter" -> IndigoNeon
                        "asterdex" -> GreenEmerald
                        "binance" -> YellowAmber
                        else -> CyanNeon
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = ticker.exchange.uppercase().take(2),
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = FormatUtils.cleanSymbol(ticker.symbol),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close details",
                        tint = GrayDim
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Price Display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$" + FormatUtils.formatPrice(ticker.price),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Price Change
                val pos = ticker.priceChangePercent >= 0
                val color = if (pos) GreenEmerald else RedSunset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = (if (pos) "▲ +" else "▼ ") + String.format("%.2f", ticker.priceChangePercent) + "%",
                        color = color,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 1: Custom Price Range Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(CyanNeon, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PRICE RANGE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayDim
                            )
                        }

                        // Period Dropdown Selector
                        var showDropdown by remember { mutableStateOf(false) }
                        Box {
                            TextButton(
                                onClick = { showDropdown = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = CyanNeon)
                            ) {
                                Text(
                                    text = when (lookbackPeriod) {
                                        "15m" -> "15 Minutes"
                                        "30m" -> "30 Minutes"
                                        "1h" -> "1 Hour"
                                        "2h" -> "2 Hours"
                                        "4h" -> "4 Hours"
                                        "8h" -> "8 Hours"
                                        "12h" -> "12 Hours"
                                        "24h" -> "24 Hours"
                                        "3d" -> "3 Days"
                                        "7d" -> "7 Days"
                                        else -> "24 Hours"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DropdownMenu(
                                expanded = showDropdown,
                                onDismissRequest = { showDropdown = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                listOf("15m", "30m", "1h", "2h", "4h", "8h", "12h", "24h", "3d", "7d").forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = when (p) {
                                                    "15m" -> "15 Minutes"
                                                    "30m" -> "30 Minutes"
                                                    "1h" -> "1 Hour"
                                                    "2h" -> "2 Hours"
                                                    "4h" -> "4 Hours"
                                                    "8h" -> "8 Hours"
                                                    "12h" -> "12 Hours"
                                                    "24h" -> "24 Hours"
                                                    "3d" -> "3 Days"
                                                    "7d" -> "7 Days"
                                                    else -> p
                                                },
                                                color = if (p == lookbackPeriod) CyanNeon else TextLight
                                            )
                                        },
                                        onClick = {
                                            viewModel.changeLookbackPeriod(p)
                                            showDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (klinesRange == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CyanNeon, strokeWidth = 2.dp)
                        }
                    } else {
                        val (low, high) = klinesRange!!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Low", color = GrayMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("$" + FormatUtils.formatPrice(low), color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("High", color = GrayMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("$" + FormatUtils.formatPrice(high), color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Linear Slider Bar: proportional Cyan Marker
                        val proportion = if (high > low) {
                            val current = ticker.price
                            ((current - low) / (high - low)).coerceIn(0.0, 1.0).toFloat()
                        } else 0.5f

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // Slider gradient track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(RedSunset, YellowAmber, GreenEmerald)
                                        )
                                    )
                            )

                            // Slider thumb marker
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(proportion)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size(16.dp)
                                        .background(Color.White, CircleShape)
                                        .border(2.dp, CyanNeon, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Recovery Performance calculations from multi-day lows
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(GreenEmerald, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RECOVERY & PERFORMANCE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayDim
                            )
                        }

                        // Days Period Selector Dropdown
                        var showDaysDropdown by remember { mutableStateOf(false) }
                        Box {
                            TextButton(
                                onClick = { showDaysDropdown = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = GreenEmerald)
                            ) {
                                Text(
                                    text = "$recoveryDays Days Low",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DropdownMenu(
                                expanded = showDaysDropdown,
                                onDismissRequest = { showDaysDropdown = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                listOf(1, 7, 30, 90, 180, 365).forEach { days ->
                                    DropdownMenuItem(
                                        text = { Text("$days Days Low", color = if (days == recoveryDays) GreenEmerald else TextLight) },
                                        onClick = {
                                            viewModel.changeRecoveryDays(days)
                                            showDaysDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = basePriceCustomInput,
                            onValueChange = {
                                basePriceCustomInput = it
                            },
                            label = { Text("Base Price ($)", fontSize = 12.sp) },
                            textStyle = TextStyle(color = TextLight, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenEmerald,
                                unfocusedBorderColor = BorderColor,
                                focusedLabelColor = GreenEmerald,
                                cursorColor = GreenEmerald
                            ),
                            placeholder = { Text(text = if (historicalLow == null) "Fetching..." else "Enter Price...") },
                            modifier = Modifier.weight(1f)
                        )

                        // Calculate percentage recovery immediately
                        val basePrice = basePriceCustomInput.toDoubleOrNull() ?: 0.0
                        val recoveryPercentage = if (basePrice > 0.0) {
                            ((ticker.price - basePrice) / basePrice) * 100.0
                        } else null

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .align(Alignment.CenterVertically)
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Up from Base Price", color = GrayMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (recoveryPercentage != null) {
                                            (if (recoveryPercentage >= 0.0) "+" else "") + String.format("%.2f", recoveryPercentage) + "%"
                                        } else "—",
                                        color = if (recoveryPercentage != null && recoveryPercentage >= 0.0) GreenEmerald else RedSunset,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2.5: Multi-day Performance Grid
            val performances by viewModel.historicalPerformances.collectAsState()

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(IndigoNeon, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TIMEFRAME PERFORMANCE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayDim
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (performances == null) {
                        Box(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = IndigoNeon, strokeWidth = 2.dp)
                        }
                    } else {
                        // Display 2-column Grid
                        val chunked = performances!!.chunked(2)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunked.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { perf ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = perf.timeframe,
                                                        color = TextLight,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    
                                                    val chipColor = if (perf.isUp) GreenEmerald else RedSunset
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(chipColor.copy(alpha = 0.1f))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (perf.isUp) "UP" else "DOWN",
                                                            color = chipColor,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = (if (perf.isUp) "+" else "") + String.format("%.2f", perf.changePercent) + "%",
                                                    color = if (perf.isUp) GreenEmerald else RedSunset,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )

                                                Text(
                                                    text = "Price then: $" + FormatUtils.formatPrice(perf.priceThen),
                                                    color = GrayMuted,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                    if (rowItems.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Calculators (Holdings & Target)
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(IndigoNeon, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CALCULATORS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayDim
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calculator 1: Holdings Valuation Evaluator
                    Text("Holdings Value (Asset amount)", color = GrayDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = coinsInput,
                        onValueChange = { coinsInput = it },
                        placeholder = { Text("e.g. 10.5", color = GrayMuted) },
                        textStyle = TextStyle(color = TextLight, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoNeon,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = IndigoNeon
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val computedValuation = (coinsInput.toDoubleOrNull() ?: 0.0) * ticker.price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Portfolio Value", color = GrayDim, fontSize = 12.sp)
                        Text(
                            text = if (computedValuation > 0.0) {
                                String.format(Locale.US, "$%,.2f", computedValuation)
                            } else "$0.00",
                            color = IndigoNeon,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Calculator 2: Target Valuation Evaluator
                    Text("Target price projection by change (%)", color = GrayDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = targetPctInput,
                        onValueChange = { targetPctInput = it },
                        placeholder = { Text("e.g. +10 or -5", color = GrayMuted) },
                        textStyle = TextStyle(color = TextLight, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoNeon,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = IndigoNeon
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val changePct = targetPctInput.toDoubleOrNull() ?: 0.0
                    val projectedPrice = ticker.price * (1.0 + changePct / 100.0)
                    val priceDifference = projectedPrice - ticker.price

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Price", color = GrayDim, fontSize = 12.sp)
                            Text(
                                text = "$" + FormatUtils.formatPrice(projectedPrice),
                                color = TextLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Difference", color = GrayDim, fontSize = 12.sp)
                            Text(
                                text = (if (priceDifference >= 0.0) "+" else "") + "$" + FormatUtils.formatPrice(priceDifference),
                                color = if (priceDifference >= 0.0) GreenEmerald else RedSunset,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
