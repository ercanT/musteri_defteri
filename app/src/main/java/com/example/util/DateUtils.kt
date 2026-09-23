package com.example.util

import android.app.DatePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DueDateStatus(
    val badgeText: String,
    val isOverdue: Boolean,
    val isDueToday: Boolean,
    val daysDiff: Long,
    val formattedDate: String
)

object DateUtils {

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    fun getDueDateStatus(dueDate: Long?): DueDateStatus? {
        if (dueDate == null || dueDate <= 0) return null

        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dueCal = Calendar.getInstance().apply {
            timeInMillis = dueDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = dueCal.timeInMillis - nowCal.timeInMillis
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)
        val formattedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(dueDate))

        return when {
            diffDays < 0 -> DueDateStatus(
                badgeText = "Vadesi Geçti (${-diffDays} gün önce)",
                isOverdue = true,
                isDueToday = false,
                daysDiff = diffDays,
                formattedDate = formattedDate
            )
            diffDays == 0L -> DueDateStatus(
                badgeText = "Vadesi Bugün!",
                isOverdue = false,
                isDueToday = true,
                daysDiff = 0,
                formattedDate = formattedDate
            )
            else -> DueDateStatus(
                badgeText = "Vade: $formattedDate ($diffDays gün kaldı)",
                isOverdue = false,
                isDueToday = false,
                daysDiff = diffDays,
                formattedDate = formattedDate
            )
        }
    }

    fun showDatePickerDialog(
        context: Context,
        initialDate: Long? = null,
        onDateSelected: (Long) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        if (initialDate != null && initialDate > 0) {
            calendar.timeInMillis = initialDate
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selected.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
