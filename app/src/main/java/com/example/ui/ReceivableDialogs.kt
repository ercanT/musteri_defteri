package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MaterialCatalogManager
import com.example.data.ReceivableEntity
import com.example.data.ReceivablePayment
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.PickContactButton
import com.example.util.WhatsAppUtils

/**
 * Dialog for adding a new receivable record (Person/Company) or editing basic info.
 */
@Composable
fun AddEditReceivableDialog(
    initialReceivable: ReceivableEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (personOrCompany: String, phone: String, notes: String, initialItemTitle: String?, initialItemAmount: Double?, dueDate: Long?) -> Unit
) {
    val context = LocalContext.current
    val catalogManager = remember { MaterialCatalogManager.getInstance(context) }

    var personOrCompany by remember { mutableStateOf(initialReceivable?.personOrCompany ?: "") }
    var phone by remember { mutableStateOf(initialReceivable?.phoneNumber ?: "") }
    var notes by remember { mutableStateOf(initialReceivable?.notes ?: "") }
    var dueDate by remember { mutableStateOf<Long?>(initialReceivable?.dueDate) }

    var itemTitle by remember { mutableStateOf("") }
    var itemAmountStr by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    val initialSuggestions = remember(itemTitle) {
        if (itemTitle.length >= 2) catalogManager.getSuggestions(itemTitle) else emptyList()
    }

    val isEditing = initialReceivable != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Alacak Bilgilerini Düzenle" else "Yeni Alacak Kaydı",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Rehberden Seç Butonu
                PickContactButton(
                    onContactPicked = { pickedName, pickedPhone ->
                        personOrCompany = pickedName
                        phone = pickedPhone
                        if (pickedName.isNotBlank()) nameError = false
                    },
                    label = "Telefon Rehberinden Kişi / Firma Seç"
                )

                // Person or Company Name
                OutlinedTextField(
                    value = personOrCompany,
                    onValueChange = {
                        personOrCompany = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Kişi veya Firma Adı *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Business, contentDescription = null)
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Lütfen kişi veya firma adını giriniz") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_receivable_name")
                )

                // Phone Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon Numarası (WhatsApp)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_receivable_phone")
                )

                // Vade Tarihi Alanı (İsteğe Bağlı)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = CleanMinPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = "Tahsilat Vade Tarihi",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (dueDate != null) {
                                TextButton(
                                    onClick = { dueDate = null },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Temizle", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                DateUtils.showDatePickerDialog(context, dueDate) { selectedMillis ->
                                    dueDate = selectedMillis
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = if (dueDate != null) DateUtils.formatDate(dueDate!!) else "📅 Vade Tarihi Belirle",
                                fontWeight = if (dueDate != null) FontWeight.Bold else FontWeight.Normal,
                                color = if (dueDate != null) CleanMinPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val dueStatus = DateUtils.getDueDateStatus(dueDate)
                        if (dueStatus != null) {
                            Text(
                                text = dueStatus.badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (dueStatus.isOverdue) Color(0xFFC62828) else CleanMinPrimary
                            )
                        }
                    }
                }

                // Notes / Ekstra Alan
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ekstra Not / Açıklama") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                    },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_receivable_notes")
                )

                // Initial item input (only for new records)
                if (!isEditing) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "İlk Satış / Malzeme / Hizmet (İsteğe Bağlı):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = itemTitle,
                        onValueChange = { itemTitle = it },
                        label = { Text("Kalem / Hizmet / Malzeme") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_receivable_initial_item_title")
                    )

                    if (initialSuggestions.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(initialSuggestions) { sug ->
                                SuggestionChip(
                                    onClick = {
                                        itemTitle = sug.name
                                        if (sug.defaultPrice > 0 && itemAmountStr.isBlank()) {
                                            itemAmountStr = if (sug.defaultPrice % 1.0 == 0.0) sug.defaultPrice.toInt().toString() else sug.defaultPrice.toString()
                                        }
                                    },
                                    label = { Text(sug.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = itemAmountStr,
                        onValueChange = { itemAmountStr = it },
                        label = { Text("Tutar (₺ - KDV Dahil)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_receivable_initial_item_amount")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (personOrCompany.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val amount = itemAmountStr.replace(",", ".").toDoubleOrNull()
                    if (itemTitle.isNotBlank() && amount != null && amount > 0) {
                        catalogManager.addOrUpdateMaterial(itemTitle.trim(), amount)
                    }
                    onConfirm(
                        personOrCompany.trim(),
                        phone.trim(),
                        notes.trim(),
                        if (itemTitle.isNotBlank()) itemTitle.trim() else null,
                        amount,
                        dueDate
                    )
                },
                modifier = Modifier.testTag("btn_save_receivable")
            ) {
                Text(if (isEditing) "Güncelle" else "Kaydet")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_receivable")
            ) {
                Text("İptal")
            }
        }
    )
}

/**
 * Dialog for adding a new item / material to an existing receivable.
 */
@Composable
fun AddReceivableItemDialog(
    receivableName: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, quantity: Int) -> Unit
) {
    val context = LocalContext.current
    val catalogManager = remember { MaterialCatalogManager.getInstance(context) }

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("1") }
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var saveToCatalog by remember { mutableStateOf(true) }
    var showFullCatalogDialog by remember { mutableStateOf(false) }

    val suggestions = remember(title) {
        catalogManager.getSuggestions(title)
    }

    val unitPrice = amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0
    val qty = quantityStr.toIntOrNull() ?: 1
    val totalLine = unitPrice * if (qty < 1) 1 else qty

    if (showFullCatalogDialog) {
        MaterialCatalogDialog(
            onDismissRequest = { showFullCatalogDialog = false },
            onSelectMaterial = { selectedItem ->
                title = selectedItem.name
                if (selectedItem.defaultPrice > 0) {
                    amountStr = if (selectedItem.defaultPrice % 1.0 == 0.0) {
                        selectedItem.defaultPrice.toInt().toString()
                    } else {
                        selectedItem.defaultPrice.toString()
                    }
                }
                titleError = false
                amountError = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kalem / Malzeme Ekle",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = receivableName,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                TextButton(
                    onClick = { showFullCatalogDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Rehber", fontSize = 12.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Verilen Malzeme / Hizmet / Ürün *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                    },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Lütfen açıklama giriniz") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_item_title")
                )

                // Otomatik Tamamlama Önerileri
                if (suggestions.isNotEmpty() && suggestions.none { it.name.equals(title.trim(), ignoreCase = true) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "💡 Önerilen Malzemeler (Dokun ve doldur):",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanMinPrimary
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestions) { item ->
                                SuggestionChip(
                                    onClick = {
                                        title = item.name
                                        if (item.defaultPrice > 0) {
                                            amountStr = if (item.defaultPrice % 1.0 == 0.0) {
                                                item.defaultPrice.toInt().toString()
                                            } else {
                                                item.defaultPrice.toString()
                                            }
                                        }
                                        titleError = false
                                        if (amountStr.isNotBlank()) amountError = false
                                    },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            if (item.defaultPrice > 0) {
                                                Text(
                                                    CurrencyUtils.formatCurrency(item.defaultPrice),
                                                    fontSize = 10.sp,
                                                    color = CleanMinPrimary
                                                )
                                            }
                                        }
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = CleanMinPrimaryContainer.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = {
                            amountStr = it
                            if (it.isNotBlank()) amountError = false
                        },
                        label = { Text("Birim Fiyat *") },
                        placeholder = { Text("250.0") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                        },
                        isError = amountError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("input_item_price")
                    )

                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Adet") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.7f)
                            .testTag("input_item_quantity")
                    )
                }

                // Kataloğa kaydet seçeneği
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = saveToCatalog,
                        onCheckedChange = { saveToCatalog = it },
                        colors = CheckboxDefaults.colors(checkedColor = CleanMinPrimary)
                    )
                    Text(
                        text = "Bu malzemeyi gelecekte önermek için kaydet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (totalLine > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CleanMinPrimaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Toplam Satır Tutarı (KDV Dahil):",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = CurrencyUtils.formatCurrency(totalLine),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CleanMinPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "KDV Hariç (%20):",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = CurrencyUtils.formatCurrency(CurrencyUtils.calculateKdvHaric(totalLine)),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    val amount = amountStr.replace(",", ".").toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        amountError = true
                        return@Button
                    }
                    val q = quantityStr.toIntOrNull() ?: 1
                    if (saveToCatalog) {
                        catalogManager.addOrUpdateMaterial(title.trim(), amount)
                    }
                    onConfirm(title.trim(), amount, if (q < 1) 1 else q)
                },
                modifier = Modifier.testTag("btn_save_item")
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_item")
            ) {
                Text("İptal")
            }
        }
    )
}

