package com.example.data

import kotlinx.coroutines.flow.Flow

class DebtRepository(private val debtDao: DebtDao) {
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()

    suspend fun getDebtById(id: Long): DebtEntity? {
        return debtDao.getDebtById(id)
    }

    suspend fun insertDebt(debt: DebtEntity): Long {
        return debtDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtEntity) {
        debtDao.updateDebt(debt.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteDebt(id: Long) {
        debtDao.deleteDebtById(id)
    }

    /**
     * Seeding is disabled per user request. No demo data is automatically inserted.
     */
    suspend fun seedSampleIfEmpty() {
        // Disabled: all debts are created manually by the user
    }

    suspend fun addItemToDebt(debtId: Long, item: DebtItem): Boolean {
        val existing = debtDao.getDebtById(debtId) ?: return false
        val currentItems = existing.getItems().toMutableList()
        currentItems.add(item)
        val updated = existing.copy(
            itemsJson = DebtEntity.itemsToJson(currentItems),
            updatedAt = System.currentTimeMillis()
        )
        debtDao.updateDebt(updated)
        return true
    }

    suspend fun removeItemFromDebt(debtId: Long, itemId: String): Boolean {
        val existing = debtDao.getDebtById(debtId) ?: return false
        val currentItems = existing.getItems().filter { it.id != itemId }
        val updated = existing.copy(
            itemsJson = DebtEntity.itemsToJson(currentItems),
            updatedAt = System.currentTimeMillis()
        )
        debtDao.updateDebt(updated)
        return true
    }

    suspend fun addPaymentToDebt(debtId: Long, payment: DebtPayment): Boolean {
        val existing = debtDao.getDebtById(debtId) ?: return false
        val currentPayments = existing.getPayments().toMutableList()
        currentPayments.add(payment)
        val updated = existing.copy(
            paymentsJson = DebtEntity.paymentsToJson(currentPayments),
            updatedAt = System.currentTimeMillis()
        )
        debtDao.updateDebt(updated)
        return true
    }

    suspend fun updatePaymentInDebt(debtId: Long, updatedPayment: DebtPayment): Boolean {
        val existing = debtDao.getDebtById(debtId) ?: return false
        val currentPayments = existing.getPayments().map {
            if (it.id == updatedPayment.id) updatedPayment else it
        }
        val updated = existing.copy(
            paymentsJson = DebtEntity.paymentsToJson(currentPayments),
            updatedAt = System.currentTimeMillis()
        )
        debtDao.updateDebt(updated)
        return true
    }

    suspend fun removePaymentFromDebt(debtId: Long, paymentId: String): Boolean {
        val existing = debtDao.getDebtById(debtId) ?: return false
        val currentPayments = existing.getPayments().filter { it.id != paymentId }
        val updated = existing.copy(
            paymentsJson = DebtEntity.paymentsToJson(currentPayments),
            updatedAt = System.currentTimeMillis()
        )
        debtDao.updateDebt(updated)
        return true
    }
}
