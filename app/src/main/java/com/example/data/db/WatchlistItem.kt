package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_items")
data class WatchlistItem(
    @PrimaryKey
    val symbol: String, // e.g. "BTCUSDT" or "BTC"
    val exchange: String, // "binance", "hyperliquid", "asterdex", "lighter"
    val isPinned: Boolean = false,
    val orderIndex: Int = 0,
    val category: String = "Core"
)
