package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upbit_markets")
data class UpbitMarket(
    @PrimaryKey
    val market: String, // e.g. "KRW-BTC"
    val koreanName: String,
    val englishName: String,
    val isAlertEnabled: Boolean = true,
    val discoveredAt: Long = System.currentTimeMillis()
)
