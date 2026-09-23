package com.example.data

import kotlinx.coroutines.flow.Flow

class ReceivableRepository(
    private val receivableDao: ReceivableDao
) {
    val allReceivables: Flow<List<ReceivableEntity>> = receivableDao.getAllReceivables()

    suspend fun getReceivableById(id: Long): ReceivableEntity? {
        return receivableDao.getReceivableById(id)
    }

    suspend fun insertReceivable(receivable: ReceivableEntity): Long {
        return receivableDao.insertReceivable(receivable)
    }

    suspend fun updateReceivable(receivable: ReceivableEntity) {
        receivableDao.updateReceivable(receivable.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteReceivable(receivable: ReceivableEntity) {
        receivableDao.deleteReceivable(receivable)
    }

    suspend fun deleteReceivableById(id: Long) {
        receivableDao.deleteReceivableById(id)
    }

    suspend fun addItemToReceivable(receivableId: Long, item: ReceivableItem): Boolean {
        val current = receivableDao.getReceivableById(receivableId) ?: return false
        val currentItems = current.getItems().toMutableList()
        currentItems.add(0, item)
        val updatedJson = ReceivableEntity.itemsToJson(currentItems)
        receivableDao.updateReceivable(
            current.copy(
                itemsJson = updatedJson,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun removeItemFromReceivable(receivableId: Long, itemId: String): Boolean {
        val current = receivableDao.getReceivableById(receivableId) ?: return false
        val currentItems = current.getItems().filter { it.id != itemId }
        val updatedJson = ReceivableEntity.itemsToJson(currentItems)
        receivableDao.updateReceivable(
            current.copy(
                itemsJson = updatedJson,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun addPaymentToReceivable(receivableId: Long, payment: ReceivablePayment): Boolean {
        val current = receivableDao.getReceivableById(receivableId) ?: return false
        val currentPayments = current.getPayments().toMutableList()
        currentPayments.add(0, payment)
        val updatedJson = ReceivableEntity.paymentsToJson(currentPayments)
        receivableDao.updateReceivable(
            current.copy(
                paymentsJson = updatedJson,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun updatePaymentInReceivable(receivableId: Long, updatedPayment: ReceivablePayment): Boolean {
        val current = receivableDao.getReceivableById(receivableId) ?: return false
        val currentPayments = current.getPayments().toMutableList()
        val index = currentPayments.indexOfFirst { it.id == updatedPayment.id }
        if (index == -1) return false
        currentPayments[index] = updatedPayment
        val updatedJson = ReceivableEntity.paymentsToJson(currentPayments)
        receivableDao.updateReceivable(
            current.copy(
                paymentsJson = updatedJson,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun removePaymentFromReceivable(receivableId: Long, paymentId: String): Boolean {
        val current = receivableDao.getReceivableById(receivableId) ?: return false
        val currentPayments = current.getPayments().filter { it.id != paymentId }
        val updatedJson = ReceivableEntity.paymentsToJson(currentPayments)
        receivableDao.updateReceivable(
            current.copy(
                paymentsJson = updatedJson,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }
}
