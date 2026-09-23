package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("""
        SELECT * FROM customers 
        ORDER BY 
            is_favorite DESC,
            CASE status
                WHEN 'PARASI_GELDI' THEN 1
                WHEN 'HAZIRLANIYOR' THEN 2
                WHEN 'TAMAMLANDI' THEN 3
                ELSE 4
            END ASC,
            id DESC
    """)
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("""
        SELECT * FROM customers 
        WHERE full_name LIKE '%' || :query || '%' 
           OR phone_number LIKE '%' || :query || '%' 
           OR address LIKE '%' || :query || '%' 
           OR extra_notes LIKE '%' || :query || '%'
        ORDER BY 
            is_favorite DESC,
            CASE status
                WHEN 'PARASI_GELDI' THEN 1
                WHEN 'HAZIRLANIYOR' THEN 2
                WHEN 'TAMAMLANDI' THEN 3
                ELSE 4
            END ASC,
            id DESC
    """)
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Int): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: Int, newStatus: String)

    @Query("UPDATE customers SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Int)

    @Query("SELECT * FROM customers ORDER BY id ASC")
    suspend fun getAllCustomersList(): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Query("DELETE FROM customers")
    suspend fun clearAllCustomers()
}
