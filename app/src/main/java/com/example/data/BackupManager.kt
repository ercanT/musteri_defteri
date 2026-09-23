package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RestoreMode {
    MERGE,   // Mevcut kayıtları koru, yedekten yenileri ekle
    REPLACE  // Mevcut tüm kayıtları temizle ve yedeği yükle
}

data class BackupSummary(
    val isValid: Boolean,
    val customerCount: Int = 0,
    val debtCount: Int = 0,
    val receivableCount: Int = 0,
    val backupDateFormatted: String = "",
    val errorMessage: String? = null
)

data class RestoreResult(
    val success: Boolean,
    val message: String,
    val customerCount: Int = 0,
    val debtCount: Int = 0,
    val receivableCount: Int = 0
)

class BackupManager(private val database: AppDatabase) {

    private val customerDao = database.customerDao()
    private val debtDao = database.debtDao()
    private val receivableDao = database.receivableDao()

    /**
     * Tüm müşteri, borç ve alacak kayıtlarını tek bir JSON formatında toplar.
     */
    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val customers = customerDao.getAllCustomersList()
        val debts = debtDao.getAllDebtsList()
        val receivables = receivableDao.getAllReceivablesList()

        val rootJson = JSONObject()
        rootJson.put("app", "MusteriDefteri")
        rootJson.put("version", 1)
        val now = System.currentTimeMillis()
        rootJson.put("backupDate", now)
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        rootJson.put("backupDateFormatted", dateFormat.format(Date(now)))

        // 1. Müşteriler (Kargolar)
        val customersArray = JSONArray()
        customers.forEach { customer ->
            val obj = JSONObject().apply {
                put("id", customer.id)
                put("fullName", customer.fullName)
                put("phoneNumber", customer.phoneNumber)
                put("address", customer.address)
                put("extraNotes", customer.extraNotes)
                put("status", customer.status)
                put("amount", customer.amount)
                put("isFavorite", customer.isFavorite)
                put("createdAt", customer.createdAt)
            }
            customersArray.put(obj)
        }
        rootJson.put("customers", customersArray)

        // 2. Borçlarım
        val debtsArray = JSONArray()
        debts.forEach { debt ->
            val obj = JSONObject().apply {
                put("id", debt.id)
                put("personOrCompany", debt.personOrCompany)
                put("phoneNumber", debt.phoneNumber)
                put("notes", debt.notes)
                put("itemsJson", debt.itemsJson)
                put("paymentsJson", debt.paymentsJson)
                if (debt.dueDate != null) {
                    put("dueDate", debt.dueDate)
                }
                put("createdAt", debt.createdAt)
                put("updatedAt", debt.updatedAt)
            }
            debtsArray.put(obj)
        }
        rootJson.put("debts", debtsArray)

        // 3. Alacaklarım
        val receivablesArray = JSONArray()
        receivables.forEach { receivable ->
            val obj = JSONObject().apply {
                put("id", receivable.id)
                put("personOrCompany", receivable.personOrCompany)
                put("phoneNumber", receivable.phoneNumber)
                put("notes", receivable.notes)
                put("itemsJson", receivable.itemsJson)
                put("paymentsJson", receivable.paymentsJson)
                if (receivable.dueDate != null) {
                    put("dueDate", receivable.dueDate)
                }
                put("createdAt", receivable.createdAt)
                put("updatedAt", receivable.updatedAt)
            }
            receivablesArray.put(obj)
        }
        rootJson.put("receivables", receivablesArray)

