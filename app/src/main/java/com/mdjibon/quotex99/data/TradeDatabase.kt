package com.mdjibon.quotex99.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Database(entities = [TradeEntity::class], version = 1)
abstract class TradeDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao

    companion object {
        @Volatile
        private var INSTANCE: TradeDatabase? = null

        fun getDatabase(context: Context): TradeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TradeDatabase::class.java,
                    "quotex_trades.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey
    val id: String,
    val direction: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val amount: Int,
    val status: String,
    val profit: Double,
    val profitPercent: Double,
    val pattern: String,
    val confidence: Float,
    val createdAt: Long,
    val closedAt: Long
) {
    fun toTradeResult(): TradeResult {
        return TradeResult(
            id = id,
            direction = TradeDirection.valueOf(direction),
            entryPrice = entryPrice,
            exitPrice = exitPrice,
            amount = amount,
            status = TradeStatus.valueOf(status),
            profit = profit,
            profitPercent = profitPercent,
            pattern = pattern,
            confidence = confidence,
            createdAt = createdAt,
            closedAt = closedAt
        )
    }
}

@Dao
interface TradeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity)

    @Update
    suspend fun updateTrade(trade: TradeEntity)

    @Delete
    suspend fun deleteTrade(trade: TradeEntity)

    @Query("SELECT * FROM trades ORDER BY createdAt DESC")
    fun getAllTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE status = 'WON' OR status = 'LOST'")
    fun getCompletedTrades(): Flow<List<TradeEntity>>

    @Query("SELECT COUNT(*) FROM trades WHERE status = 'WON'")
    suspend fun getWinCount(): Int

    @Query("SELECT COUNT(*) FROM trades WHERE status = 'LOST'")
    suspend fun getLossCount(): Int

    @Query("SELECT SUM(profit) FROM trades WHERE status = 'WON' OR status = 'LOST'")
    suspend fun getTotalProfit(): Double?

    @Query("SELECT AVG(confidence) FROM trades WHERE status = 'WON'")
    suspend fun getAverageConfidenceWins(): Float?

    @Query("DELETE FROM trades")
    suspend fun clearAllTrades()

    @Query("SELECT * FROM trades WHERE createdAt >= :startTime ORDER BY createdAt DESC")
    fun getTradesSince(startTime: Long): Flow<List<TradeEntity>>
}

class TradeRepository(private val tradeDao: TradeDao) {
    val allTrades: Flow<List<TradeEntity>> = tradeDao.getAllTrades()
    val completedTrades: Flow<List<TradeEntity>> = tradeDao.getCompletedTrades()

    suspend fun insertTrade(trade: TradeResult) {
        val entity = TradeEntity(
            id = trade.id,
            direction = trade.direction.name,
            entryPrice = trade.entryPrice,
            exitPrice = trade.exitPrice,
            amount = trade.amount,
            status = trade.status.name,
            profit = trade.profit,
            profitPercent = trade.profitPercent,
            pattern = trade.pattern,
            confidence = trade.confidence,
            createdAt = trade.createdAt,
            closedAt = trade.closedAt
        )
        tradeDao.insertTrade(entity)
    }

    suspend fun updateTrade(trade: TradeResult) {
        val entity = TradeEntity(
            id = trade.id,
            direction = trade.direction.name,
            entryPrice = trade.entryPrice,
            exitPrice = trade.exitPrice,
            amount = trade.amount,
            status = trade.status.name,
            profit = trade.profit,
            profitPercent = trade.profitPercent,
            pattern = trade.pattern,
            confidence = trade.confidence,
            createdAt = trade.createdAt,
            closedAt = trade.closedAt
        )
        tradeDao.updateTrade(entity)
    }

    suspend fun getWinRate(): Float {
        val wins = tradeDao.getWinCount()
        val losses = tradeDao.getLossCount()
        val total = wins + losses
        return if (total > 0) (wins.toFloat() / total) * 100 else 0f
    }

    suspend fun getTotalStats(): Map<String, Any> {
        return mapOf(
            "wins" to (tradeDao.getWinCount() ?: 0),
            "losses" to (tradeDao.getLossCount() ?: 0),
            "totalProfit" to (tradeDao.getTotalProfit() ?: 0.0),
            "avgConfidence" to (tradeDao.getAverageConfidenceWins() ?: 0f)
        )
    }

    suspend fun clearHistory() {
        tradeDao.clearAllTrades()
    }
}
