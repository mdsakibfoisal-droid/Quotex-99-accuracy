package com.mdjibon.quotex99.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "trades")
data class Trade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val market: String,
    val action: String,
    val amount: Int,
    val result: String,
    val patternId: Int? = null,
    val similarity: Float = 0f,
    val status: String = "EXECUTED"
)

@Dao
interface TradeDao {
    @Insert
    suspend fun insertTrade(trade: Trade): Long

    @Query("SELECT * FROM trades ORDER BY timestamp DESC LIMIT 50")
    suspend fun getLastTrades(): List<Trade>

    @Query("SELECT * FROM trades WHERE market = :market ORDER BY timestamp DESC LIMIT 20")
    suspend fun getTradesByMarket(market: String): List<Trade>

    @Query("SELECT COUNT(*) FROM trades WHERE result = 'WIN'")
    suspend fun getWinCount(): Int

    @Query("SELECT COUNT(*) FROM trades")
    suspend fun getTotalCount(): Int

    @Query("DELETE FROM trades")
    suspend fun clearAllTrades()

    @Query("SELECT * FROM trades WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getTradesAfter(startTime: Long): List<Trade>
}

@Database(entities = [Trade::class], version = 1, exportSchema = false)
abstract class TradeDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao

    companion object {
        @Volatile
        private var instance: TradeDatabase? = null

        fun getInstance(context: Context): TradeDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TradeDatabase::class.java,
                    "quotex_trade_db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}
