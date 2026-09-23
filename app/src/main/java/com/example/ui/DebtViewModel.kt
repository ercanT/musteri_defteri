package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DebtEntity
import com.example.data.DebtItem
import com.example.data.DebtPayment
import com.example.data.DebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DebtFilter {
    ALL,
    UNPAID,   // Bakiye Var (Açık Borçlar)
    DUE_DATE, // Vadeliler
    PAID      // Kapanmış / Ödenmiş
}

data class DebtSummary(
    val totalItemsAmount: Double = 0.0,
    val totalPaidAmount: Double = 0.0,
    val totalRemainingBalance: Double = 0.0,
    val openDebtCount: Int = 0,
    val paidOffCount: Int = 0,
    val totalCount: Int = 0,
    val dueCount: Int = 0
)

class DebtViewModel(
    application: Application,
    private val repository: DebtRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DebtFilter.UNPAID)
    val selectedFilter: StateFlow<DebtFilter> = _selectedFilter.asStateFlow()

    val rawDebts = repository.allDebts

    val summary: StateFlow<DebtSummary> = rawDebts
        .combine(MutableStateFlow(Unit)) { list, _ ->
            var totalItems = 0.0
            var totalPaid = 0.0
            var totalBalance = 0.0
            var openCount = 0
            var paidCount = 0
            var dueCount = 0

            for (debt in list) {
                val itemsSum = debt.calculateTotalAmount()
                val paidSum = debt.calculateTotalPaid()
                val balance = debt.calculateRemainingBalance()

                totalItems += itemsSum
                totalPaid += paidSum
                totalBalance += balance

                if (balance > 0.001) {
                    openCount++
                    if (debt.dueDate != null) {
                        dueCount++
                    }
                } else {
                    paidCount++
                }
            }

            DebtSummary(
                totalItemsAmount = totalItems,
                totalPaidAmount = totalPaid,
                totalRemainingBalance = totalBalance,
                openDebtCount = openCount,
                paidOffCount = paidCount,
                totalCount = list.size,
                dueCount = dueCount
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DebtSummary()
        )

    val debts: StateFlow<List<DebtEntity>> = combine(
        rawDebts,
        _searchQuery,
        _selectedFilter
    ) { list, query, filter ->
        val trimmed = query.trim().lowercase()

        list.filter { debt ->
            val matchesFilter = when (filter) {
                DebtFilter.ALL -> true
                DebtFilter.UNPAID -> debt.calculateRemainingBalance() > 0.001
                DebtFilter.DUE_DATE -> debt.dueDate != null && debt.calculateRemainingBalance() > 0.001
                DebtFilter.PAID -> debt.calculateRemainingBalance() <= 0.001
            }

            if (!matchesFilter) return@filter false

            if (trimmed.isEmpty()) return@filter true

            val matchesName = debt.personOrCompany.lowercase().contains(trimmed)
            val matchesPhone = debt.phoneNumber.lowercase().contains(trimmed)
            val matchesNotes = debt.notes.lowercase().contains(trimmed)
            val matchesItems = debt.getItems().any { it.title.lowercase().contains(trimmed) }

            matchesName || matchesPhone || matchesNotes || matchesItems
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: DebtFilter) {
        _selectedFilter.value = filter
    }

    fun addDebt(
        personOrCompany: String,
        phone: String = "",
        notes: String = "",
        initialItemTitle: String? = null,
        initialItemAmount: Double? = null,
        dueDate: Long? = null
    ) {
        viewModelScope.launch {
            val items = mutableListOf<DebtItem>()
            if (!initialItemTitle.isNullOrBlank() && initialItemAmount != null && initialItemAmount > 0) {
                items.add(
                    DebtItem(
                        title = initialItemTitle.trim(),
                        amount = initialItemAmount,
                        quantity = 1
                    )
                )
            }

            val entity = DebtEntity(
                personOrCompany = personOrCompany.trim(),
                phoneNumber = phone.trim(),
                notes = notes.trim(),
                itemsJson = DebtEntity.itemsToJson(items),
                paymentsJson = DebtEntity.paymentsToJson(emptyList()),
                dueDate = dueDate,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertDebt(entity)
        }
    }

    fun updateDebtInfo(
        debtId: Long,
        personOrCompany: String,
        phone: String,
        notes: String,
        dueDate: Long? = null
    ) {
        viewModelScope.launch {
            val existing = repository.getDebtById(debtId) ?: return@launch
            val updated = existing.copy(
                personOrCompany = personOrCompany.trim(),
                phoneNumber = phone.trim(),
                notes = notes.trim(),
                dueDate = dueDate
            )
            repository.updateDebt(updated)
        }
    }

    fun updateDebtDueDate(debtId: Long, dueDate: Long?) {
        viewModelScope.launch {
            val existing = repository.getDebtById(debtId) ?: return@launch
            repository.updateDebt(existing.copy(dueDate = dueDate))
        }
    }

    fun deleteDebt(id: Long) {
        viewModelScope.launch {
            repository.deleteDebt(id)
        }
    }

    fun addItemToDebt(debtId: Long, title: String, amount: Double, quantity: Int = 1) {
        if (title.isBlank() || amount <= 0) return
        viewModelScope.launch {
            val item = DebtItem(
                title = title.trim(),
                amount = amount,
                quantity = if (quantity <= 0) 1 else quantity
            )
            repository.addItemToDebt(debtId, item)
        }
    }

    fun removeItemFromDebt(debtId: Long, itemId: String) {
        viewModelScope.launch {
            repository.removeItemFromDebt(debtId, itemId)
        }
    }

    fun makePayment(debtId: Long, amount: Double, note: String = "") {
        if (amount <= 0) return
        viewModelScope.launch {
            val payment = DebtPayment(
                amount = amount,
                note = note.trim()
            )
            repository.addPaymentToDebt(debtId, payment)
        }
    }

    fun updatePayment(debtId: Long, paymentId: String, newAmount: Double, newNote: String) {
        if (newAmount <= 0) return
        viewModelScope.launch {
            val existing = repository.getDebtById(debtId)?.getPayments()?.find { it.id == paymentId }
            val updated = (existing ?: DebtPayment(id = paymentId, amount = newAmount)).copy(
                amount = newAmount,
                note = newNote.trim()
            )
            repository.updatePaymentInDebt(debtId, updated)
        }
    }

    fun removePayment(debtId: Long, paymentId: String) {
        viewModelScope.launch {
            repository.removePaymentFromDebt(debtId, paymentId)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DebtViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val repo = DebtRepository(db.debtDao())
                return DebtViewModel(application, repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
