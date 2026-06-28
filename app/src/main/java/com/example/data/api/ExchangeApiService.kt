package com.example.data.api

import okhttp3.ResponseBody
import retrofit2.http.*

interface ExchangeApiService {

    @GET("https://fapi.binance.com/fapi/v1/ticker/24hr")
    suspend fun getBinanceTickers(): List<BinanceTicker>

    @GET("https://fapi.binance.com/fapi/v1/exchangeInfo")
    suspend fun getBinanceExchangeInfo(): BinanceExchangeInfo

    @GET("https://fapi.binance.com/fapi/v1/klines")
    suspend fun getBinanceKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int
    ): List<List<Any>>

    @POST("https://api.hyperliquid.xyz/info")
    @Headers("Content-Type: application/json")
    suspend fun getHyperliquidInfo(@Body body: HyperliquidRequest): ResponseBody

    @GET("https://fapi.asterdex.com/fapi/v1/klines")
    suspend fun getAsterDexKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int
    ): List<List<Any>>

    @GET("https://fapi.asterdex.com/fapi/v1/ticker/24hr")
    suspend fun getAsterDexTickers(): List<AsterDexTicker>

    @GET
    suspend fun getLighterCandles(@Url url: String): ResponseBody

    @GET("https://api.upbit.com/v1/market/all")
    suspend fun getUpbitMarkets(
        @Query("isDetails") isDetails: Boolean = false
    ): List<UpbitMarketResponse>
}
