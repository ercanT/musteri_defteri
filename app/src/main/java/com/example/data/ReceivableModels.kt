package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Represents an individual item, service or product sold/provided to a debtor or customer.
 * Example: "Kablo / Montaj Hizmeti", 250.0 TL
 */
data class ReceivableItem(
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
        fun fromJsonObject(obj: JSONObject): ReceivableItem {
            return ReceivableItem(
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
 * Represents a payment/collection (tahsilat) received towards an open receivable.
 */
data class ReceivablePayment(
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
        fun fromJsonObject(obj: JSONObject): ReceivablePayment {
            return ReceivablePayment(
                id = obj.optString("id", UUID.randomUUID().toString()),
                amount = obj.optDouble("amount", 0.0),
                note = obj.optString("note", ""),
                paymentDate = obj.optLong("paymentDate", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Database Entity for a receivable record (Person or Company).
 */
@Entity(tableName = "receivables")
data class ReceivableEntity(
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
    fun getItems(): List<ReceivableItem> {
        if (itemsJson.isBlank()) return emptyList()
        return try {
            val array = JSONArray(itemsJson)
            val list = mutableListOf<ReceivableItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(ReceivableItem.fromJsonObject(obj))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPayments(): List<ReceivablePayment> {
        if (paymentsJson.isBlank()) return emptyList()
        return try {
            val array = JSONArray(paymentsJson)
            val list = mutableListOf<ReceivablePayment>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(ReceivablePayment.fromJsonObject(obj))
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
        fun itemsToJson(items: List<ReceivableItem>): String {
            val array = JSONArray()
            items.forEach { array.put(it.toJsonObject()) }
            return array.toString()
        }

        fun paymentsToJson(payments: List<ReceivablePayment>): String {
            val array = JSONArray()
            payments.forEach { array.put(it.toJsonObject()) }
            return array.toString()
        }
    }
}
