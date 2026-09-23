package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Represents an individual purchased/received material or item.
 * Example: "100 cm wallwasher", 250.0 TL
 */
data class DebtItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val quantity: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalLineAmount: Double
        get() = amount * quantity

    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("amount", amount)
            put("quantity", quantity)
            put("createdAt", createdAt)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): DebtItem {
            return DebtItem(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", ""),
                amount = obj.optDouble("amount", 0.0),
                quantity = obj.optInt("quantity", 1),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Represents a payment made towards a debt.
 * Example: 100.0 TL payment deducted from remaining balance.
 */
data class DebtPayment(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val note: String = "",
    val paymentDate: Long = System.currentTimeMillis()
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("amount", amount)
            put("note", note)
            put("paymentDate", paymentDate)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): DebtPayment {
            return DebtPayment(
                id = obj.optString("id", UUID.randomUUID().toString()),
                amount = obj.optDouble("amount", 0.0),
                note = obj.optString("note", ""),
                paymentDate = obj.optLong("paymentDate", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Database Entity for a debt record (Person or Company).
 * All items and payments are stored in a single record to guarantee
 * atomic consistency and simplicity without complex multi-table syncs.
 */
@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personOrCompany: String,
    val phoneNumber: String = "",
    val notes: String = "",
    val itemsJson: String = "[]",
    val paymentsJson: String = "[]",
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getItems(): List<DebtItem> {
        if (itemsJson.isBlank()) return emptyList()
        return try {
            val array = JSONArray(itemsJson)
            val list = mutableListOf<DebtItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(DebtItem.fromJsonObject(obj))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPayments(): List<DebtPayment> {
        if (paymentsJson.isBlank()) return emptyList()
        return try {
            val array = JSONArray(paymentsJson)
            val list = mutableListOf<DebtPayment>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(DebtPayment.fromJsonObject(obj))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun calculateTotalAmount(): Double {
        return getItems().sumOf { it.totalLineAmount }
    }

    fun calculateTotalPaid(): Double {
        return getPayments().sumOf { it.amount }
    }

    fun calculateRemainingBalance(): Double {
        val remaining = calculateTotalAmount() - calculateTotalPaid()
        return if (remaining < 0.001) 0.0 else remaining
    }

    fun isFullyPaid(): Boolean {
        val total = calculateTotalAmount()
        return total > 0.0 && calculateRemainingBalance() <= 0.001
    }

    companion object {
        fun itemsToJson(items: List<DebtItem>): String {
            val array = JSONArray()
            for (item in items) {
                array.put(item.toJsonObject())
            }
            return array.toString()
        }

        fun paymentsToJson(payments: List<DebtPayment>): String {
            val array = JSONArray()
            for (payment in payments) {
                array.put(payment.toJsonObject())
            }
            return array.toString()
        }
    }
}
