package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TickerData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketPulseViewModel
import com.example.utils.FormatUtils

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: MarketPulseViewModel,
    onShowDetail: (TickerData) -> Unit
) {
    val selectedExchange by viewModel.selectedExchange.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uiTickers by viewModel.uiTickers.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    var addInputSymbol by remember { mutableStateOf("") }

    val exchanges = listOf(
        Pair("binance", "Binance Futures"),
        Pair("hyperliquid", "Hyperliquid"),
        Pair("asterdex", "AsterDex"),
        Pair("lighter", "Lighter")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Live Exchange Selector Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            exchanges.forEach { (ex, label) ->
                val active = selectedExchange == ex
                val badgeColor = when (ex) {
                    "hyperliquid" -> CyanNeon
                    "lighter" -> IndigoNeon
                    "asterdex" -> GreenEmerald
                    "binance" -> YellowAmber
                    else -> CyanNeon
                }
                FilterChip(
                    selected = active,
                    onClick = { viewModel.selectExchange(ex) },
                    label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = badgeColor.copy(alpha = 0.15f),
                        selectedLabelColor = badgeColor,
                        selectedLeadingIconColor = badgeColor,
                        containerColor = DarkCard,
                        labelColor = GrayDim
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = active,
                        selectedBorderColor = badgeColor.copy(alpha = 0.4f),
                        selectedBorderWidth = 1.dp,
                        borderColor = BorderColor,
                        borderWidth = 1.dp
                    )
                )
            }
        }

        // 2. Add Symbol and View Switcher Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = addInputSymbol,
                onValueChange = { addInputSymbol = it },
                placeholder = { Text("Add pair (e.g. BTC)...", fontSize = 13.sp, color = GrayMuted) },
                singleLine = true,
                textStyle = TextStyle(color = TextLight, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (addInputSymbol.isNotBlank()) {
                        viewModel.addCoinToActiveWatchlist(addInputSymbol)
                        addInputSymbol = ""
                        keyboardController?.hide()
                    }
                }),
                trailingIcon = {
                    IconButton(onClick = {
                        if (addInputSymbol.isNotBlank()) {
                            viewModel.addCoinToActiveWatchlist(addInputSymbol)
                            addInputSymbol = ""
                            keyboardController?.hide()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add ticker", tint = CyanNeon)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = CyanNeon
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Grid / List Toggler Button Pair
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkCard)
                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                    .padding(2.dp)
            ) {
                IconButton(
                    onClick = { viewModel.setGridView(true) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (isGridView) CyanNeon.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Grid View",
                        tint = if (isGridView) CyanNeon else GrayDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.setGridView(false) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (!isGridView) CyanNeon.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "List View",
                        tint = if (!isGridView) CyanNeon else GrayDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Live dot status indicators top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(GreenEmerald, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedExchange) {
                        "binance" -> "Binance Perpetuals (Streaming)"
                        "hyperliquid" -> "Hyperliquid Perpetuals"
                        "asterdex" -> "AsterDex Futures"
                        else -> "Lighter Orderbook"
                    },
                    fontSize = 12.sp,
                    color = GrayDim,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (isLoading) {
                CircularProgressIndicator(color = CyanNeon, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
            }
        }

        // 3. Grid / List UI content body
        if (uiTickers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = "Empty",
                        tint = GrayMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No tickers in watchlist.", color = GrayDim)
                    Text("Enter a ticker symbol above to start tracking.", color = GrayMuted, fontSize = 12.sp)
                }
            }
        } else {
            if (isGridView) {
                // Render GRID of cards
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiTickers, key = { it.symbol }) { ticker ->
                        WatchlistGridCard(
                            ticker = ticker,
                            onCardClick = { onShowDetail(ticker) },
                            onPinClick = { viewModel.togglePinSymbol(ticker.symbol) },
                            onDeleteClick = { viewModel.removeCoinFromWatchlist(ticker.symbol) }
                        )
                    }
                }
            } else {
                // Render LIST of rows
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiTickers, key = { it.symbol }) { ticker ->
                        WatchlistRowItem(
                            ticker = ticker,
                            onRowClick = { onShowDetail(ticker) },
                            onPinClick = { viewModel.togglePinSymbol(ticker.symbol) },
                            onDeleteClick = { viewModel.removeCoinFromWatchlist(ticker.symbol) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistGridCard(
    ticker: TickerData,
    onCardClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val pos = ticker.priceChangePercent >= 0
    val color = if (pos) GreenEmerald else RedSunset

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (ticker.isPinned) CyanNeon.copy(alpha = 0.35f) else BorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onCardClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticker.displaySymbol,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TextLight
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pinned Icon
                    IconButton(
                        onClick = onPinClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin top",
                            tint = if (ticker.isPinned) CyanNeon else GrayMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete ticker",
                            tint = RedSunset.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Monospace Price Label
            Text(
                text = if (ticker.price > 0.0) "$" + FormatUtils.formatPrice(ticker.price) else "Fallback",
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (ticker.price > 0.0) TextLight else RedSunset.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom stats footer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Change badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (pos) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = String.format("%.2f", ticker.priceChangePercent) + "%",
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Volume
                Text(
                    text = FormatUtils.formatVolume(ticker.volume),
                    color = GrayMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun WatchlistRowItem(
    ticker: TickerData,
    onRowClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val pos = ticker.priceChangePercent >= 0
    val color = if (pos) GreenEmerald else RedSunset

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (ticker.isPinned) CyanNeon.copy(alpha = 0.35f) else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onRowClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left block (Symbol character icon & Ticker Label)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, BorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ticker.displaySymbol.take(2),
                        fontWeight = FontWeight.Bold,
                        color = GrayDim,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = ticker.displaySymbol,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        fontSize = 15.sp
                    )
                    Text(
                        text = FormatUtils.formatVolume(ticker.volume) + " Vol",
                        color = GrayMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Right block (Monospace active Ticking price, change pill, modifier pins)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = if (ticker.price > 0.0) "$" + FormatUtils.formatPrice(ticker.price) else "Fallback",
                        color = if (ticker.price > 0.0) TextLight else RedSunset.copy(alpha = 0.8f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = (if (pos) "+" else "") + String.format("%.2f", ticker.priceChangePercent) + "%",
                        color = color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Actions Column
                IconButton(
                    onClick = onPinClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin top",
                        tint = if (ticker.isPinned) CyanNeon else GrayMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete ticker",
                        tint = RedSunset.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
