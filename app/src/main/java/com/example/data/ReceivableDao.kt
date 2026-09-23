package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivableDao {
    @Query("SELECT * FROM receivables ORDER BY updatedAt DESC, id DESC")
    fun getAllReceivables(): Flow<List<ReceivableEntity>>

    @Query("SELECT * FROM receivables WHERE id = :id LIMIT 1")
    suspend fun getReceivableById(id: Long): ReceivableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceivable(receivable: ReceivableEntity): Long

    @Update
    suspend fun updateReceivable(receivable: ReceivableEntity)

    @Delete
    suspend fun deleteReceivable(receivable: ReceivableEntity)

    @Query("DELETE FROM receivables WHERE id = :id")
    suspend fun deleteReceivableById(id: Long)

    @Query("SELECT COUNT(*) FROM receivables")
    suspend fun getReceivableCount(): Int

    @Query("SELECT * FROM receivables ORDER BY id ASC")
    suspend fun getAllReceivablesList(): List<ReceivableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(receivables: List<ReceivableEntity>)

    @Query("DELETE FROM receivables")
    suspend fun clearAllReceivables()
}
