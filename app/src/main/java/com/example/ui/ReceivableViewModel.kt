package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ReceivableEntity
import com.example.data.ReceivableItem
import com.example.data.ReceivablePayment
import com.example.data.ReceivableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReceivableFilter {
    ALL,
    UNPAID,   // Açık Alacaklar (Kalan Bakiye > 0)
    DUE_DATE, // Vadeliler
    PAID      // Tahsil Edilenler / Kapanmış (Kalan Bakiye <= 0)
}

data class ReceivableSummary(
    val totalItemsAmount: Double = 0.0,
    val totalPaidAmount: Double = 0.0,
    val totalRemainingBalance: Double = 0.0,
    val openReceivableCount: Int = 0,
    val paidOffCount: Int = 0,
    val totalCount: Int = 0,
    val dueCount: Int = 0
)

class ReceivableViewModel(
    application: Application,
    private val repository: ReceivableRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ReceivableFilter.UNPAID)
    val selectedFilter: StateFlow<ReceivableFilter> = _selectedFilter.asStateFlow()

    val rawReceivables = repository.allReceivables

    val summary: StateFlow<ReceivableSummary> = rawReceivables
        .combine(MutableStateFlow(Unit)) { list, _ ->
            var totalItems = 0.0
            var totalPaid = 0.0
            var totalBalance = 0.0
            var openCount = 0
            var paidCount = 0
            var dueCount = 0

            for (receivable in list) {
                val itemsSum = receivable.calculateTotalAmount()
                val paidSum = receivable.calculateTotalPaid()
                val balance = receivable.calculateRemainingBalance()

                totalItems += itemsSum
                totalPaid += paidSum
                totalBalance += balance

                if (balance > 0.001) {
                    openCount++
                    if (receivable.dueDate != null) {
                        dueCount++
                    }
                } else {
                    paidCount++
                }
            }

            ReceivableSummary(
                totalItemsAmount = totalItems,
                totalPaidAmount = totalPaid,
                totalRemainingBalance = totalBalance,
                openReceivableCount = openCount,
                paidOffCount = paidCount,
                totalCount = list.size,
                dueCount = dueCount
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReceivableSummary()
        )

    val receivables: StateFlow<List<ReceivableEntity>> = combine(
        rawReceivables,
        _searchQuery,
        _selectedFilter
    ) { list, query, filter ->
        val trimmed = query.trim().lowercase()

        list.filter { receivable ->
            val matchesFilter = when (filter) {
                ReceivableFilter.ALL -> true
                ReceivableFilter.UNPAID -> receivable.calculateRemainingBalance() > 0.001
                ReceivableFilter.DUE_DATE -> receivable.dueDate != null && receivable.calculateRemainingBalance() > 0.001
                ReceivableFilter.PAID -> receivable.calculateRemainingBalance() <= 0.001
            }

            val matchesQuery = if (trimmed.isBlank()) {
                true
            } else {
                val nameMatch = receivable.personOrCompany.lowercase().contains(trimmed)
                val phoneMatch = receivable.phoneNumber.filter { it.isDigit() }
                    .contains(trimmed.filter { it.isDigit() })
                val notesMatch = receivable.notes.lowercase().contains(trimmed)
                val itemsMatch = receivable.getItems().any { it.title.lowercase().contains(trimmed) }
                nameMatch || phoneMatch || notesMatch || itemsMatch
            }

            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: ReceivableFilter) {
        _selectedFilter.value = filter
    }

    fun addReceivable(
        personOrCompany: String,
        phoneNumber: String,
        notes: String,
        initialItemTitle: String? = null,
        initialItemAmount: Double? = null,
        dueDate: Long? = null
    ) {
        viewModelScope.launch {
            val initialItems = mutableListOf<ReceivableItem>()
            if (!initialItemTitle.isNullOrBlank() && initialItemAmount != null && initialItemAmount > 0) {
                initialItems.add(
                    ReceivableItem(
                        title = initialItemTitle.trim(),
                        amount = initialItemAmount,
                        quantity = 1
                    )
                )
            }

            val entity = ReceivableEntity(
                personOrCompany = personOrCompany.trim(),
                phoneNumber = phoneNumber.trim(),
                notes = notes.trim(),
                itemsJson = ReceivableEntity.itemsToJson(initialItems),
                paymentsJson = "[]",
                dueDate = dueDate
            )
            repository.insertReceivable(entity)
        }
    }

    fun updateReceivableInfo(
        receivableId: Long,
        personOrCompany: String,
        phoneNumber: String,
        notes: String,
        dueDate: Long? = null
    ) {
        viewModelScope.launch {
            val current = repository.getReceivableById(receivableId) ?: return@launch
            repository.updateReceivable(
                current.copy(
                    personOrCompany = personOrCompany.trim(),
                    phoneNumber = phoneNumber.trim(),
                    notes = notes.trim(),
                    dueDate = dueDate
                )
            )
        }
    }

    fun updateReceivableDueDate(receivableId: Long, dueDate: Long?) {
        viewModelScope.launch {
            val current = repository.getReceivableById(receivableId) ?: return@launch
            repository.updateReceivable(current.copy(dueDate = dueDate))
        }
    }

    fun deleteReceivable(receivable: ReceivableEntity) {
        viewModelScope.launch {
            repository.deleteReceivable(receivable)
        }
    }

    fun addItemToReceivable(
        receivableId: Long,
        title: String,
        amount: Double,
        quantity: Int = 1
    ) {
        viewModelScope.launch {
            val item = ReceivableItem(
                title = title.trim(),
                amount = amount,
                quantity = if (quantity < 1) 1 else quantity
            )
            repository.addItemToReceivable(receivableId, item)
        }
    }

    fun removeItemFromReceivable(receivableId: Long, itemId: String) {
        viewModelScope.launch {
            repository.removeItemFromReceivable(receivableId, itemId)
        }
    }

    fun makePayment(
        receivableId: Long,
        amount: Double,
        note: String = "",
        updatedPhoneNumber: String? = null
    ) {
        viewModelScope.launch {
            val current = repository.getReceivableById(receivableId) ?: return@launch

            if (!updatedPhoneNumber.isNullOrBlank() && updatedPhoneNumber.trim() != current.phoneNumber) {
                repository.updateReceivable(
                    current.copy(phoneNumber = updatedPhoneNumber.trim())
                )
            }

            val payment = ReceivablePayment(
                amount = amount,
                note = note.trim()
            )
            repository.addPaymentToReceivable(receivableId, payment)
        }
    }

    fun updatePayment(
        receivableId: Long,
        updatedPayment: ReceivablePayment
    ) {
        viewModelScope.launch {
            repository.updatePaymentInReceivable(receivableId, updatedPayment)
        }
    }

    fun removePaymentFromReceivable(receivableId: Long, paymentId: String) {
        viewModelScope.launch {
            repository.removePaymentFromReceivable(receivableId, paymentId)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReceivableViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val repo = ReceivableRepository(db.receivableDao())
                return ReceivableViewModel(application, repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
