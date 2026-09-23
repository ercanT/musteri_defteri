package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    private val turkishSymbols = DecimalFormatSymbols(Locale("tr", "TR")).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    }

    private val currencyFormat = DecimalFormat("#,##0.00", turkishSymbols)

    /**
     * KDV Hariç Tutarı Hesaplar (%20 KDV üzerinden)
     * Örn: 1000 TL KDV dahil -> 1000 / 1.20 = 833.33 TL
     */
    fun calculateKdvHaric(kdvDahil: Double): Double {
        if (kdvDahil <= 0.0) return 0.0
        return kdvDahil / 1.20
    }

    /**
     * KDV Tutarını Hesaplar (%20 KDV üzerinden)
     * Örn: 1000 TL KDV dahil -> 1000 - 833.33 = 166.67 TL
     */
    fun calculateKdvTutari(kdvDahil: Double): Double {
        if (kdvDahil <= 0.0) return 0.0
        return kdvDahil - calculateKdvHaric(kdvDahil)
    }

    /**
     * Tutarı Türk Lirası formatında döndürür (Örn: "1.000,00 ₺")
     */
    fun formatCurrency(amount: Double): String {
        return "${currencyFormat.format(amount)} ₺"
    }

    /**
     * Metin girişini Double'a parse eder (virgül veya nokta destekli)
     */
    fun parseAmount(input: String): Double {
        val clean = input.trim().replace(" ", "").replace(",", ".")
        return clean.toDoubleOrNull() ?: 0.0
    }
}