        rootJson.toString(2)
    }

    /**
     * Seçilen yedek JSON verisini inceler ve içerisindeki kayıt sayılarını döner.
     */
    fun inspectBackup(jsonString: String): BackupSummary {
        return try {
            val root = JSONObject(jsonString)
            val app = root.optString("app", "")
            if (app != "MusteriDefteri" && !root.has("customers") && !root.has("debts") && !root.has("receivables")) {
                return BackupSummary(
                    isValid = false,
                    errorMessage = "Geçersiz yedek dosyası formatı. Bu dosya Müşteri Defteri yedeği değil."
                )
            }

            val customersArray = root.optJSONArray("customers") ?: JSONArray()
            val debtsArray = root.optJSONArray("debts") ?: JSONArray()
            val receivablesArray = root.optJSONArray("receivables") ?: JSONArray()
            val dateFormatted = root.optString("backupDateFormatted", "Bilinmeyen Tarih")

            BackupSummary(
                isValid = true,
                customerCount = customersArray.length(),
                debtCount = debtsArray.length(),
                receivableCount = receivablesArray.length(),
                backupDateFormatted = dateFormatted
            )
        } catch (e: Exception) {
            BackupSummary(
                isValid = false,
                errorMessage = "Dosya okunamadı: ${e.localizedMessage ?: "Bilinmeyen hata"}"
            )
        }
    }

    /**
     * JSON metninden veritabanını geri yükler.
     */
    suspend fun restoreFromJson(jsonString: String, mode: RestoreMode): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val customersArray = root.optJSONArray("customers") ?: JSONArray()
            val debtsArray = root.optJSONArray("debts") ?: JSONArray()
            val receivablesArray = root.optJSONArray("receivables") ?: JSONArray()

            val customersToInsert = mutableListOf<CustomerEntity>()
            for (i in 0 until customersArray.length()) {
                val obj = customersArray.getJSONObject(i)
                customersToInsert.add(
                    CustomerEntity(
                        id = if (mode == RestoreMode.REPLACE) obj.optInt("id", 0) else 0,
                        fullName = obj.optString("fullName", ""),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        address = obj.optString("address", ""),
                        extraNotes = obj.optString("extraNotes", ""),
                        status = obj.optString("status", CustomerStatus.HAZIRLANIYOR.name),
                        amount = obj.optDouble("amount", 0.0),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val debtsToInsert = mutableListOf<DebtEntity>()
            for (i in 0 until debtsArray.length()) {
                val obj = debtsArray.getJSONObject(i)
                debtsToInsert.add(
                    DebtEntity(
                        id = if (mode == RestoreMode.REPLACE) obj.optLong("id", 0) else 0L,
                        personOrCompany = obj.optString("personOrCompany", ""),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        notes = obj.optString("notes", ""),
                        itemsJson = obj.optString("itemsJson", "[]"),
                        paymentsJson = obj.optString("paymentsJson", "[]"),
                        dueDate = if (obj.has("dueDate") && !obj.isNull("dueDate")) obj.optLong("dueDate") else null,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val receivablesToInsert = mutableListOf<ReceivableEntity>()
            for (i in 0 until receivablesArray.length()) {
                val obj = receivablesArray.getJSONObject(i)
                receivablesToInsert.add(
                    ReceivableEntity(
                        id = if (mode == RestoreMode.REPLACE) obj.optLong("id", 0) else 0L,
                        personOrCompany = obj.optString("personOrCompany", ""),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        notes = obj.optString("notes", ""),
                        itemsJson = obj.optString("itemsJson", "[]"),
                        paymentsJson = obj.optString("paymentsJson", "[]"),
                        dueDate = if (obj.has("dueDate") && !obj.isNull("dueDate")) obj.optLong("dueDate") else null,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            // Geri yükleme işlemi
            if (mode == RestoreMode.REPLACE) {
                customerDao.clearAllCustomers()
                debtDao.clearAllDebts()
                receivableDao.clearAllReceivables()
            }

            if (customersToInsert.isNotEmpty()) {
                customerDao.insertAll(customersToInsert)
            }
            if (debtsToInsert.isNotEmpty()) {
                debtDao.insertAll(debtsToInsert)
            }
            if (receivablesToInsert.isNotEmpty()) {
                receivableDao.insertAll(receivablesToInsert)
            }

            RestoreResult(
                success = true,
                message = "Yedek başarıyla yüklendi.",
                customerCount = customersToInsert.size,
                debtCount = debtsToInsert.size,
                receivableCount = receivablesToInsert.size
            )
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                message = "Yükleme hatası: ${e.localizedMessage ?: "Bilinmeyen hata"}"
            )
        }
    }

    /**
     * Yedek dosyasını cache dizininde oluşturup WhatsApp, Drive veya diğer uygulamalara
     * göndermek için Paylaşım (ACTION_SEND) Intent'i başlatır.
     */
    suspend fun shareBackup(context: Context, jsonString: String): Intent = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val fileName = "MusteriDefteri_Yedek_${dateFormat.format(Date())}.json"

        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        val backupFile = File(backupDir, fileName)
        FileOutputStream(backupFile).use { it.write(jsonString.toByteArray(Charsets.UTF_8)) }

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Müşteri Defteri Yedek Dosyası")
            putExtra(Intent.EXTRA_TEXT, "Müşteri Defteri uygulamasının yedek dosyasıdır. Tarih: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Intent.createChooser(shareIntent, "Yedek Dosyasını Paylaş veya Kaydet")
    }

    /**
     * Kullanıcının Dosya Yöneticisi üzerinden seçtiği konuma (Uri) JSON yedeği yazar.
     */
    suspend fun writeBackupToUri(context: Context, uri: Uri, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Kullanıcının Dosya Yöneticisi üzerinden seçtiği JSON yedeği okur.
     */
    suspend fun readBackupFromUri(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "backup_prefs"
        private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val KEY_AUTO_BACKUP_INTERVAL_DAYS = "auto_backup_interval_days"
        private const val KEY_LAST_AUTO_BACKUP_TIME = "last_auto_backup_time"
        private const val KEY_LAST_AUTO_BACKUP_DESC = "last_auto_backup_desc"
        const val AUTO_BACKUP_FILE = "MusteriDefteri_OtoYedek.json"
        const val AUTO_BACKUP_PREV_FILE = "MusteriDefteri_OtoYedek_Onceki.json"
    }

    fun isAutoBackupEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, true)
    }

    fun setAutoBackupEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply()
    }

    fun getAutoBackupIntervalDays(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_AUTO_BACKUP_INTERVAL_DAYS, 1)
    }

    fun setAutoBackupIntervalDays(context: Context, days: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_AUTO_BACKUP_INTERVAL_DAYS, days.coerceAtLeast(1)).apply()
    }

    fun getLastAutoBackupInfo(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_AUTO_BACKUP_DESC, null)
    }

    /**
     * Otomatik sessiz yedekleme alır:
     * - Hem dahili filesDir hem de external Documents klasörüne yazar.
     * - Belirlenen gün aralığıyla veya force = true ise çalışır.
     * - Veritabanı boşsa mevcut yedeği ezmez.
     */
    suspend fun performAutoBackup(context: Context, force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        if (!isAutoBackupEnabled(context) && !force) return@withContext false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastTime = prefs.getLong(KEY_LAST_AUTO_BACKUP_TIME, 0L)
        val now = System.currentTimeMillis()
        val intervalDays = getAutoBackupIntervalDays(context)
        val intervalMillis = intervalDays.toLong() * 24 * 60 * 60 * 1000L

        if (!force && (now - lastTime < intervalMillis)) {
            return@withContext false
        }

        val customers = customerDao.getAllCustomersList()
        val debts = debtDao.getAllDebtsList()
        val receivables = receivableDao.getAllReceivablesList()

        if (customers.isEmpty() && debts.isEmpty() && receivables.isEmpty()) {
            return@withContext false
        }

        try {
            val json = createBackupJson()

            // 1. Dahili depolama (Internal filesDir)
            val internalFile = File(context.filesDir, AUTO_BACKUP_FILE)
            val prevInternalFile = File(context.filesDir, AUTO_BACKUP_PREV_FILE)
            if (internalFile.exists()) {
                internalFile.copyTo(prevInternalFile, overwrite = true)
            }
            internalFile.writeText(json, Charsets.UTF_8)

            // 2. Harici belgeler dizini (External Documents)
            val extDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
            if (extDir != null) {
                if (!extDir.exists()) extDir.mkdirs()
                val extFile = File(extDir, AUTO_BACKUP_FILE)
                val prevExtFile = File(extDir, AUTO_BACKUP_PREV_FILE)
                if (extFile.exists()) {
                    extFile.copyTo(prevExtFile, overwrite = true)
                }
                extFile.writeText(json, Charsets.UTF_8)
            }

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date(now))
            val desc = "$dateStr (${customers.size} Kargo, ${debts.size} Borç, ${receivables.size} Alacak)"

            prefs.edit()
                .putLong(KEY_LAST_AUTO_BACKUP_TIME, now)
                .putString(KEY_LAST_AUTO_BACKUP_DESC, desc)
                .apply()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Varsa cihazda saklanan son otomatik yedek dosyasını getirir.
     */
    suspend fun getSavedAutoBackupJson(context: Context): String? = withContext(Dispatchers.IO) {
        val extDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
        val extFile = if (extDir != null) File(extDir, AUTO_BACKUP_FILE) else null
        if (extFile != null && extFile.exists() && extFile.length() > 0) {
            return@withContext extFile.readText(Charsets.UTF_8)
        }

        val internalFile = File(context.filesDir, AUTO_BACKUP_FILE)
        if (internalFile.exists() && internalFile.length() > 0) {
            return@withContext internalFile.readText(Charsets.UTF_8)
        }

        null
    }
}
