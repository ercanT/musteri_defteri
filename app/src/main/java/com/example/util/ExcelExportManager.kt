package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.CustomerEntity
import com.example.data.DebtEntity
import com.example.data.ReceivableEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExportManager {

    private const val BOM = "\uFEFF"
    private const val DELIMITER = ";"

    private fun escapeCell(value: String): String {
        var str = value.replace("\r", " ").replace("\n", " ")
        if (str.contains(DELIMITER) || str.contains("\"") || str.contains(",")) {
            str = "\"" + str.replace("\"", "\"\"") + "\""
        }
        return str
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    private fun formatDateOnly(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0) return "-"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    private fun formatAmount(amount: Double): String {
        return String.format(Locale("tr", "TR"), "%.2f", amount)
    }

    fun exportCustomersToCsv(customers: List<CustomerEntity>): String {
        val sb = StringBuilder()
        sb.append(BOM)
        sb.append("Müşteri Adı;Telefon;Durum;Tutar (TL);Adres;Açıklama / Not;Kayıt Tarihi\n")

        for (c in customers) {
            val line = listOf(
                escapeCell(c.fullName),
                escapeCell(c.phoneNumber),
                escapeCell(c.status),
                escapeCell(formatAmount(c.amount)),
                escapeCell(c.address),
                escapeCell(c.extraNotes),
                escapeCell(formatDate(c.createdAt))
            ).joinToString(DELIMITER)
            sb.append(line).append("\n")
        }
        return sb.toString()
    }

    fun exportDebtsToCsv(debts: List<DebtEntity>): String {
        val sb = StringBuilder()
        sb.append(BOM)
        sb.append("Kişi veya Firma;Telefon;Notlar;Alınan Malzemeler;Toplam Borç (TL);Ödenen (TL);Kalan Borç (TL);Vade Tarihi;Kayıt Tarihi\n")

        for (d in debts) {
            val itemsSummary = d.getItems().joinToString(", ") { "${it.title} (${it.quantity}x ${formatAmount(it.amount)} TL)" }
            val line = listOf(
                escapeCell(d.personOrCompany),
                escapeCell(d.phoneNumber),
                escapeCell(d.notes),
                escapeCell(itemsSummary),
                escapeCell(formatAmount(d.calculateTotalAmount())),
                escapeCell(formatAmount(d.calculateTotalPaid())),
                escapeCell(formatAmount(d.calculateRemainingBalance())),
                escapeCell(formatDateOnly(d.dueDate)),
                escapeCell(formatDate(d.createdAt))
            ).joinToString(DELIMITER)
            sb.append(line).append("\n")
        }
        return sb.toString()
    }

    fun exportReceivablesToCsv(receivables: List<ReceivableEntity>): String {
        val sb = StringBuilder()
        sb.append(BOM)
        sb.append("Kişi veya Firma;Telefon;Notlar;İşler ve Hizmetler;Toplam Alacak (TL);Tahsil Edilen (TL);Kalan Alacak (TL);Vade Tarihi;Kayıt Tarihi\n")

        for (r in receivables) {
            val itemsSummary = r.getItems().joinToString(", ") { "${it.title} (${it.quantity}x ${formatAmount(it.amount)} TL)" }
            val line = listOf(
                escapeCell(r.personOrCompany),
                escapeCell(r.phoneNumber),
                escapeCell(r.notes),
                escapeCell(itemsSummary),
                escapeCell(formatAmount(r.calculateTotalAmount())),
                escapeCell(formatAmount(r.calculateTotalPaid())),
                escapeCell(formatAmount(r.calculateRemainingBalance())),
                escapeCell(formatDateOnly(r.dueDate)),
                escapeCell(formatDate(r.createdAt))
            ).joinToString(DELIMITER)
            sb.append(line).append("\n")
        }
        return sb.toString()
    }

    fun exportCompleteReportToCsv(
        customers: List<CustomerEntity>,
        debts: List<DebtEntity>,
        receivables: List<ReceivableEntity>
    ): String {
        val sb = StringBuilder()
        sb.append(BOM)

        // GENEL MALİ DURUM ÖZETİ
        val totalDebt = debts.sumOf { it.calculateTotalAmount() }
        val totalDebtPaid = debts.sumOf { it.calculateTotalPaid() }
        val remainingDebt = debts.sumOf { it.calculateRemainingBalance() }

        val totalReceivable = receivables.sumOf { it.calculateTotalAmount() }
        val totalReceivableCollected = receivables.sumOf { it.calculateTotalPaid() }
        val remainingReceivable = receivables.sumOf { it.calculateRemainingBalance() }

        sb.append("=== MÜŞTERİ DEFTERİ GENEL HESAP VE RAPOR TABLOSU ===;;\n")
        sb.append("Rapor Tarihi:;${formatDate(System.currentTimeMillis())};\n\n")

        sb.append("GENEL FİNANSAL ÖZET;;\n")
        sb.append("Toplam Borç:;${formatAmount(totalDebt)} TL;\n")
        sb.append("Ödenen Borç:;${formatAmount(totalDebtPaid)} TL;\n")
        sb.append("Kalan Borç Bakiyesi:;${formatAmount(remainingDebt)} TL;\n")
        sb.append("Toplam Alacak:;${formatAmount(totalReceivable)} TL;\n")
        sb.append("Tahsil Edilen Alacak:;${formatAmount(totalReceivableCollected)} TL;\n")
        sb.append("Kalan Alacak Bakiyesi:;${formatAmount(remainingReceivable)} TL;\n")
        sb.append("Net Durum (Alacak - Borç):;${formatAmount(remainingReceivable - remainingDebt)} TL;\n\n")

        // 1. KARGOLAR BÖLÜMÜ
        sb.append("--- KARGOLAR VE SİPARİŞLER (Toplam: ${customers.size}) ---;;;;;;\n")
        sb.append("Müşteri Adı;Telefon;Durum;Tutar (TL);Adres;Açıklama / Not;Kayıt Tarihi\n")
        for (c in customers) {
            val line = listOf(
                escapeCell(c.fullName),
                escapeCell(c.phoneNumber),
                escapeCell(c.status),
                escapeCell(formatAmount(c.amount)),
                escapeCell(c.address),
                escapeCell(c.extraNotes),
                escapeCell(formatDate(c.createdAt))
            ).joinToString(DELIMITER)
            sb.append(line).append("\n")
        }
        sb.append("\n\n")

        // 2. BORÇLARIM BÖLÜMÜ
        sb.append("--- BORÇLARIM (Toplam: ${debts.size}) ---;;;;;;;;\n")
        sb.append("Kişi veya Firma;Telefon;Notlar;Alınan Malzemeler;Toplam Borç (TL);Ödenen (TL);Kalan Borç (TL);Vade Tarihi;Kayıt Tarihi\n")
        for (d in debts) {
            val itemsSummary = d.getItems().joinToString(", ") { "${it.title} (${it.quantity}x ${formatAmount(it.amount)} TL)" }
            val line = listOf(
                escapeCell(d.personOrCompany),
                escapeCell(d.phoneNumber),
                escapeCell(d.notes),
                escapeCell(itemsSummary),
                escapeCell(formatAmount(d.calculateTotalAmount())),
                escapeCell(formatAmount(d.calculateTotalPaid())),
                escapeCell(formatAmount(d.calculateRemainingBalance())),
                escapeCell(formatDateOnly(d.dueDate)),
                escapeCell(formatDate(d.createdAt))
            ).joinToString(DELIMITER)
            sb.append(line).append("\n")
        }
        sb.append("\n\n")

        // 3. ALACAKLARIM BÖLÜMÜ
        sb.append("--- ALACAKLARIM (Toplam: ${receivables.size}) ---;;;;;;;;\n")
        sb.append("Kişi veya Firma;Telefon;Notlar;İşler ve Hizmetler;Toplam Alacak (TL);Tahsil Edilen (TL);Kalan Alacak (TL);Vade Tarihi;Kayıt Tarihi\n")
        for (r in receivables) {
            val itemsSummary = r.getItems().joinToString(", ") { "${it.title} (${it.quantity}x ${formatAmount(it.amount)} TL)" }
            val line = listOf(
                escapeCell(r.personOrCompany),
                escapeCell(r.phoneNumber),
                escapeCell(r.notes),
                escapeCell(itemsSummary),
                escapeCell(formatAmount(r.calculateTotalAmount())),
                escapeCell(formatAmount(r.calculateTotalPaid())),
                escapeCell(formatAmount(r.calculateRemainingBalance())),
                escapeCell(formatDateOnly(r.dueDate)),
                escapeCell(formatDate(r.createdAt))
            ).joinToString(DELIMITER)
            sb.append(line).append("\n")
        }

        return sb.toString()
    }

    suspend fun shareCsvFile(
        context: Context,
        filenamePrefix: String,
        csvContent: String,
        subject: String
    ): Intent = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val fileName = "${filenamePrefix}_${dateFormat.format(Date())}.csv"

        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { it.write(csvContent.toByteArray(Charsets.UTF_8)) }

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "Excel ile açılabilir Müşteri Defteri tablosu ekte yer almaktadır.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Intent.createChooser(shareIntent, "Excel / CSV Dosyasını Paylaş")
    }

    suspend fun saveCsvToDownloads(
        context: Context,
        filenamePrefix: String,
        csvContent: String
    ): Boolean = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val fileName = "${filenamePrefix}_${dateFormat.format(Date())}.csv"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MusteriDefteri")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext false

                resolver.openOutputStream(uri)?.use { stream: OutputStream ->
                    stream.write(csvContent.toByteArray(Charsets.UTF_8))
                } ?: return@withContext false

                true
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "MusteriDefteri")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, fileName)
                FileOutputStream(targetFile).use { stream ->
                    stream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
