package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_items WHERE exchange = :exchange ORDER BY isPinned DESC, orderIndex ASC")
    fun getWatchlistForExchange(exchange: String): Flow<List<WatchlistItem>>

    @Query("SELECT * FROM watchlist_items ORDER BY isPinned DESC, orderIndex ASC")
    fun getAllWatchlistItems(): Flow<List<WatchlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WatchlistItem>)

    @Query("DELETE FROM watchlist_items WHERE symbol = :symbol AND exchange = :exchange")
    suspend fun deleteWatchlistItem(symbol: String, exchange: String)

    @Update
    suspend fun updateWatchlistItem(item: WatchlistItem)
}
