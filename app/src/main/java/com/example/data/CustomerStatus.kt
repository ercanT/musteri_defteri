package com.example.data

enum class CustomerStatus(
    val label: String,
    val priority: Int
) {
    PARASI_GELDI("Parası Geldi", 1),
    HAZIRLANIYOR("Hazırlanıyor", 2),
    TAMAMLANDI("Tamamlandı", 3);

    companion object {
        fun fromString(value: String): CustomerStatus {
            return entries.find { 
                it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) 
            } ?: HAZIRLANIYOR
        }
    }
}
