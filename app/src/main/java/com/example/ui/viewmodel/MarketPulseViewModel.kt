package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.PriceAlert
import com.example.data.db.WatchlistItem
import com.example.data.repository.MarketPulseRepository
import com.example.data.repository.TickerData
import com.example.data.repository.TimeframePerformance
import com.example.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MarketPulseRepository(application, db.watchlistDao(), db.priceAlertDao())

    private val _selectedExchange = MutableStateFlow("binance")
    val selectedExchange: StateFlow<String> = _selectedExchange.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _refreshIntervalSeconds = MutableStateFlow(10)
    val refreshIntervalSeconds: StateFlow<Int> = _refreshIntervalSeconds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _tickers = MutableStateFlow<List<TickerData>>(emptyList())
    val rawTickers: StateFlow<List<TickerData>> = _tickers.asStateFlow()

    // Loaded watchlist items for active exchange
    val watchlistItems: StateFlow<List<WatchlistItem>> = _selectedExchange
        .flatMapLatest { exchange -> repository.getWatchlistForExchange(exchange) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Dynamically fetch all categories present in the watchlist for the active exchange, plus default ones
    val availableCategories: StateFlow<List<String>> = watchlistItems
        .map { list ->
            val defaults = listOf("All", "Core", "DeFi", "L1/L2", "Memes", "Web3")
            val custom = list.map { it.category }.distinct()
            (defaults + custom).distinct()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All", "Core", "DeFi", "L1/L2", "Memes", "Web3"))

    // UI-ready tickers, matching order inside watchlist, filtered by selected category if not "All"
    val uiTickers: StateFlow<List<TickerData>> = combine(watchlistItems, _tickers, _selectedCategory) { watchlist, tickers, selectedCat ->
        val filteredWatchlist = if (selectedCat == "All") {
            watchlist
        } else {
            watchlist.filter { it.category.uppercase() == selectedCat.uppercase() }
        }
        val tickersMap = tickers.associateBy { it.symbol.uppercase() }
        filteredWatchlist.map { item ->
            val symUpper = item.symbol.uppercase()
            // AsterDex and Binance might have suffix standard
            var testSym = symUpper
            var match = tickersMap[testSym]
            if (match == null && !testSym.endsWith("USDT")) {
                match = tickersMap[testSym + "USDT"]
            }
            match?.copy(isPinned = item.isPinned, category = item.category) ?: TickerData(
                symbol = item.symbol,
                displaySymbol = item.symbol.replace("USDT", "").replace("-USDT", "").replace("-USD", ""),
                exchange = item.exchange,
                price = 0.0,
                priceChangePercent = 0.0,
                volume = 0.0,
                high24h = null,
                low24h = null,
                isPinned = item.isPinned,
                category = item.category
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Price Alerts
    val alertsList: StateFlow<List<PriceAlert>> = repository.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Modal detail ticker lookup
    private val _detailTicker = MutableStateFlow<TickerData?>(null)
    val detailTicker: StateFlow<TickerData?> = _detailTicker.asStateFlow()

    private val _detailKlinesRange = MutableStateFlow<Pair<Double, Double>?>(null)
    val detailKlinesRange: StateFlow<Pair<Double, Double>?> = _detailKlinesRange.asStateFlow()

    private val _lookbackPeriod = MutableStateFlow("24h")
    val lookbackPeriod: StateFlow<String> = _lookbackPeriod.asStateFlow()

    private val _historicalLow = MutableStateFlow<Double?>(null)
    val historicalLow: StateFlow<Double?> = _historicalLow.asStateFlow()

    private val _recoveryDays = MutableStateFlow(30)
    val recoveryDays: StateFlow<Int> = _recoveryDays.asStateFlow()

    private val _historicalPerformances = MutableStateFlow<List<TimeframePerformance>?>(null)
    val historicalPerformances: StateFlow<List<TimeframePerformance>?> = _historicalPerformances.asStateFlow()

    private var refreshJob: Job? = null

    init {
        // Pre-create notification channel
        NotificationHelper.createNotificationChannel(application)

        viewModelScope.launch {
            repository.checkAndPrepopulateWatchlist()
        }

        // Periodically refresh tickers for selecting exchange
        viewModelScope.launch {
            combine(_selectedExchange, _refreshIntervalSeconds) { ex, seconds -> Pair(ex, seconds) }
                .collect { (exchange, seconds) ->
                    startTickerRefreshLoop(exchange, seconds)
                }
        }
    }

    private fun startTickerRefreshLoop(exchange: String, seconds: Int) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                _isLoading.value = true
                val fetched = repository.fetchTickersForExchange(exchange)
                _tickers.value = fetched
                checkPriceAlerts(fetched, exchange)
                _isLoading.value = false
                delay(seconds * 1000L)
            }
        }
    }

    private fun checkPriceAlerts(liveTickers: List<TickerData>, exchange: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val alerts = alertsList.value.filter { it.isActive && it.exchange == exchange }
            val tickersMap = liveTickers.associateBy { it.symbol.uppercase() }
            for (alert in alerts) {
                val tickerUpper = alert.symbol.uppercase()
                // Check direct matching or check matching with USDT suffix
                var ticker = tickersMap[tickerUpper]
                if (ticker == null && !tickerUpper.endsWith("USDT")) {
                    ticker = tickersMap[tickerUpper + "USDT"]
                }

                if (ticker != null && ticker.price > 0.0) {
                    val currentPrice = ticker.price
                    val triggered = if (alert.isAbove) {
                        currentPrice >= alert.targetPrice
                    } else {
                        currentPrice <= alert.targetPrice
                    }

                    if (triggered) {
                        repository.triggerAlert(alert.id)
                        viewModelScope.launch(Dispatchers.Main) {
                            NotificationHelper.showPriceAlertNotification(
                                getApplication(),
                                alert,
                                currentPrice
                            )
                        }
                    }
                }
            }
        }
    }

    // --- VIEW TRIGGERS ---
    fun selectExchange(exchange: String) {
        _selectedExchange.value = exchange
    }

    fun setGridView(isGrid: Boolean) {
        _isGridView.value = isGrid
    }

    fun updateRefreshInterval(seconds: Int) {
        _refreshIntervalSeconds.value = seconds
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // --- COIN ADD/REMOVE/PIN ---
    fun addCoinToActiveWatchlist(symbol: String, category: String = "Core") {
        viewModelScope.launch {
            var formattedSymbol = symbol.trim().uppercase()
            // Binance & AsterDex lookups standard suffix
            if ((_selectedExchange.value == "binance" || _selectedExchange.value == "asterdex") && !formattedSymbol.endsWith("USDT")) {
                formattedSymbol += "USDT"
            }
            repository.addWatchlistItem(formattedSymbol, _selectedExchange.value, category)
            forceImmediateRefresh()
        }
    }

    fun changeCoinCategory(symbol: String, category: String) {
        viewModelScope.launch {
            repository.updateWatchlistItemCategory(symbol, _selectedExchange.value, category)
            forceImmediateRefresh()
        }
    }

    fun removeCoinFromWatchlist(symbol: String) {
        viewModelScope.launch {
            repository.removeWatchlistItem(symbol, _selectedExchange.value)
            forceImmediateRefresh()
        }
    }

    fun togglePinSymbol(symbol: String) {
        viewModelScope.launch {
            val match = watchlistItems.value.find { it.symbol == symbol }
            if (match != null) {
                repository.setWatchlistItemPinned(symbol, _selectedExchange.value, !match.isPinned)
            }
        }
    }

    fun reorderSymbols(fromSym: String, toSym: String) {
        viewModelScope.launch {
            repository.reorderWatchlist(_selectedExchange.value, fromSym, toSym)
        }
    }

    private fun forceImmediateRefresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val fetched = repository.fetchTickersForExchange(_selectedExchange.value)
            _tickers.value = fetched
            checkPriceAlerts(fetched, _selectedExchange.value)
            _isLoading.value = false
        }
    }

    // --- DETAILED RANGES INFO ---
    fun showTickerDetail(ticker: TickerData?) {
        _detailTicker.value = ticker
        _detailKlinesRange.value = null
        _historicalLow.value = null
        _historicalPerformances.value = null
        if (ticker != null) {
            loadKlinesRange(ticker.symbol, lookbackPeriod.value)
            loadHistoricalLow(ticker.symbol, recoveryDays.value)
            loadHistoricalPerformances(ticker.symbol, ticker.price)
        }
    }

    fun changeLookbackPeriod(period: String) {
        _lookbackPeriod.value = period
        _detailTicker.value?.let { loadKlinesRange(it.symbol, period) }
    }

    private fun loadKlinesRange(symbol: String, period: String) {
        viewModelScope.launch {
            _detailKlinesRange.value = null
            val range = repository.fetchDetailedRanges(symbol, period)
            _detailKlinesRange.value = range
        }
    }

    private fun loadHistoricalPerformances(symbol: String, currentPrice: Double) {
        viewModelScope.launch {
            _historicalPerformances.value = null
            val perfs = repository.fetchHistoricalPerformances(symbol, currentPrice)
            _historicalPerformances.value = perfs
        }
    }

    fun changeRecoveryDays(days: Int) {
        _recoveryDays.value = days
        _detailTicker.value?.let { loadHistoricalLow(it.symbol, days) }
    }

    private fun loadHistoricalLow(symbol: String, days: Int) {
        viewModelScope.launch {
            _historicalLow.value = null
            val low = repository.fetchHistoricalLow(symbol, days)
            _historicalLow.value = low
        }
    }

    // --- ALERTS TRIGGER ACTIONS ---
    fun addNewPriceAlert(symbol: String, exchange: String, targetPrice: Double, isAbove: Boolean) {
        viewModelScope.launch {
            var formattedSymbol = symbol.trim().uppercase()
            if ((exchange == "binance" || exchange == "asterdex") && !formattedSymbol.endsWith("USDT")) {
                formattedSymbol += "USDT"
            }
            repository.addAlert(formattedSymbol, exchange, targetPrice, isAbove)
        }
    }

    fun deletePriceAlert(id: Int) {
        viewModelScope.launch {
            repository.deleteAlert(id)
        }
    }

    fun togglePriceAlertActive(alert: PriceAlert) {
        viewModelScope.launch {
            repository.toggleAlertActive(alert)
        }
    }
}
