package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val symbol: String,
    val exchange: String, // "binance", "hyperliquid", "asterdex", "lighter"
    val targetPrice: Double,
    val isAbove: Boolean, // true if alert triggers when price >= targetPrice, false if <= targetPrice
    val isActive: Boolean = true,
    val triggeredAt: Long? = null // Null if active and untriggered, otherwise the trigger timestamp
)
