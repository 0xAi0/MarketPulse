package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.TickerData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketPulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPulseApp(
    viewModel: MarketPulseViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedDetailTicker by remember { mutableStateOf<TickerData?>(null) }

    val activeTickerForDetail by viewModel.detailTicker.collectAsState()

    // Map selected ticker change to Sheet modal
    LaunchedEffect(activeTickerForDetail) {
        selectedDetailTicker = activeTickerForDetail
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Animated pulse live dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GreenEmerald)
                        )
                        Text(
                            text = "⬡ Market Pulse",
                            style = TextStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(CyanNeon, IndigoNeon, Color(0xFFA78BFA))
                                ),
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = GrayDim)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = TextLight
                ),
                modifier = Modifier.border(0.dp, Color.Transparent)
            )
        },
        containerColor = DarkBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // Material 3 Tabs Selector corresponding to Watchlist / Alerts / Calculators
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = CyanNeon,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanNeon
                    )
                },
                divider = { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderColor)) },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Watchlist", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Watchlist", modifier = Modifier.size(16.dp)) },
                    selectedContentColor = CyanNeon,
                    unselectedContentColor = GrayDim
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Price Alerts", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    icon = { Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Alerts", modifier = Modifier.size(16.dp)) },
                    selectedContentColor = CyanNeon,
                    unselectedContentColor = GrayDim
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Calculators", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    icon = { Icon(imageVector = Icons.Default.Calculate, contentDescription = "Calculators", modifier = Modifier.size(16.dp)) },
                    selectedContentColor = CyanNeon,
                    unselectedContentColor = GrayDim
                )
            }

            // Tab Panels content switcher
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> WatchlistScreen(
                        viewModel = viewModel,
                        onShowDetail = { ticker -> viewModel.showTickerDetail(ticker) }
                    )
                    1 -> AlertsScreen(
                        viewModel = viewModel
                    )
                    2 -> CalculatorsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }

        // Overlay: Settings configurations Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                viewModel = viewModel,
                onDismiss = { showSettingsDialog = false }
            )
        }

        // Overlay: Detail bottom slider panel sheet if selected
        selectedDetailTicker?.let { ticker ->
            DetailBottomSheet(
                viewModel = viewModel,
                ticker = ticker,
                onDismiss = { viewModel.showTickerDetail(null) }
            )
        }
    }
}
