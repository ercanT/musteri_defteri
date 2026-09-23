package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CustomerEntity
import com.example.data.CustomerRepository
import com.example.data.CustomerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatusCounts(
    val total: Int = 0,
    val parasiGeldi: Int = 0,
    val hazirlaniyor: Int = 0,
    val tamamlandi: Int = 0
)

class CustomerViewModel(
    application: Application,
    private val repository: CustomerRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<CustomerStatus?>(CustomerStatus.PARASI_GELDI)
    val selectedStatusFilter: StateFlow<CustomerStatus?> = _selectedStatusFilter.asStateFlow()

    // Base raw customer flow
    private val rawCustomers = repository.allCustomers

    // Counts across all records
    val statusCounts: StateFlow<StatusCounts> = rawCustomers
        .combine(MutableStateFlow(Unit)) { list, _ ->
            var paid = 0
            var preparing = 0
            var completed = 0
            for (item in list) {
                when (CustomerStatus.fromString(item.status)) {
                    CustomerStatus.PARASI_GELDI -> paid++
                    CustomerStatus.HAZIRLANIYOR -> preparing++
                    CustomerStatus.TAMAMLANDI -> completed++
                }
            }
            StatusCounts(
                total = list.size,
                parasiGeldi = paid,
                hazirlaniyor = preparing,
                tamamlandi = completed
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatusCounts()
        )

    // Filtered and strictly sorted customer list
    val customerList: StateFlow<List<CustomerEntity>> = combine(
        rawCustomers,
        _searchQuery,
        _selectedStatusFilter
    ) { list, query, filter ->
        var result = list
        
        // Filter by search query if present
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { customer ->
                customer.fullName.lowercase().contains(q) ||
                customer.phoneNumber.lowercase().contains(q) ||
                customer.address.lowercase().contains(q) ||
                customer.extraNotes.lowercase().contains(q)
            }
        }

        // Filter by selected status if any
        if (filter != null) {
            result = result.filter { customer ->
                CustomerStatus.fromString(customer.status) == filter
            }
        }

        // Enforce strict priority sorting:
        // 1. Favoriler EN ÜSTTE (Tüm statülerin üstünde, benim için önemli)
        // 2. Parası Geldi (favori olmayanlar arasında 1.)
        // 3. Hazırlanıyor (ortada)
        // 4. Tamamlandı (en altta)
        // Secondary sort: id DESC
        result.sortedWith(
            compareByDescending<CustomerEntity> { it.isFavorite }
                .thenBy { customer ->
                    when (CustomerStatus.fromString(customer.status)) {
                        CustomerStatus.PARASI_GELDI -> 1
                        CustomerStatus.HAZIRLANIYOR -> 2
                        CustomerStatus.TAMAMLANDI -> 3
                    }
                }
                .thenByDescending { it.id }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onStatusFilterChange(filter: CustomerStatus?) {
        _selectedStatusFilter.value = filter
    }

    fun toggleFavorite(id: Int, currentFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateFavorite(id, !currentFavorite)
        }
    }

    fun addCustomer(
        fullName: String,
        phoneNumber: String,
        address: String,
        extraNotes: String,
        status: CustomerStatus,
        amount: Double = 0.0,
        isFavorite: Boolean = false
    ) {
        viewModelScope.launch {
            val entity = CustomerEntity(
                fullName = fullName.trim(),
                phoneNumber = phoneNumber.trim(),
                address = address.trim(),
                extraNotes = extraNotes.trim(),
                status = status.name,
                amount = amount,
                isFavorite = isFavorite
            )
            repository.insertCustomer(entity)
        }
    }

    fun updateCustomer(
        id: Int,
        fullName: String,
        phoneNumber: String,
        address: String,
        extraNotes: String,
        status: CustomerStatus,
        amount: Double = 0.0,
        isFavorite: Boolean = false
    ) {
        viewModelScope.launch {
            val entity = CustomerEntity(
                id = id,
                fullName = fullName.trim(),
                phoneNumber = phoneNumber.trim(),
                address = address.trim(),
                extraNotes = extraNotes.trim(),
                status = status.name,
                amount = amount,
                isFavorite = isFavorite
            )
            repository.updateCustomer(entity)
        }
    }

    fun updateStatus(id: Int, status: CustomerStatus) {
        viewModelScope.launch {
            repository.updateStatus(id, status)
        }
    }

    fun deleteCustomer(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomerById(id)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(application)
            val repo = CustomerRepository(db.customerDao())
            return CustomerViewModel(application, repo) as T
        }
    }
}
