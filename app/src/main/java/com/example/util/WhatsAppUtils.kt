package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppUtils {

    /**
     * Cleans and formats Turkish/international phone numbers into standard format with country code.
     */
    fun cleanPhoneNumber(phoneNumber: String): String {
        val digits = phoneNumber.filter { it.isDigit() }
        return when {
            digits.startsWith("00") -> digits.substring(2)
            digits.startsWith("0") -> "90" + digits.substring(1)
            digits.startsWith("90") -> digits
            digits.length == 10 && digits.startsWith("5") -> "90$digits"
            else -> digits
        }
    }

    /**
     * Builds WhatsApp message for Cargo/Customer card:
     * Includes:
     * - Customer Full Name (Müşteri Adı Soyadı)
     * - Delivery Address (Adres Bilgisi)
     * - Extra Notes (Ekstra Alan Notları)
     */
    fun buildCustomerCargoMessage(
        fullName: String,
        address: String = "",
        extraNotes: String = ""
    ): String {
        val name = fullName.trim()
        val sb = StringBuilder()

        sb.append("Merhaba ${if (name.isNotBlank()) name else "Sayın Müşterimiz"},\n\n")
        sb.append("📦 Kargo & Teslimat Bilgileri:\n")
        sb.append("👤 Müşteri: ${if (name.isNotBlank()) name else "-"}\n")

        if (address.isNotBlank()) {
            sb.append("📍 Adres: ${address.trim()}\n")
        }

        if (extraNotes.isNotBlank()) {
            sb.append("📝 Notlar: ${extraNotes.trim()}\n")
        }

        sb.append("\nİyi çalışmalar / İyi günler dileriz.")
        return sb.toString().trim()
    }

    /**
     * Builds the WhatsApp reminder message for Receivables (Alacaklar).
     * Template requested by user:
     * "Merhaba [Kişi veya Firma Adı], güncel bakiyemiz [Tutar] TL. Gün içinde ödeme yapılıp tarafımıza bilgi veriniz. İyi günler dilerim."
     */
    fun buildReceivableWhatsAppMessage(
        personOrCompany: String,
        remainingBalance: Double,
        notes: String = ""
    ): String {
        val name = personOrCompany.trim()
        val formattedBalance = CurrencyUtils.formatCurrency(if (remainingBalance < 0.001) 0.0 else remainingBalance)

        val sb = StringBuilder()
        if (name.isNotBlank()) {
            sb.append("Merhaba $name,\n\n")
        } else {
            sb.append("Merhaba Sayın İlgili,\n\n")
        }

        sb.append("Güncel bakiyemiz $formattedBalance.\n")
        sb.append("Gün içinde ödeme yapılıp tarafımıza bilgi veriniz.\n")

        if (notes.isNotBlank()) {
            sb.append("\n📝 Not / Açıklama: ${notes.trim()}\n")
        }

        sb.append("\nİyi günler dilerim.")
        return sb.toString().trim()
    }

    /**
     * Builds the WhatsApp notification message sent immediately after recording a payment/collection (Tahsilat).
     */
    fun buildReceivablePaymentNotificationMessage(
        personOrCompany: String,
        paidAmount: Double,
        paymentNote: String,
        remainingBalance: Double,
        receivableNotes: String = ""
    ): String {
        val formattedPaid = CurrencyUtils.formatCurrency(paidAmount)
        val formattedRemaining = CurrencyUtils.formatCurrency(if (remainingBalance < 0.001) 0.0 else remainingBalance)
        val name = personOrCompany.trim()

        val sb = StringBuilder()
        sb.append("Merhaba ${if (name.isNotBlank()) name else "Sayın İlgili"},\n\n")
        sb.append("✅ $formattedPaid tutarında ödemeniz/tahsilatınız alınmıştır.\n")

        if (paymentNote.isNotBlank()) {
            sb.append("📝 Açıklama: ${paymentNote.trim()}\n")
        }

        if (receivableNotes.isNotBlank()) {
            sb.append("📌 Alacak Notu: ${receivableNotes.trim()}\n")
        }

        if (remainingBalance <= 0.001) {
            sb.append("🎉 Kalan Bakiyeniz: 0,00 ₺ (Hesap tamamen kapanmıştır)\n\n")
            sb.append("Teşekkür eder, iyi günler dileriz.")
        } else {
            sb.append("📊 Güncel Kalan Bakiyeniz: $formattedRemaining\n\n")
            sb.append("Teşekkür eder, iyi günler dileriz.")
        }

        return sb.toString()
    }

    /**
     * Builds the WhatsApp notification message sent immediately after recording a payment.
     * Includes:
     * - Paid amount
     * - Payment note / explanation (if provided)
     * - Extra debt notes (if present)
     * - Current remaining balance
     */
    fun buildPaymentNotificationMessage(
        personOrCompany: String,
        paidAmount: Double,
        paymentNote: String,
        remainingBalance: Double,
        debtNotes: String = ""
    ): String {
        val formattedPaid = CurrencyUtils.formatCurrency(paidAmount)
        val formattedRemaining = CurrencyUtils.formatCurrency(if (remainingBalance < 0.001) 0.0 else remainingBalance)
        val name = personOrCompany.trim()

        val sb = StringBuilder()
        sb.append("Merhaba ${if (name.isNotBlank()) name else "Sayın İlgili"},\n\n")
        sb.append("✅ $formattedPaid tutarında ödemeniz yapılmıştır.\n")

        if (paymentNote.isNotBlank()) {
            sb.append("📝 Ödeme Açıklaması: ${paymentNote.trim()}\n")
        }

        if (debtNotes.isNotBlank()) {
            sb.append("📌 Ekstra Not / Açıklama: ${debtNotes.trim()}\n")
        }

        if (remainingBalance <= 0.001) {
            sb.append("🎉 Kalan Bakiyemiz: 0,00 ₺ (Borç tamamen kapanmıştır)\n\n")
            sb.append("Teşekkür eder, iyi çalışmalar dileriz.")
        } else {
            sb.append("📊 Güncel Kalan Bakiyemiz: $formattedRemaining\n\n")
            sb.append("İyi çalışmalar dileriz.")
        }

        return sb.toString()
    }

    /**
     * Builds a general account summary WhatsApp message for a debt record.
     * Includes:
     * - Customer / Company name
     * - Ekstra Alan / Notlar (if present)
     * - List of purchased items/materials (if any)
     * - Total, Paid, and Remaining balance
     */
    fun buildDebtSummaryMessage(
        personOrCompany: String,
        totalItemsAmount: Double,
        totalPaidAmount: Double,
        remainingBalance: Double,
        notes: String = "",
        items: List<com.example.data.DebtItem> = emptyList()
    ): String {
        val name = personOrCompany.trim()
        val formattedTotal = CurrencyUtils.formatCurrency(totalItemsAmount)
        val formattedPaid = CurrencyUtils.formatCurrency(totalPaidAmount)
        val formattedRemaining = CurrencyUtils.formatCurrency(if (remainingBalance < 0.001) 0.0 else remainingBalance)

        val sb = StringBuilder()
        sb.append("Merhaba ${if (name.isNotBlank()) name else "Sayın İlgili"},\n\n")
        sb.append("📋 Malzeme ve bakiye durum özeti:\n")

        // Include the extra field notes
        if (notes.isNotBlank()) {
            sb.append("📝 Ekstra Not / Açıklama:\n${notes.trim()}\n\n")
        }

        // Include item details if available
        if (items.isNotEmpty()) {
            sb.append("📦 Alınan Malzemeler:\n")
            for (item in items) {
                if (item.quantity > 1) {
                    sb.append("• ${item.title} (${item.quantity} adet): ${CurrencyUtils.formatCurrency(item.totalLineAmount)}\n")
                } else {
                    sb.append("• ${item.title}: ${CurrencyUtils.formatCurrency(item.amount)}\n")
                }
            }
            sb.append("\n")
        }

        sb.append("• Toplam Malzemeler: $formattedTotal\n")
        sb.append("• Yapılan Ödemeler: $formattedPaid\n")
        if (remainingBalance <= 0.001) {
            sb.append("• Kalan Bakiye: 0,00 ₺ (Kapanmıştır)\n\n")
            sb.append("Teşekkür eder, iyi çalışmalar dileriz.")
        } else {
            sb.append("• Güncel Kalan Bakiye: $formattedRemaining\n\n")
            sb.append("İyi çalışmalar dileriz.")
        }
        return sb.toString()
    }

    /**
     * Builds a WhatsApp message for sending specifically the extra note / description.
     */
    fun buildDebtNoteOnlyMessage(
        personOrCompany: String,
        notes: String,
        remainingBalance: Double? = null
    ): String {
        val name = personOrCompany.trim()
        val sb = StringBuilder()
        sb.append("Merhaba ${if (name.isNotBlank()) name else "Sayın İlgili"},\n\n")
        sb.append("📝 Ekstra Not / Açıklama:\n${notes.trim()}\n\n")
        if (remainingBalance != null) {
            val formatted = CurrencyUtils.formatCurrency(if (remainingBalance < 0.001) 0.0 else remainingBalance)
            sb.append("📊 Güncel Kalan Bakiye: $formatted\n\n")
        }
        sb.append("İyi çalışmalar dileriz.")
        return sb.toString()
    }

    /**
     * Opens WhatsApp with a specified message.
     * If phoneNumber is provided: opens chat directly.
     * If phoneNumber is empty: opens WhatsApp contact/chat picker via ACTION_SEND.
     */
    fun openWhatsApp(context: Context, phoneNumber: String, message: String): Boolean {
        val cleanNumber = cleanPhoneNumber(phoneNumber)
        val encodedText = Uri.encode(message)

        if (cleanNumber.isBlank()) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                setPackage("com.whatsapp")
            }
            return try {
                context.startActivity(sendIntent)
                true
            } catch (e: Exception) {
                try {
                    val w4bIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                        setPackage("com.whatsapp.w4b")
                    }
                    context.startActivity(w4bIntent)
                    true
                } catch (e2: Exception) {
                    try {
                        val chooser = Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message)
                            },
                            "WhatsApp ile Gönder"
                        )
                        context.startActivity(chooser)
                        true
                    } catch (e3: Exception) {
                        Toast.makeText(context, "WhatsApp açılamadı", Toast.LENGTH_SHORT).show()
                        false
                    }
                }
            }
        }

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedText")

        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            val pm = context.packageManager

            val isW4bInstalled = try {
                pm.getPackageInfo("com.whatsapp.w4b", 0)
                true
            } catch (e: Exception) {
                false
            }

            if (isW4bInstalled) {
                intent.setPackage("com.whatsapp.w4b")
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanNumber?text=$encodedText"))
                context.startActivity(fallbackIntent)
                true
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp açılamadı", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
}
