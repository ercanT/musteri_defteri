package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "full_name")
    val fullName: String,
    
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "extra_notes")
    val extraNotes: String,
    
    @ColumnInfo(name = "status")
    val status: String = CustomerStatus.HAZIRLANIYOR.name,

    @ColumnInfo(name = "amount", defaultValue = "0.0")
    val amount: Double = 0.0,

    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
