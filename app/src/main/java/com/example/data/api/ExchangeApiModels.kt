package com.example.data.api

import com.squareup.moshi.JsonClass

// === BINANCE MODELS ===
@JsonClass(generateAdapter = true)
data class BinanceTicker(
    val symbol: String,
    val lastPrice: String,
    val priceChangePercent: String,
    val quoteVolume: String,
    val highPrice: String?,
    val lowPrice: String?,
    val count: Long? = 0
)

@JsonClass(generateAdapter = true)
data class BinanceExchangeInfo(
    val symbols: List<BinanceSymbolInfo>
)

@JsonClass(generateAdapter = true)
data class BinanceSymbolInfo(
    val symbol: String,
    val status: String,
    val contractType: String,
    val quoteAsset: String
)

// === ASTERDEX MODELS ===
@JsonClass(generateAdapter = true)
data class AsterDexTicker(
    val symbol: String,
    val lastPrice: String,
    val priceChangePercent: String,
    val quoteVolume: String,
    val highPrice: String?,
    val lowPrice: String?
)

// === HYPERLIQUID REQUEST ===
@JsonClass(generateAdapter = true)
data class HyperliquidRequest(
    val type: String
)

// === HYPERLIQUID HELPER PARSER MODELS ===
@JsonClass(generateAdapter = true)
class HyperliquidCoin(val name: String)

@JsonClass(generateAdapter = true)
class HyperliquidUniverseContainer(val universe: List<HyperliquidCoin>)

@JsonClass(generateAdapter = true)
class HyperliquidCtx(
    val prevDayPx: String?,
    val markPx: String?,
    val dayNtlVlm: String?
)

// === LIGHTER MODELS ===
@JsonClass(generateAdapter = true)
data class LighterCandlesResponse(
    val c: List<Any>? = null, // Can be array of doubles or array of objects
    val v: List<Any>? = null,
    val h: List<Any>? = null,
    val l: List<Any>? = null,
    val o: List<Any>? = null
)

// === UPBIT MODELS ===
@JsonClass(generateAdapter = true)
data class UpbitMarketResponse(
    val market: String,
    val korean_name: String,
    val english_name: String
)
