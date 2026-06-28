package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TickerData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketPulseViewModel
import com.example.utils.FormatUtils
import kotlin.math.roundToInt

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
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    var addInputSymbol by remember { mutableStateOf("") }
    var selectedAddCategory by remember { mutableStateOf("Core") }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }

    // Drag-and-drop states
    var draggingSymbol by remember { mutableStateOf<String?>(null) }
    var draggingDisplaySymbol by remember { mutableStateOf("") }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }

    val exchanges = listOf(
        Pair("binance", "Binance Futures"),
        Pair("hyperliquid", "Hyperliquid"),
        Pair("asterdex", "AsterDex"),
        Pair("lighter", "Lighter")
    )

    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // Core 5 Drop categories for visual dragging targets
        val dropCategories = listOf("Core", "DeFi", "L1/L2", "Memes", "Web3")
        val dropColWidth = widthPx / dropCategories.size

        // Calculate if dragging position is hovering over the bottom drop tray
        val trayHeightPx = with(density) { 140.dp.toPx() }
        val isOverTray = draggingSymbol != null && dragPosition.y > (heightPx - trayHeightPx - 100f)
        val hoveredCategoryIndex = if (isOverTray) {
            (dragPosition.x / dropColWidth).toInt().coerceIn(0, dropCategories.lastIndex)
        } else {
            -1
        }
        val hoveredCategory = if (hoveredCategoryIndex != -1) dropCategories[hoveredCategoryIndex] else null

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

            // 1.5. Live Category Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORY:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = GrayMuted,
                    modifier = Modifier.padding(end = 4.dp)
                )
                availableCategories.forEach { cat ->
                    val active = selectedCategory == cat
                    FilterChip(
                        selected = active,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoNeon.copy(alpha = 0.15f),
                            selectedLabelColor = IndigoNeon,
                            containerColor = DarkCard,
                            labelColor = GrayDim
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = active,
                            selectedBorderColor = IndigoNeon.copy(alpha = 0.4f),
                            borderColor = BorderColor,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            // 2. Add Symbol Row with category indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                            viewModel.addCoinToActiveWatchlist(addInputSymbol, selectedAddCategory)
                            addInputSymbol = ""
                            keyboardController?.hide()
                        }
                    }),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (addInputSymbol.isNotBlank()) {
                                viewModel.addCoinToActiveWatchlist(addInputSymbol, selectedAddCategory)
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

            // 2.2. Category Selection Chips for ADDING a ticker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Add to:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayMuted
                )
                val addCategories = listOf("Core", "DeFi", "L1/L2", "Memes", "Web3", "+ Custom")
                addCategories.forEach { cat ->
                    val isSel = if (cat == "+ Custom") false else selectedAddCategory == cat
                    val isCustomSelected = cat == "+ Custom" && !listOf("Core", "DeFi", "L1/L2", "Memes", "Web3").contains(selectedAddCategory)
                    
                    InputChip(
                        selected = isSel || isCustomSelected,
                        onClick = {
                            if (cat == "+ Custom") {
                                showCustomCategoryDialog = true
                            } else {
                                selectedAddCategory = cat
                            }
                        },
                        label = { 
                            Text(
                                text = if (cat == "+ Custom" && isCustomSelected) selectedAddCategory else cat, 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.15f),
                            selectedLabelColor = CyanNeon,
                            containerColor = DarkCard,
                            labelColor = GrayDim
                        ),
                        border = InputChipDefaults.inputChipBorder(
                            enabled = true,
                            selected = isSel || isCustomSelected,
                            borderColor = BorderColor,
                            selectedBorderColor = CyanNeon.copy(alpha = 0.3f),
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            // Custom category alert dialog for adding
            if (showCustomCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showCustomCategoryDialog = false },
                    title = { Text("New Category", color = TextLight, fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = customCategoryInput,
                            onValueChange = { customCategoryInput = it },
                            placeholder = { Text("E.g. Layer2, AI, Metaverse...", color = GrayMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = CyanNeon,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (customCategoryInput.isNotBlank()) {
                                    selectedAddCategory = customCategoryInput.trim()
                                }
                                showCustomCategoryDialog = false
                                customCategoryInput = ""
                            }
                        ) {
                            Text("Set", color = CyanNeon, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomCategoryDialog = false }) {
                            Text("Cancel", color = GrayDim)
                        }
                    },
                    containerColor = DarkSurface
                )
            }

            // 3. Live dot status indicators top
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

            // Drag Instruction Label when dragging starts
            if (draggingSymbol == null) {
                Text(
                    text = "Tip: Long-press card to drag and drop onto a category",
                    color = GrayMuted,
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 4. Grid / List UI content body
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
                        Text(
                            text = if (selectedCategory == "All") "No tickers in watchlist." else "No tickers in '$selectedCategory'.", 
                            color = GrayDim
                        )
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
                            val isDraggingThis = draggingSymbol == ticker.symbol
                            WatchlistGridCard(
                                ticker = ticker,
                                onCardClick = { onShowDetail(ticker) },
                                onPinClick = { viewModel.togglePinSymbol(ticker.symbol) },
                                onDeleteClick = { viewModel.removeCoinFromWatchlist(ticker.symbol) },
                                onCategoryChange = { cat -> viewModel.changeCoinCategory(ticker.symbol, cat) },
                                onDragStart = { offset ->
                                    draggingSymbol = ticker.symbol
                                    draggingDisplaySymbol = ticker.displaySymbol
                                    dragPosition = Offset(widthPx / 2f, heightPx / 2f)
                                },
                                onDrag = { amount ->
                                    dragPosition = Offset(
                                        x = (dragPosition.x + amount.x).coerceIn(0f, widthPx),
                                        y = (dragPosition.y + amount.y).coerceIn(0f, heightPx)
                                    )
                                },
                                onDragEnd = {
                                    if (hoveredCategory != null) {
                                        viewModel.changeCoinCategory(draggingSymbol!!, hoveredCategory)
                                    }
                                    draggingSymbol = null
                                },
                                onDragCancel = {
                                    draggingSymbol = null
                                },
                                isDraggingAny = draggingSymbol != null,
                                modifier = Modifier.alpha(if (isDraggingThis) 0.2f else 1f)
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
                            val isDraggingThis = draggingSymbol == ticker.symbol
                            WatchlistRowItem(
                                ticker = ticker,
                                onRowClick = { onShowDetail(ticker) },
                                onPinClick = { viewModel.togglePinSymbol(ticker.symbol) },
                                onDeleteClick = { viewModel.removeCoinFromWatchlist(ticker.symbol) },
                                onCategoryChange = { cat -> viewModel.changeCoinCategory(ticker.symbol, cat) },
                                onDragStart = { offset ->
                                    draggingSymbol = ticker.symbol
                                    draggingDisplaySymbol = ticker.displaySymbol
                                    dragPosition = Offset(widthPx / 2f, heightPx / 2f)
                                },
                                onDrag = { amount ->
                                    dragPosition = Offset(
                                        x = (dragPosition.x + amount.x).coerceIn(0f, widthPx),
                                        y = (dragPosition.y + amount.y).coerceIn(0f, heightPx)
                                    )
                                },
                                onDragEnd = {
                                    if (hoveredCategory != null) {
                                        viewModel.changeCoinCategory(draggingSymbol!!, hoveredCategory)
                                    }
                                    draggingSymbol = null
                                },
                                onDragCancel = {
                                    draggingSymbol = null
                                },
                                isDraggingAny = draggingSymbol != null,
                                modifier = Modifier.alpha(if (isDraggingThis) 0.2f else 1f)
                            )
                        }
                    }
                }
            }
        }

        // 5. Drag-and-drop Bottom Categorize Tray overlay
        AnimatedVisibility(
            visible = draggingSymbol != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .border(
                        width = 2.dp,
                        color = CyanNeon.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DRAG ${draggingDisplaySymbol.uppercase()} TO CATEGORY",
                        color = CyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // 5 Columns Drop Zones
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dropCategories.forEachIndexed { idx, cat ->
                            val isHovered = hoveredCategory == cat
                            val activeColor = when (cat) {
                                "Core" -> YellowAmber
                                "DeFi" -> IndigoNeon
                                "L1/L2" -> GreenEmerald
                                "Memes" -> RedSunset
                                else -> CyanNeon
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isHovered) activeColor.copy(alpha = 0.15f)
                                        else Color.White.copy(alpha = 0.02f)
                                    )
                                    .border(
                                        width = if (isHovered) 2.dp else 1.dp,
                                        color = if (isHovered) activeColor else BorderColor.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when (cat) {
                                            "Core" -> Icons.Default.Star
                                            "DeFi" -> Icons.Default.Savings
                                            "L1/L2" -> Icons.Default.Layers
                                            "Memes" -> Icons.Default.Face
                                            else -> Icons.Default.Folder
                                        },
                                        contentDescription = cat,
                                        tint = if (isHovered) activeColor else GrayMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat,
                                        color = if (isHovered) TextLight else GrayDim,
                                        fontSize = 11.sp,
                                        fontWeight = if (isHovered) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Floating Drag shadow following touch position
        if (draggingSymbol != null) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (dragPosition.x - 75f).roundToInt(),
                            y = (dragPosition.y - 40f).roundToInt()
                        )
                    }
                    .shadow(12.dp, RoundedCornerShape(12.dp))
                    .background(DarkCard, RoundedCornerShape(12.dp))
                    .border(2.dp, CyanNeon, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CyanNeon, CircleShape)
                    )
                    Text(
                        text = draggingDisplaySymbol,
                        color = TextLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
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
    onDeleteClick: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    isDraggingAny: Boolean,
    modifier: Modifier = Modifier
) {
    val pos = ticker.priceChangePercent >= 0
    val color = if (pos) GreenEmerald else RedSunset

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (ticker.isPinned) CyanNeon.copy(alpha = 0.35f) else BorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(ticker.symbol) {
                detectDragGesturesAfterLongPress(
                    onDragStart = onDragStart,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel
                )
            }
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
                Column {
                    Text(
                        text = ticker.displaySymbol,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextLight
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    // Small Category badge clickable
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, BorderColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .clickable { showCategoryMenu = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Category",
                            tint = GrayMuted,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = ticker.category,
                            color = GrayDim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box {
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            Text(
                                text = " MOVE TO CATEGORY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = GrayMuted,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            listOf("Core", "DeFi", "L1/L2", "Memes", "Web3").forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = if (ticker.category == cat) CyanNeon else TextLight, fontSize = 12.sp) },
                                    onClick = {
                                        onCategoryChange(cat)
                                        showCategoryMenu = false
                                    }
                                )
                            }
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                            DropdownMenuItem(
                                text = { Text("+ Custom Category", color = CyanNeon, fontSize = 12.sp) },
                                onClick = {
                                    showCategoryMenu = false
                                    showCustomCategoryDialog = true
                                }
                            )
                        }
                    }

                    if (showCustomCategoryDialog) {
                        AlertDialog(
                            onDismissRequest = { showCustomCategoryDialog = false },
                            title = { Text("Move to New Category", color = TextLight, fontWeight = FontWeight.Bold) },
                            text = {
                                OutlinedTextField(
                                    value = customCategoryInput,
                                    onValueChange = { customCategoryInput = it },
                                    placeholder = { Text("Category name...", color = GrayMuted) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanNeon,
                                        unfocusedBorderColor = BorderColor,
                                        cursorColor = CyanNeon,
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight
                                    )
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        if (customCategoryInput.isNotBlank()) {
                                            onCategoryChange(customCategoryInput.trim())
                                        }
                                        showCustomCategoryDialog = false
                                        customCategoryInput = ""
                                    }
                                ) {
                                    Text("Move", color = CyanNeon, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showCustomCategoryDialog = false }) {
                                    Text("Cancel", color = GrayDim)
                                }
                            },
                            containerColor = DarkSurface
                        )
                    }
                }

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
    onDeleteClick: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    isDraggingAny: Boolean,
    modifier: Modifier = Modifier
) {
    val pos = ticker.priceChangePercent >= 0
    val color = if (pos) GreenEmerald else RedSunset

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (ticker.isPinned) CyanNeon.copy(alpha = 0.35f) else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(ticker.symbol) {
                detectDragGesturesAfterLongPress(
                    onDragStart = onDragStart,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel
                )
            }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = ticker.displaySymbol,
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            fontSize = 15.sp
                        )

                        // Compact Category badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, BorderColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .clickable { showCategoryMenu = true }
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = ticker.category,
                                color = GrayDim,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = FormatUtils.formatVolume(ticker.volume) + " Vol",
                        color = GrayMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Box {
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            Text(
                                text = " MOVE TO CATEGORY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = GrayMuted,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            listOf("Core", "DeFi", "L1/L2", "Memes", "Web3").forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = if (ticker.category == cat) CyanNeon else TextLight, fontSize = 12.sp) },
                                    onClick = {
                                        onCategoryChange(cat)
                                        showCategoryMenu = false
                                    }
                                )
                            }
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                            DropdownMenuItem(
                                text = { Text("+ Custom Category", color = CyanNeon, fontSize = 12.sp) },
                                onClick = {
                                    showCategoryMenu = false
                                    showCustomCategoryDialog = true
                                }
                            )
                        }
                    }

                    if (showCustomCategoryDialog) {
                        AlertDialog(
                            onDismissRequest = { showCustomCategoryDialog = false },
                            title = { Text("Move to New Category", color = TextLight, fontWeight = FontWeight.Bold) },
                            text = {
                                OutlinedTextField(
                                    value = customCategoryInput,
                                    onValueChange = { customCategoryInput = it },
                                    placeholder = { Text("Category name...", color = GrayMuted) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanNeon,
                                        unfocusedBorderColor = BorderColor,
                                        cursorColor = CyanNeon,
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight
                                    )
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        if (customCategoryInput.isNotBlank()) {
                                            onCategoryChange(customCategoryInput.trim())
                                        }
                                        showCustomCategoryDialog = false
                                        customCategoryInput = ""
                                    }
                                ) {
                                    Text("Move", color = CyanNeon, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showCustomCategoryDialog = false }) {
                                    Text("Cancel", color = GrayDim)
                                }
                            },
                            containerColor = DarkSurface
                        )
                    }
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
