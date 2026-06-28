package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import com.example.data.db.UpbitMarket
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketPulseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    viewModel: MarketPulseViewModel
) {
    val markets by viewModel.upbitMarkets.collectAsState()
    val isUpbitLoading by viewModel.isUpbitLoading.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedQuoteFilter by remember { mutableStateOf("All") } // "All", "KRW", "BTC", "USDT"

    // Filtered markets
    val filteredMarkets = remember(markets, searchQuery, selectedQuoteFilter) {
        markets.filter { market ->
            val matchesSearch = market.market.contains(searchQuery, ignoreCase = true) ||
                    market.englishName.contains(searchQuery, ignoreCase = true) ||
                    market.koreanName.contains(searchQuery, ignoreCase = true)

            val matchesQuote = when (selectedQuoteFilter) {
                "All" -> true
                else -> market.market.startsWith(selectedQuoteFilter, ignoreCase = true)
            }

            matchesSearch && matchesQuote
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. Header Card with Refresh button and summary
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "UPBIT LISTING DECTECTOR",
                                color = CyanNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Real-time listing alerts & monitor",
                                color = GrayDim,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Refresh/Sync Button
                        IconButton(
                            onClick = { viewModel.triggerUpbitRefresh() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .border(1.dp, BorderColor, CircleShape)
                        ) {
                            if (isUpbitLoading) {
                                CircularProgressIndicator(
                                    color = CyanNeon,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Upbit Listings",
                                    tint = CyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Listings", color = GrayMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${markets.size}", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Filtered", color = GrayMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${filteredMarkets.size}", color = IndigoNeon, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Automatic Alerting", color = GrayMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(GreenEmerald, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enabled", color = GreenEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by symbol, English or Korean name...", fontSize = 13.sp, color = GrayMuted) },
                singleLine = true,
                textStyle = TextStyle(color = TextLight, fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                }),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = GrayMuted, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = GrayMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = CyanNeon,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // 3. Filter Row for Quote Currency
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUOTE:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = GrayMuted,
                    modifier = Modifier.padding(end = 4.dp)
                )

                listOf("All", "KRW", "BTC", "USDT").forEach { filter ->
                    val active = selectedQuoteFilter == filter
                    FilterChip(
                        selected = active,
                        onClick = { selectedQuoteFilter = filter },
                        label = { Text(filter, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.15f),
                            selectedLabelColor = CyanNeon,
                            containerColor = DarkCard,
                            labelColor = GrayDim
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = active,
                            selectedBorderColor = CyanNeon.copy(alpha = 0.4f),
                            borderColor = BorderColor,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            // 4. Listing Items LazyColumn
            if (filteredMarkets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = "Empty",
                            tint = GrayMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "No listings loaded yet." else "No markets matched your search.",
                            color = GrayDim,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "Tap refresh above to sync with Upbit API." else "Try adjusting your filters.",
                            color = GrayMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredMarkets, key = { it.market }) { market ->
                        UpbitListingItemRow(
                            market = market,
                            onToggleAlert = { viewModel.setUpbitAlertEnabled(market.market, it) },
                            onAddToWatchlist = {
                                // Extract the base asset e.g., BTC from KRW-BTC
                                val baseAsset = market.market.split("-").getOrElse(1) { market.market }
                                viewModel.addCoinToActiveWatchlist(baseAsset, "Core")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpbitListingItemRow(
    market: UpbitMarket,
    onToggleAlert: (Boolean) -> Unit,
    onAddToWatchlist: () -> Unit
) {
    val quoteCurrency = market.market.split("-").firstOrNull() ?: "KRW"
    val baseAsset = market.market.split("-").getOrNull(1) ?: market.market

    // Format discovery date nicely
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val formattedDate = remember(market.discoveredAt) { sdf.format(Date(market.discoveredAt)) }

    // Check if the item was discovered recently (e.g. last 48 hours to make it visible on template)
    val isRecentlyDiscovered = remember(market.discoveredAt) {
        System.currentTimeMillis() - market.discoveredAt < 1000 * 3600 * 48
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isRecentlyDiscovered) CyanNeon.copy(alpha = 0.4f) else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Left Column (Asset Name & Details)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = baseAsset,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = TextLight,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))

                    // Quote currency badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (quoteCurrency) {
                                    "KRW" -> Color(0xFF1E3A8A).copy(alpha = 0.15f) // Blue-ish
                                    "BTC" -> Color(0xFFD97706).copy(alpha = 0.15f) // Amber
                                    else -> CyanNeon.copy(alpha = 0.15f)
                                }
                            )
                            .border(
                                1.dp,
                                when (quoteCurrency) {
                                    "KRW" -> Color(0xFF3B82F6).copy(alpha = 0.4f)
                                    "BTC" -> Color(0xFFF59E0B).copy(alpha = 0.4f)
                                    else -> CyanNeon.copy(alpha = 0.4f)
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = quoteCurrency,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (quoteCurrency) {
                                "KRW" -> Color(0xFF60A5FA)
                                "BTC" -> Color(0xFFFBBF24)
                                else -> CyanNeon
                            }
                        )
                    }

                    // "NEW" tag if recently discovered
                    if (isRecentlyDiscovered) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GreenEmerald.copy(alpha = 0.15f))
                                .border(1.dp, GreenEmerald.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NEW",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenEmerald
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Full names English & Korean
                Text(
                    text = "${market.englishName} • ${market.koreanName}",
                    color = GrayDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Discovered Date Label
                Text(
                    text = "Detected: $formattedDate",
                    color = GrayMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Right Actions Block (Alert Toggle & Quick Watchlist Add)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                
                // Watchlist Quick Add Button
                IconButton(
                    onClick = onAddToWatchlist,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.04f), CircleShape)
                        .border(1.dp, BorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = "Quick Add to Watchlist",
                        tint = GrayDim,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Listing Alert Toggler (Bell Icon)
                IconButton(
                    onClick = { onToggleAlert(!market.isAlertEnabled) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (market.isAlertEnabled) CyanNeon.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.04f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (market.isAlertEnabled) CyanNeon.copy(alpha = 0.3f) else BorderColor,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (market.isAlertEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = "Toggle Listing Alert",
                        tint = if (market.isAlertEnabled) CyanNeon else GrayDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
