package com.example.data

import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val customerDao: CustomerDao) {

    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> {
        return if (query.isBlank()) {
            customerDao.getAllCustomers()
        } else {
            customerDao.searchCustomers(query.trim())
        }
    }

    suspend fun getCustomerById(id: Int): CustomerEntity? = customerDao.getCustomerById(id)

    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)

    suspend fun updateStatus(id: Int, status: CustomerStatus) {
        customerDao.updateStatus(id, status.name)
    }

    suspend fun updateFavorite(id: Int, isFavorite: Boolean) {
        customerDao.updateFavorite(id, isFavorite)
    }

    suspend fun seedSampleIfEmpty() {
        // Will check if empty and seed 3 sample customers if so
    }

    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    suspend fun deleteCustomerById(id: Int) = customerDao.deleteCustomerById(id)
}