/**
 * Dialog for recording a payment/collection (Tahsilat Yap) for a receivable.
 */
@Composable
fun MakeReceivablePaymentDialog(
    receivable: ReceivableEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String, updatedPhone: String, sendWhatsApp: Boolean) -> Unit
) {
    val totalAmount = receivable.calculateTotalAmount()
    val totalPaid = receivable.calculateTotalPaid()
    val currentBalance = receivable.calculateRemainingBalance()

    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(receivable.phoneNumber) }
    var sendViaWhatsApp by remember { mutableStateOf(true) }
    var amountError by remember { mutableStateOf(false) }

    val enteredAmount = amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0
    val newRemaining = (currentBalance - enteredAmount).let { if (it < 0.001) 0.0 else it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Tahsilat Girişi Yap",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = receivable.personOrCompany,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CleanMinPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Balance summary card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Toplam Alacak:", style = MaterialTheme.typography.bodySmall)
                            Text(CurrencyUtils.formatCurrency(totalAmount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Önceden Tahsil Edilen:", style = MaterialTheme.typography.bodySmall)
                            Text(CurrencyUtils.formatCurrency(totalPaid), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Mevcut Kalan Alacak:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                CurrencyUtils.formatCurrency(currentBalance),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }

                // Payment amount input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        if (it.isNotBlank()) amountError = false
                    },
                    label = { Text("Tahsil Edilen Tutar (₺) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                    },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Lütfen geçerli bir tahsilat tutarı giriniz") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_payment_amount")
                )

                // Quick buttons for full or partial balance
                if (currentBalance > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                amountStr = if (currentBalance % 1.0 == 0.0) {
                                    currentBalance.toLong().toString()
                                } else {
                                    String.format(java.util.Locale.US, "%.2f", currentBalance)
                                }
                                amountError = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tümünü Kapat", fontSize = 12.sp)
                        }

                        if (currentBalance > 100) {
                            OutlinedButton(
                                onClick = {
                                    val half = currentBalance / 2.0
                                    amountStr = String.format(java.util.Locale.US, "%.2f", half)
                                    amountError = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Yarısını Öde", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Note / Explanation
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Tahsilat Açıklaması (İsteğe Bağlı)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_payment_note")
                )

                // Phone number for WhatsApp
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon Numarası (WhatsApp)") },
                    placeholder = { Text("0532 123 45 67") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_payment_phone")
                )

                // WhatsApp notification toggle
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (sendViaWhatsApp) {
                            androidx.compose.ui.graphics.Color(0xFF25D366).copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp",
                                    tint = if (sendViaWhatsApp) androidx.compose.ui.graphics.Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "WhatsApp ile Bilgilendir",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (sendViaWhatsApp) androidx.compose.ui.graphics.Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "Tahsilat makbuzu ve kalan bakiye gönderilsin",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                            Switch(
                                checked = sendViaWhatsApp,
                                onCheckedChange = { sendViaWhatsApp = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = androidx.compose.ui.graphics.Color(0xFF25D366),
                                    checkedTrackColor = androidx.compose.ui.graphics.Color(0xFF25D366).copy(alpha = 0.5f)
                                )
                            )
                        }

                        // WhatsApp Message Preview
                        if (sendViaWhatsApp && enteredAmount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Mesaj Önizleme:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = androidx.compose.ui.graphics.Color(0xFF15803D)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = WhatsAppUtils.buildReceivablePaymentNotificationMessage(
                                            personOrCompany = receivable.personOrCompany,
                                            paidAmount = enteredAmount,
                                            paymentNote = note,
                                            remainingBalance = newRemaining,
                                            receivableNotes = receivable.notes
                                        ),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = androidx.compose.ui.graphics.Color(0xFF1E293B)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Balance change indicator
                if (enteredAmount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (newRemaining <= 0.001) {
                                androidx.compose.ui.graphics.Color(0xFF15803D).copy(alpha = 0.12f)
                            } else {
                                CleanMinPrimaryContainer.copy(alpha = 0.5f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (newRemaining <= 0.001) "🎉 Tahsilat Sonrası Hesap Kapanıyor!" else "Tahsilat Sonrası Kalan:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = CurrencyUtils.formatCurrency(newRemaining),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (newRemaining <= 0.001) androidx.compose.ui.graphics.Color(0xFF15803D) else CleanMinPrimary
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val willLaunchWhatsApp = sendViaWhatsApp && phone.isNotBlank()
            Button(
                onClick = {
                    val amount = amountStr.replace(",", ".").toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        amountError = true
                        return@Button
                    }
                    onConfirm(amount, note, phone.trim(), sendViaWhatsApp)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (willLaunchWhatsApp) androidx.compose.ui.graphics.Color(0xFF25D366) else androidx.compose.ui.graphics.Color(0xFF2E7D32)
                ),
                modifier = Modifier.testTag("btn_confirm_payment")
            ) {
                if (willLaunchWhatsApp) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_whatsapp),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                }
                Text(
                    text = if (willLaunchWhatsApp) "Tahsilatı Kaydet & WhatsApp" else "Tahsilatı Kaydet",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_payment")
            ) {
                Text("İptal")
            }
        }
    )
}

/**
 * Dialog for editing an existing payment/collection.
 */
@Composable
fun EditReceivablePaymentDialog(
    initialPayment: ReceivablePayment,
    onDismiss: () -> Unit,
    onConfirm: (updatedAmount: Double, updatedNote: String) -> Unit
) {
    var amountStr by remember {
        mutableStateOf(
            if (initialPayment.amount % 1.0 == 0.0) {
                initialPayment.amount.toLong().toString()
            } else {
                String.format(java.util.Locale.US, "%.2f", initialPayment.amount)
            }
        )
    }
    var note by remember { mutableStateOf(initialPayment.note) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tahsilatı Düzelt / Güncelle",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Tahsilat tutarını veya açıklama notunu düzeltebilirsiniz. Kalan bakiye otomatik olarak yeniden hesaplanacaktır.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        if (it.isNotBlank()) amountError = false
                    },
                    label = { Text("Tahsil Edilen Tutar (₺) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                    },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Lütfen geçerli bir tahsilat tutarı giriniz") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_payment_amount")
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Tahsilat Açıklaması") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_payment_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.replace(",", ".").toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        amountError = true
                        return@Button
                    }
                    onConfirm(amount, note.trim())
                },
                modifier = Modifier.testTag("btn_save_edit_payment")
            ) {
                Text("Değişiklikleri Kaydet")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_edit_payment")
            ) {
                Text("İptal")
            }
        }
    )
}

/**
 * Confirmation dialog for deleting an entire receivable record.
 */
@Composable
fun ConfirmDeleteReceivableDialog(
    receivableName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Alacak Kaydını Sil",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "\"$receivableName\" isimli alacak kaydı ve bu kayda ait tüm malzeme ve tahsilat geçmişi kalıcı olarak silinecektir. Devam etmek istiyor musunuz?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("btn_confirm_delete_receivable")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Sil")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_delete_receivable")
            ) {
                Text("Vazgeç")
            }
        }
    )
}
