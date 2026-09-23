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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.DebtEntity
import com.example.data.MaterialCatalogManager
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.util.CurrencyUtils
import com.example.util.PickContactButton
import com.example.util.DateUtils
import com.example.util.WhatsAppUtils

/**
 * Dialog for adding a new debt record (Person/Company) or editing basic info.
 */
@Composable
fun AddEditDebtDialog(
    initialDebt: DebtEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (personOrCompany: String, phone: String, notes: String, initialItemTitle: String?, initialItemAmount: Double?, dueDate: Long?) -> Unit
) {
    val context = LocalContext.current
    val catalogManager = remember { MaterialCatalogManager.getInstance(context) }

    var personOrCompany by remember { mutableStateOf(initialDebt?.personOrCompany ?: "") }
    var phone by remember { mutableStateOf(initialDebt?.phoneNumber ?: "") }
    var notes by remember { mutableStateOf(initialDebt?.notes ?: "") }
    var dueDate by remember { mutableStateOf<Long?>(initialDebt?.dueDate) }

    // Initial material fields for new debt records
    var itemTitle by remember { mutableStateOf("") }
    var itemAmountStr by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    val initialSuggestions = remember(itemTitle) {
        if (itemTitle.length >= 2) catalogManager.getSuggestions(itemTitle) else emptyList()
    }

    val isEditing = initialDebt != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Kişi / Firma Düzenle" else "Yeni Borç Kaydı Ekle",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        { Text("Bu alan zorunludur") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_debt_person")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon Numarası (İsteğe bağlı)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_debt_phone")
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
                                    text = "Vade Tarihi (Ödeme Günü)",
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
                                text = if (dueDate != null) DateUtils.formatDate(dueDate!!) else "📅 Vade Tarihi Seçin",
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Açıklama / Not (İsteğe bağlı)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                    },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // If adding new, offer immediate first item fields
                if (!isEditing) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "İlk Alınan Malzeme (İsteğe bağlı)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = itemTitle,
                        onValueChange = { itemTitle = it },
                        label = { Text("Malzeme / Ürün Adı") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_initial_item_title")
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
                        label = { Text("Malzeme Tutarı (₺)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_initial_item_amount")
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
                    val amount = CurrencyUtils.parseAmount(itemAmountStr)
                    if (itemTitle.isNotBlank() && amount > 0) {
                        catalogManager.addOrUpdateMaterial(itemTitle.trim(), amount)
                    }
                    onConfirm(
                        personOrCompany,
                        phone,
                        notes,
                        itemTitle.takeIf { it.isNotBlank() },
                        amount.takeIf { it > 0 },
                        dueDate
                    )
                },
                modifier = Modifier.testTag("btn_save_debt")
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Dialog to add a single purchased material/item to an existing debt.
 * Supports auto-complete from material catalog and past records.
 */
@Composable
fun AddDebtItemDialog(
    personOrCompany: String,
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

    val amount = CurrencyUtils.parseAmount(amountStr)
    val quantity = quantityStr.toIntOrNull() ?: 1
    val lineTotal = amount * quantity

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
                        text = "Malzeme / Ürün Ekle",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = personOrCompany,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Malzeme / Ürün Adı *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                    },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Ürün adı zorunludur") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_item_title")
                )

                // Otomatik Tamamlama Önerileri (Kullanıcı "gold" yazdığında anında çıkar)
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
                        label = { Text("Birim Fiyat (₺) *") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                        },
                        isError = amountError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("input_new_item_amount")
                    )

                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it.filter { char -> char.isDigit() } },
                        label = { Text("Adet") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
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

                if (lineTotal > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CleanMinPrimaryContainer.copy(alpha = 0.4f)
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
                                text = "Eklenecek Tutar:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = CurrencyUtils.formatCurrency(lineTotal),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CleanMinPrimary
                                )
                            )
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
                    if (amount <= 0) {
                        amountError = true
                        return@Button
                    }
                    if (saveToCatalog) {
                        catalogManager.addOrUpdateMaterial(title.trim(), amount)
                    }
                    onConfirm(title.trim(), amount, quantity)
                },
                modifier = Modifier.testTag("btn_confirm_add_item")
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Dialog to record a payment towards a debt.
 * Automatically deducts from remaining balance and directly launches WhatsApp notification.
 */
