package com.mdjibon.quotex99.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "trades")
data class Trade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
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
    suspend fun insertTrade(trade: Trade)

    @Query("SELECT * FROM trades ORDER BY timestamp DESC LIMIT 50")
    suspend fun getLastTrades(): List<Trade>

    @Query("DELETE FROM trades")
    suspend fun clearAllTrades()
}

@Database(entities = [Trade::class], version = 1)
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
