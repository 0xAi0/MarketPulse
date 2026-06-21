package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketPulseViewModel
import com.example.utils.FormatUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    viewModel: MarketPulseViewModel
) {
    val uiTickers by viewModel.uiTickers.collectAsState()
    val rawTickers by viewModel.rawTickers.collectAsState()
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Holdings state (virtual list of quantities the user owns, which we save in reactive state)
    // To keep it simple and responsive, we can save user's entered holdings dynamically!
    var selectedSymbolForHolding by remember { mutableStateOf("") }
    var holdingQuantityInput by remember { mutableStateOf("") }
    val holdingsList = remember { mutableStateMapOf<String, Double>() } // Map of Symbol/Exchange to Quantity

    // Preset initial state if empty to show usage
    LaunchedEffect(uiTickers) {
        if (holdingsList.isEmpty() && uiTickers.isNotEmpty()) {
            val first = uiTickers.firstOrNull { it.price > 0.0 }
            if (first != null) {
                holdingsList[first.symbol] = 1.5
            }
        }
    }

    // Target Calculator Input States
    var targetBasePrice by remember { mutableStateOf("") }
    var targetPercentageChange by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // --- SECTION 1: DYNAMIC PORTFOLIO VALUER ---
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
                    text = "VIRTUAL PORTFOLIO BALANCER",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select ticker to add
                var showAddDropdown by remember { mutableStateOf(false) }
                val eligiblePairs = uiTickers.filter { it.price > 0.0 }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showAddDropdown = true },
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
                            Text(
                                if (selectedSymbolForHolding.isEmpty()) "Select Tracked Token..." else "Token: $selectedSymbolForHolding"
                            )
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Dropdown", modifier = Modifier.size(16.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = showAddDropdown,
                        onDismissRequest = { showAddDropdown = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        if (eligiblePairs.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No active tickers loaded.", color = GrayMuted) },
                                onClick = { showAddDropdown = false }
                            )
                        } else {
                            eligiblePairs.forEach { pair ->
                                DropdownMenuItem(
                                    text = { Text(pair.displaySymbol, color = TextLight) },
                                    onClick = {
                                        selectedSymbolForHolding = pair.symbol
                                        showAddDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = holdingQuantityInput,
                        onValueChange = { holdingQuantityInput = it },
                        label = { Text("Quantity Owned", fontSize = 11.sp) },
                        placeholder = { Text("e.g. 2.45", color = GrayMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                    Button(
                        onClick = {
                            val qty = holdingQuantityInput.toDoubleOrNull()
                            if (qty != null && selectedSymbolForHolding.isNotBlank()) {
                                holdingsList[selectedSymbolForHolding] = qty
                                selectedSymbolForHolding = ""
                                holdingQuantityInput = ""
                                keyboardController?.hide()
                            }
                        },
                        enabled = selectedSymbolForHolding.isNotBlank() && holdingQuantityInput.toDoubleOrNull() != null,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Portfolio Summary Valuation Card
                val tickerMap = uiTickers.associateBy { it.symbol }
                var totalValuation = 0.0
                val renderedHoldings = mutableListOf<Triple<String, Double, Double>>() // Symbol, Qty, TotalVal

                holdingsList.forEach { (symbol, qty) ->
                    val pair = tickerMap[symbol]
                    val price = pair?.price ?: 0.0
                    val valuatedSum = qty * price
                    totalValuation += valuatedSum
                    renderedHoldings.add(Triple(pair?.displaySymbol ?: symbol, qty, valuatedSum))
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("PORTFOLIO NET BALANCE", color = GrayDim, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "$%,.2f", totalValuation),
                            color = CyanNeon,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Virtual holdings entries list
                if (renderedHoldings.isNotEmpty()) {
                    Text("PORTFOLIO ASSETS", color = GrayMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    renderedHoldings.forEach { (displaySym, qty, totalVal) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(displaySym, fontWeight = FontWeight.Bold, color = TextLight, fontSize = 13.sp)
                                Text("$qty Owned", color = GrayMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.US, "$%,.2f", totalVal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextLight,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { holdingsList.remove(holdingsList.keys.find { it.contains(displaySym) }) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = RedSunset.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: PERCENTAGE GAINS TARGET CALCULATOR ---
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(bottom = 32.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TARGET PROJECTION MATRIX",
                    color = IndigoNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = targetBasePrice,
                        onValueChange = { targetBasePrice = it },
                        label = { Text("Base Entry Price ($)", fontSize = 11.sp) },
                        placeholder = { Text("e.g. 500.0", color = GrayMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(color = TextLight, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoNeon,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = IndigoNeon,
                            cursorColor = IndigoNeon
                        ),
                        modifier = Modifier.weight(1.1f)
                    )

                    OutlinedTextField(
                        value = targetPercentageChange,
                        onValueChange = { targetPercentageChange = it },
                        label = { Text("Target gain (%)", fontSize = 11.sp) },
                        placeholder = { Text("20", color = GrayMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(color = TextLight, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoNeon,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = IndigoNeon,
                            cursorColor = IndigoNeon
                        ),
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val basePriceVal = targetBasePrice.toDoubleOrNull() ?: 0.0
                val pctChangeVal = targetPercentageChange.toDoubleOrNull() ?: 0.0

                if (basePriceVal > 0.0) {
                    val projectedTarget = basePriceVal * (1.0 + pctChangeVal / 100.0)
                    val dollarDiff = projectedTarget - basePriceVal

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Projected Target price", color = GrayDim, fontSize = 12.sp)
                            Text(
                                text = "$" + FormatUtils.formatPrice(projectedTarget),
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Return per Coin", color = GrayDim, fontSize = 12.sp)
                            Text(
                                text = (if (dollarDiff >= 0.0) "+" else "") + "$" + FormatUtils.formatPrice(dollarDiff),
                                fontWeight = FontWeight.Bold,
                                color = if (dollarDiff >= 0.0) GreenEmerald else RedSunset,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = GrayMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enter a base entry price to track projections", color = GrayMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
