package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.util.PickContactButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Payments
import com.example.data.CustomerEntity
import com.example.data.CustomerStatus
import com.example.util.CurrencyUtils

@Composable
fun AddEditCustomerDialog(
    customerToEdit: CustomerEntity? = null,
    onDismiss: () -> Unit,
    onSave: (fullName: String, phone: String, address: String, extraNotes: String, status: CustomerStatus, amount: Double) -> Unit
) {
    val isEditing = customerToEdit != null
    var fullName by remember { mutableStateOf(customerToEdit?.fullName ?: "") }
    var phoneNumber by remember { mutableStateOf(customerToEdit?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(customerToEdit?.address ?: "") }
    var extraNotes by remember { mutableStateOf(customerToEdit?.extraNotes ?: "") }
    var amountInput by remember {
        mutableStateOf(
            if (customerToEdit != null && customerToEdit.amount > 0) {
                if (customerToEdit.amount % 1.0 == 0.0) {
                    customerToEdit.amount.toLong().toString()
                } else {
                    customerToEdit.amount.toString().replace('.', ',')
                }
            } else ""
        )
    }
    var selectedStatus by remember {
        mutableStateOf(
            if (customerToEdit != null) {
                CustomerStatus.fromString(customerToEdit.status)
            } else {
                CustomerStatus.HAZIRLANIYOR
            }
        )
    }

    var isNameError by remember { mutableStateOf(false) }
    val parsedAmount = CurrencyUtils.parseAmount(amountInput)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("dialog_add_edit_customer"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title and Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Müşteriyi Düzenle" else "Yeni Müşteri Ekle",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isEditing && customerToEdit != null) {
                            Text(
                                text = "Kayıt Numarası (Primary Key): #${customerToEdit.id}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("dialog_close_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                // Rehberden Hızlı Seçim Butonu
                PickContactButton(
                    onContactPicked = { pickedName, pickedPhone ->
                        fullName = pickedName
                        phoneNumber = pickedPhone
                        if (pickedName.isNotBlank()) isNameError = false
                    },
                    label = "Telefon Rehberinden Müşteri Seç"
                )

                // Field 1: Customer Name & Surname
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        if (it.isNotBlank()) isNameError = false
                    },
                    label = { Text("Müşteri Adı ve Soyadı *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    },
                    isError = isNameError,
                    supportingText = {
                        if (isNameError) {
                            Text("Müşteri adı ve soyadı zorunludur")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name"),
                    shape = RoundedCornerShape(16.dp)
                )

                // Field 2: Phone Number
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Telefon Numarası") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_phone"),
                    shape = RoundedCornerShape(16.dp)
                )

                // Field 3: Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresi") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                    },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_address"),
                    shape = RoundedCornerShape(16.dp)
                )

                // Field 4: Extra Field / Notes
                OutlinedTextField(
                    value = extraNotes,
                    onValueChange = { extraNotes = it },
                    label = { Text("Ekstra Alan (Notlar)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                    },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_notes"),
                    shape = RoundedCornerShape(16.dp)
                )

                // Field 5: Para Tutarı (KDV Dahil)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            // Allow digits, dot, and comma
                            if (input.all { it.isDigit() || it == '.' || it == ',' }) {
                                amountInput = input
                            }
                        },
                        label = { Text("Gelen Para Tutarı (KDV Dahil, ₺)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_amount"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Canlı KDV Hesaplama Önizleme Kartı (Örn: KDV dahil 1000 TL -> KDV hariç 833,33 TL)
                    if (parsedAmount > 0.0) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("preview_kdv_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "KDV Dahil Tutar:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = CurrencyUtils.formatCurrency(parsedAmount),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "KDV Hariç Tutar (%20):",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = CurrencyUtils.formatCurrency(CurrencyUtils.calculateKdvHaric(parsedAmount)),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Hesaplanan KDV (%20):",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = CurrencyUtils.formatCurrency(CurrencyUtils.calculateKdvTutari(parsedAmount)),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Field 6: Status Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Statü / Durum Seçimi:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CustomerStatus.entries.forEach { status ->
                        val isSelected = selectedStatus == status
                        val style = getStatusStyle(status)

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) style.textColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            color = if (isSelected) style.backgroundColor else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedStatus = status }
                                .testTag("select_status_${status.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = style.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) style.textColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = style.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) style.textColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seçildi",
                                        tint = style.textColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_btn_cancel"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("İptal")
                    }

                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                isNameError = true
                            } else {
                                onSave(fullName, phoneNumber, address, extraNotes, selectedStatus, parsedAmount)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_btn_save"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (isEditing) "Güncelle" else "Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