@Composable
fun MakePaymentDialog(
    personOrCompany: String,
    phoneNumber: String,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String, updatedPhone: String, sendWhatsApp: Boolean) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(phoneNumber) }
    var sendViaWhatsApp by remember { mutableStateOf(true) }
    var amountError by remember { mutableStateOf(false) }

    val enteredAmount = CurrencyUtils.parseAmount(amountStr)
    val remainingAfterPayment = if (currentBalance - enteredAmount < 0.001) 0.0 else currentBalance - enteredAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Ödeme Yap",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = personOrCompany,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Balance Banner
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mevcut Kalan Borç:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = CurrencyUtils.formatCurrency(currentBalance),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }

                // Payment amount field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        if (it.isNotBlank()) amountError = false
                    },
                    label = { Text("Ödenen Tutar (₺) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                    },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Lütfen geçerli bir tutar girin") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_payment_amount")
                )

                // Quick full balance shortcut button
                if (currentBalance > 0) {
                    TextButton(
                        onClick = {
                            amountStr = if (currentBalance % 1.0 == 0.0) {
                                currentBalance.toInt().toString()
                            } else {
                                String.format(java.util.Locale.US, "%.2f", currentBalance)
                            }
                            amountError = false
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Kalanın Tamamı (${CurrencyUtils.formatCurrency(currentBalance)})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                // Payment note / explanation
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ödeme Açıklaması (İsteğe bağlı)") },
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
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_payment_phone")
                )

                // WhatsApp automatic notification toggle
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (sendViaWhatsApp) {
                            androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFF15803D),
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
                                        text = "Ödeme ve kalan bakiye doğrudan gönderilir",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = sendViaWhatsApp,
                                onCheckedChange = { sendViaWhatsApp = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                    checkedTrackColor = androidx.compose.ui.graphics.Color(0xFF25D366)
                                )
                            )
                        }

                        // WhatsApp Message Preview
                        if (sendViaWhatsApp && enteredAmount > 0) {
                            HorizontalDivider(color = androidx.compose.ui.graphics.Color(0xFF25D366).copy(alpha = 0.3f))
                            Text(
                                text = "Gönderilecek Mesaj Taslağı:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = androidx.compose.ui.graphics.Color(0xFF15803D)
                                )
                            )
                            Surface(
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Merhaba ${personOrCompany.ifBlank { "Sayın İlgili" }},",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                    )
                                    Text(
                                        text = "✅ ${CurrencyUtils.formatCurrency(enteredAmount)} tutarında ödemeniz yapılmıştır.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    if (note.isNotBlank()) {
                                        Text(
                                            text = "📝 Ödeme Açıklaması: ${note.trim()}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                        )
                                    }
                                    Text(
                                        text = if (remainingAfterPayment <= 0.001) "🎉 Kalan Bakiyemiz: 0,00 ₺ (Kapanmıştır)" else "📊 Güncel Kalan Bakiyemiz: ${CurrencyUtils.formatCurrency(remainingAfterPayment)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (remainingAfterPayment <= 0.001) androidx.compose.ui.graphics.Color(0xFF15803D) else androidx.compose.ui.graphics.Color(0xFFC62828)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Summary card of remaining balance
                if (enteredAmount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (remainingAfterPayment <= 0.001) {
                                androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                            } else {
                                CleanMinPrimaryContainer.copy(alpha = 0.4f)
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
                                text = if (remainingAfterPayment <= 0.001) "Borç Tamamen Kapanacak!" else "İşlem Sonrası Kalan:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = CurrencyUtils.formatCurrency(remainingAfterPayment),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingAfterPayment <= 0.001) {
                                        androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    } else {
                                        CleanMinPrimary
                                    }
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
                    if (enteredAmount <= 0) {
                        amountError = true
                        return@Button
                    }
                    onConfirm(enteredAmount, note, phone.trim(), sendViaWhatsApp)
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
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = if (willLaunchWhatsApp) "Ödemeyi Kaydet & WhatsApp" else "Ödemeyi Kaydet",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Dialog to confirm deletion of a debt record.
 */
@Composable
fun ConfirmDeleteDebtDialog(
    personOrCompany: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Borç Kaydını Sil",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "\"$personOrCompany\" kaydını ve tüm malzeme/ödeme geçmişini silmek istediğinize emin misiniz? Bu işlem geri alınamaz.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.testTag("btn_confirm_delete_debt")
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
            OutlinedButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Dialog to edit an existing payment (fix wrongly entered amount or note).
 */
@Composable
fun EditPaymentDialog(
    personOrCompany: String,
    initialAmount: Double,
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (newAmount: Double, newNote: String) -> Unit
) {
    var amountStr by remember {
        mutableStateOf(
            if (initialAmount % 1.0 == 0.0) initialAmount.toInt().toString()
            else String.format(java.util.Locale.US, "%.2f", initialAmount)
        )
    }
    var note by remember { mutableStateOf(initialNote) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Ödemeyi Düzenle",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = personOrCompany,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Yanlış girilen tutarı veya açıklamayı düzeltebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        if (it.isNotBlank()) amountError = false
                    },
                    label = { Text("Ödenen Tutar (₺) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                    },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Lütfen geçerli bir tutar girin") }
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
                    label = { Text("Ödeme Açıklaması") },
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
                    val amount = CurrencyUtils.parseAmount(amountStr)
                    if (amount <= 0) {
                        amountError = true
                        return@Button
                    }
                    onConfirm(amount, note.trim())
                },
                modifier = Modifier.testTag("btn_confirm_edit_payment")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Güncelle")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

