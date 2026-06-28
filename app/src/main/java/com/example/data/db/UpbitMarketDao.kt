package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UpbitMarketDao {
    @Query("SELECT * FROM upbit_markets ORDER BY discoveredAt DESC")
    fun getAllMarketsFlow(): Flow<List<UpbitMarket>>

    @Query("SELECT * FROM upbit_markets ORDER BY discoveredAt DESC")
    suspend fun getAllMarkets(): List<UpbitMarket>

    @Query("SELECT COUNT(*) FROM upbit_markets")
    suspend fun getMarketsCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMarkets(markets: List<UpbitMarket>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarket(market: UpbitMarket)

    @Query("UPDATE upbit_markets SET isAlertEnabled = :enabled WHERE market = :market")
    suspend fun setAlertEnabled(market: String, enabled: Boolean)

    @Query("DELETE FROM upbit_markets")
    suspend fun deleteAll()
}
