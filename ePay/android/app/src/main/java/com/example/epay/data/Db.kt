package com.example.epay.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val paymentIntentId: String,
    val amount: Long,
    val currency: String,
    val note: String,
    val status: String,
    val customerName: String,
    val googleEmail: String?,
    val errorMessage: String?,
    val timestamp: Long
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE paymentIntentId = :id LIMIT 1")
    suspend fun findById(id: String): TransactionEntity?
}

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class EPayDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var instance: EPayDatabase? = null
        fun get(context: Context): EPayDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context, EPayDatabase::class.java, "epay.db")
                .build().also { instance = it }
        }
    }
}