package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BackupManager
import com.example.data.BackupSummary
import com.example.data.RestoreMode
import com.example.data.RestoreResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isAutoBackupSaving: Boolean = false,
    val isAutoBackupEnabled: Boolean = true,
    val lastAutoBackupInfo: String? = null,
    val totalCustomers: Int = 0,
    val totalDebts: Int = 0,
    val totalReceivables: Int = 0,
    val inspectedSummary: BackupSummary? = null,
    val pendingJson: String? = null,
    val lastResult: RestoreResult? = null,
    val startupAutoBackupSummary: BackupSummary? = null,
    val startupAutoBackupJson: String? = null
)

class BackupViewModel(
    application: Application,
    private val backupManager: BackupManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        BackupUiState(
            isAutoBackupEnabled = backupManager.isAutoBackupEnabled(application),
            lastAutoBackupInfo = backupManager.getLastAutoBackupInfo(application)
        )
    )
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        // Uygulama her açıldığında arka planda sessizce günlük otomatik yedek kontrolü yap
        performAutoBackupSilent()
    }

    fun loadCurrentStats() {
        viewModelScope.launch {
            try {
                val db = AppDatabase.getDatabase(getApplication())
                val customerCount = db.customerDao().getAllCustomersList().size
                val debtCount = db.debtDao().getAllDebtsList().size
                val receivableCount = db.receivableDao().getAllReceivablesList().size
                _uiState.value = _uiState.value.copy(
                    totalCustomers = customerCount,
                    totalDebts = debtCount,
                    totalReceivables = receivableCount,
                    isAutoBackupEnabled = backupManager.isAutoBackupEnabled(getApplication()),
                    lastAutoBackupInfo = backupManager.getLastAutoBackupInfo(getApplication())
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Arka planda sessizce çalışan günlük otomatik yedekleme
     */
    fun performAutoBackupSilent() {
        viewModelScope.launch {
            val success = backupManager.performAutoBackup(getApplication(), force = false)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    lastAutoBackupInfo = backupManager.getLastAutoBackupInfo(getApplication())
                )
            }
        }
    }

    fun toggleAutoBackup(enabled: Boolean) {
        backupManager.setAutoBackupEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(isAutoBackupEnabled = enabled)
    }

    /**
     * Kullanıcı Ayarlar'dan "Şimdi Otomatik Yedek Al" butonuna bastığında
     */
    fun triggerManualAutoBackup(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAutoBackupSaving = true)
            try {
                val success = backupManager.performAutoBackup(context, force = true)
                if (success) {
                    val info = backupManager.getLastAutoBackupInfo(context)
                    _uiState.value = _uiState.value.copy(
                        isAutoBackupSaving = false,
                        lastAutoBackupInfo = info
                    )
                    Toast.makeText(context, "Otomatik yedek telefon hafızasına kaydedildi!", Toast.LENGTH_SHORT).show()
                } else {
                    _uiState.value = _uiState.value.copy(isAutoBackupSaving = false)
                    Toast.makeText(context, "Yedeklenecek kayıt bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAutoBackupSaving = false)
                Toast.makeText(context, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Cihazdaki son otomatik yedeği inceleyip geri yükleme penceresini açar
     */
    fun restoreFromLastAutoBackup(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val json = backupManager.getSavedAutoBackupJson(context)
                if (json.isNullOrBlank()) {
                    Toast.makeText(context, "Kayıtlı otomatik yedek dosyası bulunamadı.", Toast.LENGTH_LONG).show()
                    _uiState.value = _uiState.value.copy(isImporting = false)
                    return@launch
                }
                val summary = backupManager.inspectBackup(json)
                if (!summary.isValid) {
                    Toast.makeText(context, "Yedek dosyası bozuk veya geçersiz.", Toast.LENGTH_LONG).show()
                    _uiState.value = _uiState.value.copy(isImporting = false)
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    inspectedSummary = summary,
                    pendingJson = json
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                Toast.makeText(context, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Yedek oluşturup WhatsApp, Drive vb. paylaşım menüsünü açar.
     */
    fun exportAndShare(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val json = backupManager.createBackupJson()
                val chooserIntent = backupManager.shareBackup(context, json)
                context.startActivity(chooserIntent)
                Toast.makeText(context, "Yedek dosyası hazırlandı.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Yedekleme hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                _uiState.value = _uiState.value.copy(isExporting = false)
            }
        }
    }

    /**
     * Kullanıcının dosya yöneticisinde seçtiği konuma doğrudan yazar.
     */
    fun exportToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val json = backupManager.createBackupJson()
                val success = backupManager.writeBackupToUri(context, uri, json)
                if (success) {
                    Toast.makeText(context, "Yedek başarıyla dosyaya kaydedildi!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Yedek dosyaya yazılamadı.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Yazma hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                _uiState.value = _uiState.value.copy(isExporting = false)
            }
        }
    }

    /**
     * Kullanıcının seçtiği yedek dosyasını inceler ve onay için UI durumuna kaydeder.
     */
    fun inspectBackupFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val json = backupManager.readBackupFromUri(context, uri)
                if (json.isNullOrBlank()) {
                    Toast.makeText(context, "Dosya okunamadı veya içi boş.", Toast.LENGTH_LONG).show()
                    _uiState.value = _uiState.value.copy(isImporting = false)
                    return@launch
                }
                val summary = backupManager.inspectBackup(json)
                if (!summary.isValid) {
                    Toast.makeText(context, summary.errorMessage ?: "Geçersiz yedek dosyası.", Toast.LENGTH_LONG).show()
                    _uiState.value = _uiState.value.copy(isImporting = false)
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    inspectedSummary = summary,
                    pendingJson = json
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                _uiState.value = _uiState.value.copy(isImporting = false)
            }
        }
    }

    /**
     * İncelemesi yapılmış yedek dosyasını onaylanan modda (MERGE veya REPLACE) geri yükler.
     */
    fun executeRestore(mode: RestoreMode, onFinished: (Boolean, String) -> Unit) {
        val json = _uiState.value.pendingJson ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val result = backupManager.restoreFromJson(json, mode)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    inspectedSummary = null,
                    pendingJson = null,
                    lastResult = result
                )
                loadCurrentStats()
                onFinished(result.success, result.message)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                onFinished(false, "Geri yükleme başarısız: ${e.localizedMessage}")
            }
        }
    }

    fun dismissInspectDialog() {
        _uiState.value = _uiState.value.copy(
            inspectedSummary = null,
            pendingJson = null
        )
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BackupViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val manager = BackupManager(db)
                return BackupViewModel(application, manager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
