package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DebtEntity
import com.example.data.DebtPayment
import com.example.ui.theme.CleanMinBackground
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.util.CurrencyUtils
import com.example.util.ExcelExportManager
import com.example.util.WhatsAppUtils
import kotlinx.coroutines.launch

@Composable
fun DebtListScreen(
    viewModel: DebtViewModel,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onOpenBackupDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Dialog states
    var showAddEditDialog by remember { mutableStateOf(false) }
    var debtToEdit by remember { mutableStateOf<DebtEntity?>(null) }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var targetDebtForAddItem by remember { mutableStateOf<DebtEntity?>(null) }

    var showMakePaymentDialog by remember { mutableStateOf(false) }
    var targetDebtForPayment by remember { mutableStateOf<DebtEntity?>(null) }

    var showEditPaymentDialog by remember { mutableStateOf(false) }
    var targetDebtForEditPayment by remember { mutableStateOf<DebtEntity?>(null) }
    var paymentToEdit by remember { mutableStateOf<DebtPayment?>(null) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var debtToDelete by remember { mutableStateOf<DebtEntity?>(null) }
    var showMaterialCatalogDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Borçlarım",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Alınan Malzeme & Ödeme Bakiye Takibi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Malzeme Rehberi butonu
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { showMaterialCatalogDialog = true }
                                .testTag("btn_debt_material_catalog")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = "Malzeme Kataloğu",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // Excel CSV Paylaş / Dışa Aktar butonu
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        val csvData = ExcelExportManager.exportDebtsToCsv(debts)
                                        val intent = ExcelExportManager.shareCsvFile(
                                            context = context,
                                            filenamePrefix = "Borclarim_Listesi",
                                            csvContent = csvData,
                                            subject = "Borçlar Listesi"
                                        )
                                        context.startActivity(intent)
                                    }
                                }
                                .testTag("btn_export_debts_csv")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Excel'e Aktar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // Search toggle button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { isSearchExpanded = !isSearchExpanded }
                                .testTag("btn_toggle_debt_search")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSearchExpanded && searchQuery.isEmpty()) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Arama",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Summary Card (Toplam Borç, Ödenen Tutar, Kalan Borç)
            item(key = "summary") {
                DebtSummaryCard(
                    summary = summary,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                )
            }

            // Expandable Search Bar (açılır/kapanır)
            item(key = "search") {
                AnimatedVisibility(
                    visible = isSearchExpanded || searchQuery.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Kişi ve firma ara") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Ara",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Temizle",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = CleanMinPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_debt_input")
                        )
                    }
                }
            }

            // Horizontal Add Button (spans the screen width directly below search bar)
            item(key = "add_button") {
                Button(
                    onClick = {
                        debtToEdit = null
                        showAddEditDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CleanMinPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                        .testTag("btn_add_debt")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Yeni Kişi / Firma Ekle",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Filter Tabs: Açık Borç first/active by default, Vadeliler, Ödenen, Tümü at the end
            item(key = "filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DebtFilterChip(
                        text = "Açık Borç (${summary.openDebtCount})",
                        isSelected = selectedFilter == DebtFilter.UNPAID,
                        onClick = { viewModel.onFilterChange(DebtFilter.UNPAID) }
                    )

                    if (summary.dueCount > 0) {
                        DebtFilterChip(
                            text = "Vadeliler (${summary.dueCount})",
                            isSelected = selectedFilter == DebtFilter.DUE_DATE,
                            onClick = { viewModel.onFilterChange(DebtFilter.DUE_DATE) }
                        )
                    }

                    DebtFilterChip(
                        text = "Ödenen (${summary.paidOffCount})",
                        isSelected = selectedFilter == DebtFilter.PAID,
                        onClick = { viewModel.onFilterChange(DebtFilter.PAID) }
                    )

                    DebtFilterChip(
                        text = "Tümü (${summary.totalCount})",
                        isSelected = selectedFilter == DebtFilter.ALL,
                        onClick = { viewModel.onFilterChange(DebtFilter.ALL) }
                    )
                }
            }

            // Debts List
            if (debts.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CleanMinPrimaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = CleanMinPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (searchQuery.isNotBlank()) "Aramaya uygun borç kaydı bulunamadı"
                                else if (selectedFilter == DebtFilter.UNPAID) "Açık borcunuz bulunmuyor"
                                else if (selectedFilter == DebtFilter.PAID) "Ödenmiş / borcu olmayan kayıt bulunmuyor"
                                else "Henüz borç kaydı bulunmuyor",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = if (searchQuery.isNotBlank()) "Farklı bir kelime deneyebilir veya aramayı temizleyebilirsiniz."
                                else "Borcunuz olan kişi ve firmaları ekleyip aldığınız malzemeleri ve yaptığınız ödemeleri takip edin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )

                            if (searchQuery.isBlank()) {
                                Button(
                                    onClick = {
                                        debtToEdit = null
                                        showAddEditDialog = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Yeni Borç Kaydı Ekle")
                                }
                            }
                        }
                    }
                }
            } else {
                items(debts, key = { it.id }) { debt ->
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                        DebtCard(
                            debt = debt,
                            onAddItemClick = {
                                targetDebtForAddItem = debt
                                showAddItemDialog = true
                            },
                            onRemoveItemClick = { itemId ->
                                viewModel.removeItemFromDebt(debt.id, itemId)
                            },
                            onMakePaymentClick = {
                                targetDebtForPayment = debt
                                showMakePaymentDialog = true
                            },
                            onEditPaymentClick = { payment ->
                                targetDebtForEditPayment = debt
                                paymentToEdit = payment
                                showEditPaymentDialog = true
                            },
                            onRemovePaymentClick = { paymentId ->
                                viewModel.removePayment(debt.id, paymentId)
                            },
                            onEditClick = {
                                debtToEdit = debt
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                debtToDelete = debt
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showMaterialCatalogDialog) {
        MaterialCatalogDialog(
            onDismissRequest = { showMaterialCatalogDialog = false }
        )
    }

    if (showAddEditDialog) {
        AddEditDebtDialog(
            initialDebt = debtToEdit,
            onDismiss = {
                showAddEditDialog = false
                debtToEdit = null
            },
            onConfirm = { personOrCompany, phone, notes, initialItemTitle, initialItemAmount, dueDate ->
                val editItem = debtToEdit
                if (editItem == null) {
                    viewModel.addDebt(
                        personOrCompany = personOrCompany,
                        phone = phone,
                        notes = notes,
                        initialItemTitle = initialItemTitle,
                        initialItemAmount = initialItemAmount,
                        dueDate = dueDate
                    )
                } else {
                    viewModel.updateDebtInfo(
                        debtId = editItem.id,
                        personOrCompany = personOrCompany,
                        phone = phone,
                        notes = notes,
                        dueDate = dueDate
                    )
                }
                showAddEditDialog = false
                debtToEdit = null
            }
        )
    }

    if (showAddItemDialog && targetDebtForAddItem != null) {
        val target = targetDebtForAddItem!!
        AddDebtItemDialog(
            personOrCompany = target.personOrCompany,
            onDismiss = {
                showAddItemDialog = false
                targetDebtForAddItem = null
            },
            onConfirm = { title, amount, quantity ->
                viewModel.addItemToDebt(target.id, title, amount, quantity)
                showAddItemDialog = false
                targetDebtForAddItem = null
            }
        )
    }

    if (showMakePaymentDialog && targetDebtForPayment != null) {
        val target = targetDebtForPayment!!
        MakePaymentDialog(
            personOrCompany = target.personOrCompany,
            phoneNumber = target.phoneNumber,
            currentBalance = target.calculateRemainingBalance(),
            onDismiss = {
                showMakePaymentDialog = false
                targetDebtForPayment = null
            },
            onConfirm = { amount, note, updatedPhone, sendWhatsApp ->
                viewModel.makePayment(target.id, amount, note)

                // Update phone number if provided or modified
                if (updatedPhone.isNotBlank() && updatedPhone != target.phoneNumber) {
                    viewModel.updateDebtInfo(
                        debtId = target.id,
                        personOrCompany = target.personOrCompany,
                        phone = updatedPhone,
                        notes = target.notes
                    )
                }

                // Immediately send WhatsApp message with payment amount, note, extra notes, and remaining balance
                if (sendWhatsApp) {
                    val currentBal = target.calculateRemainingBalance()
                    val remainingAfter = if (currentBal - amount < 0.001) 0.0 else currentBal - amount
                    val message = WhatsAppUtils.buildPaymentNotificationMessage(
                        personOrCompany = target.personOrCompany,
                        paidAmount = amount,
                        paymentNote = note,
                        remainingBalance = remainingAfter,
                        debtNotes = target.notes
                    )
                    WhatsAppUtils.openWhatsApp(context, updatedPhone, message)
                }

                showMakePaymentDialog = false
                targetDebtForPayment = null
            }
        )
    }

    if (showEditPaymentDialog && targetDebtForEditPayment != null && paymentToEdit != null) {
        val target = targetDebtForEditPayment!!
        val payment = paymentToEdit!!
        EditPaymentDialog(
            personOrCompany = target.personOrCompany,
            initialAmount = payment.amount,
            initialNote = payment.note,
            onDismiss = {
                showEditPaymentDialog = false
                targetDebtForEditPayment = null
                paymentToEdit = null
            },
            onConfirm = { newAmount, newNote ->
                viewModel.updatePayment(
                    debtId = target.id,
                    paymentId = payment.id,
                    newAmount = newAmount,
                    newNote = newNote
                )
                showEditPaymentDialog = false
                targetDebtForEditPayment = null
                paymentToEdit = null
            }
        )
    }

    if (showDeleteConfirmDialog && debtToDelete != null) {
        val target = debtToDelete!!
        ConfirmDeleteDebtDialog(
            personOrCompany = target.personOrCompany,
            onDismiss = {
                showDeleteConfirmDialog = false
                debtToDelete = null
            },
            onConfirm = {
                viewModel.deleteDebt(target.id)
                showDeleteConfirmDialog = false
                debtToDelete = null
            }
        )
    }
}

@Composable
private fun DebtFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) CleanMinPrimary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isSelected) CleanMinPrimary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DebtSummaryCard(
    summary: DebtSummary,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Toplam Borç
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Toplam Borç",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = CurrencyUtils.formatCurrency(summary.totalItemsAmount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Ödenen Tutar
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Ödenen Tutar",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = CurrencyUtils.formatCurrency(summary.totalPaidAmount),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                )
            }

            // Kalan Borç
            Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                val hasRemaining = summary.totalRemainingBalance > 0.001
                val balanceColor = if (hasRemaining) Color(0xFFDC2626) else Color(0xFF15803D)
                Text(
                    text = "Kalan Borç",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = balanceColor,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = CurrencyUtils.formatCurrency(summary.totalRemainingBalance),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = balanceColor
                    )
                )
            }
        }
    }
}
