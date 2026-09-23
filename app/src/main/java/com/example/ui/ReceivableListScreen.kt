package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.ReceivableEntity
import com.example.data.ReceivablePayment
import com.example.ui.theme.CleanMinBackground
import com.example.ui.theme.CleanMinOnPrimaryContainer
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.util.CurrencyUtils
import com.example.util.ExcelExportManager
import com.example.util.WhatsAppUtils
import kotlinx.coroutines.launch

@Composable
fun ReceivableListScreen(
    viewModel: ReceivableViewModel,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onOpenBackupDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val receivables by viewModel.receivables.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Dialog states
    var showAddEditDialog by remember { mutableStateOf(false) }
    var receivableToEdit by remember { mutableStateOf<ReceivableEntity?>(null) }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var targetReceivableForAddItem by remember { mutableStateOf<ReceivableEntity?>(null) }

    var showMakePaymentDialog by remember { mutableStateOf(false) }
    var targetReceivableForPayment by remember { mutableStateOf<ReceivableEntity?>(null) }

    var showEditPaymentDialog by remember { mutableStateOf(false) }
    var targetReceivableForEditPayment by remember { mutableStateOf<ReceivableEntity?>(null) }
    var paymentToEdit by remember { mutableStateOf<ReceivablePayment?>(null) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var receivableToDelete by remember { mutableStateOf<ReceivableEntity?>(null) }
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
            // Top Bar: Screen Title and New Receivable Button
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Alacaklarım",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Müşteri ve firma alacakları & tahsilat takibi",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Malzeme Kataloğu Butonu
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { showMaterialCatalogDialog = true }
                                .testTag("btn_receivable_material_catalog")
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

                        // Excel CSV Paylaş Butonu
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        val csvData = ExcelExportManager.exportReceivablesToCsv(receivables)
                                        val intent = ExcelExportManager.shareCsvFile(
                                            context = context,
                                            filenamePrefix = "Alacaklarim_Listesi",
                                            csvContent = csvData,
                                            subject = "Alacaklar Listesi"
                                        )
                                        context.startActivity(intent)
                                    }
                                }
                                .testTag("btn_export_receivables_csv")
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
                                .testTag("btn_toggle_receivable_search")
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

            // Overview Summary Dashboard Card
            item(key = "summary") {
                ReceivableSummaryCard(
                    summary = summary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Expandable Search Bar (açılır/kapanır)
            item(key = "search") {
                AnimatedVisibility(
                    visible = isSearchExpanded || searchQuery.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Kişi, firma, telefon, not veya malzeme ara...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Temizle"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("input_receivable_search")
                    )
                }
            }

            // Horizontal Add Button (spans the screen width directly below search bar)
            item(key = "add_button") {
                Button(
                    onClick = {
                        receivableToEdit = null
                        showAddEditDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CleanMinPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("btn_add_receivable_bar")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Yeni Alacak Ekle",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Filter Tabs: Açık Alacak (UNPAID - Default active) -> Vadeliler (if > 0) -> Tahsil Edilen (PAID) -> Tümü (ALL)
            item(key = "filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReceivableFilterChip(
                        text = "Açık Alacak",
                        count = summary.openReceivableCount,
                        isSelected = selectedFilter == ReceivableFilter.UNPAID,
                        onClick = { viewModel.onFilterChange(ReceivableFilter.UNPAID) }
                    )

                    if (summary.dueCount > 0) {
                        ReceivableFilterChip(
                            text = "Vadeliler",
                            count = summary.dueCount,
                            isSelected = selectedFilter == ReceivableFilter.DUE_DATE,
                            onClick = { viewModel.onFilterChange(ReceivableFilter.DUE_DATE) }
                        )
                    }

                    ReceivableFilterChip(
                        text = "Tahsil Edilen",
                        count = summary.paidOffCount,
                        isSelected = selectedFilter == ReceivableFilter.PAID,
                        onClick = { viewModel.onFilterChange(ReceivableFilter.PAID) }
                    )

                    ReceivableFilterChip(
                        text = "Tümü",
                        count = summary.totalCount,
                        isSelected = selectedFilter == ReceivableFilter.ALL,
                        onClick = { viewModel.onFilterChange(ReceivableFilter.ALL) }
                    )
                }
            }

            // Main List or Empty State
            if (receivables.isEmpty()) {
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
                                text = when {
                                    searchQuery.isNotBlank() -> "Aramanıza uygun alacak kaydı bulunamadı."
                                    selectedFilter == ReceivableFilter.UNPAID -> "Şu anda bekleyen açık alacağınız bulunmuyor."
                                    selectedFilter == ReceivableFilter.PAID -> "Henüz tamamen tahsil edilmiş bir kayıt yok."
                                    else -> "Henüz kaydedilmiş bir alacak bulunmuyor."
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            if (searchQuery.isBlank() && selectedFilter != ReceivableFilter.ALL) {
                                OutlinedButton(
                                    onClick = { viewModel.onFilterChange(ReceivableFilter.ALL) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Tüm Alacakları Göster")
                                }
                            } else if (searchQuery.isBlank()) {
                                Button(
                                    onClick = {
                                        receivableToEdit = null
                                        showAddEditDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("İlk Alacağı Ekle")
                                }
                            }
                        }
                    }
                }
            } else {
                items(receivables, key = { it.id }) { receivable ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        ReceivableCard(
                            receivable = receivable,
                            onEditClick = {
                                receivableToEdit = receivable
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                receivableToDelete = receivable
                                showDeleteConfirmDialog = true
                            },
                            onAddItemClick = {
                                targetReceivableForAddItem = receivable
                                showAddItemDialog = true
                            },
                            onRemoveItemClick = { itemId ->
                                viewModel.removeItemFromReceivable(receivable.id, itemId)
                            },
                            onMakePaymentClick = {
                                targetReceivableForPayment = receivable
                                showMakePaymentDialog = true
                            },
                            onEditPaymentClick = { payment ->
                                targetReceivableForEditPayment = receivable
                                paymentToEdit = payment
                                showEditPaymentDialog = true
                            },
                            onRemovePaymentClick = { paymentId ->
                                viewModel.removePaymentFromReceivable(receivable.id, paymentId)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add or Edit Receivable Dialog
    if (showAddEditDialog) {
        AddEditReceivableDialog(
            initialReceivable = receivableToEdit,
            onDismiss = {
                showAddEditDialog = false
                receivableToEdit = null
            },
            onConfirm = { personOrCompany, phone, notes, initialItemTitle, initialItemAmount, dueDate ->
                if (receivableToEdit != null) {
                    viewModel.updateReceivableInfo(
                        receivableId = receivableToEdit!!.id,
                        personOrCompany = personOrCompany,
                        phoneNumber = phone,
                        notes = notes,
                        dueDate = dueDate
                    )
                } else {
                    viewModel.addReceivable(
                        personOrCompany = personOrCompany,
                        phoneNumber = phone,
                        notes = notes,
                        initialItemTitle = initialItemTitle,
                        initialItemAmount = initialItemAmount,
                        dueDate = dueDate
                    )
                }
                showAddEditDialog = false
                receivableToEdit = null
            }
        )
    }

    // Add Item Dialog
    if (showAddItemDialog && targetReceivableForAddItem != null) {
        val target = targetReceivableForAddItem!!
        AddReceivableItemDialog(
            receivableName = target.personOrCompany,
            onDismiss = {
                showAddItemDialog = false
                targetReceivableForAddItem = null
            },
            onConfirm = { title, amount, quantity ->
                viewModel.addItemToReceivable(
                    receivableId = target.id,
                    title = title,
                    amount = amount,
                    quantity = quantity
                )
                showAddItemDialog = false
                targetReceivableForAddItem = null
            }
        )
    }

    // Make Payment (Tahsilat Yap) Dialog
    if (showMakePaymentDialog && targetReceivableForPayment != null) {
        val target = targetReceivableForPayment!!
        MakeReceivablePaymentDialog(
            receivable = target,
            onDismiss = {
                showMakePaymentDialog = false
                targetReceivableForPayment = null
            },
            onConfirm = { amount, note, updatedPhone, sendWhatsApp ->
                val currentRemaining = target.calculateRemainingBalance()
                val newRemaining = (currentRemaining - amount).let { if (it < 0.001) 0.0 else it }

                viewModel.makePayment(
                    receivableId = target.id,
                    amount = amount,
                    note = note,
                    updatedPhoneNumber = updatedPhone
                )

                // Send WhatsApp notification if requested
                if (sendWhatsApp && updatedPhone.isNotBlank()) {
                    val message = WhatsAppUtils.buildReceivablePaymentNotificationMessage(
                        personOrCompany = target.personOrCompany,
                        paidAmount = amount,
                        paymentNote = note,
                        remainingBalance = newRemaining,
                        receivableNotes = target.notes
                    )
                    WhatsAppUtils.openWhatsApp(context, updatedPhone, message)
                }

                showMakePaymentDialog = false
                targetReceivableForPayment = null
            }
        )
    }

    // Edit Payment Dialog
    if (showEditPaymentDialog && targetReceivableForEditPayment != null && paymentToEdit != null) {
        val target = targetReceivableForEditPayment!!
        val payment = paymentToEdit!!
        EditReceivablePaymentDialog(
            initialPayment = payment,
            onDismiss = {
                showEditPaymentDialog = false
                targetReceivableForEditPayment = null
                paymentToEdit = null
            },
            onConfirm = { updatedAmount, updatedNote ->
                viewModel.updatePayment(
                    receivableId = target.id,
                    updatedPayment = payment.copy(
                        amount = updatedAmount,
                        note = updatedNote
                    )
                )
                showEditPaymentDialog = false
                targetReceivableForEditPayment = null
                paymentToEdit = null
            }
        )
    }

    // Confirm Delete Dialog
    if (showDeleteConfirmDialog && receivableToDelete != null) {
        val target = receivableToDelete!!
        ConfirmDeleteReceivableDialog(
            receivableName = target.personOrCompany,
            onDismiss = {
                showDeleteConfirmDialog = false
                receivableToDelete = null
            },
            onConfirm = {
                viewModel.deleteReceivable(target)
                showDeleteConfirmDialog = false
                receivableToDelete = null
            }
        )
    }

    // Material Catalog Dialog
    if (showMaterialCatalogDialog) {
        MaterialCatalogDialog(
            onDismissRequest = { showMaterialCatalogDialog = false }
        )
    }
}

@Composable
private fun ReceivableSummaryCard(
    summary: ReceivableSummary,
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
            // Total Receivables
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Toplam Alacak",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = CurrencyUtils.formatCurrency(summary.totalItemsAmount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Total Paid / Collected
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tahsil Edilen",
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

            // Remaining Balance (Kalan Alacak)
            Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                Text(
                    text = "Kalan Alacak",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (summary.totalRemainingBalance > 0.001) Color(0xFF0284C7) else Color(0xFF15803D),
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = CurrencyUtils.formatCurrency(summary.totalRemainingBalance),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (summary.totalRemainingBalance > 0.001) Color(0xFF0284C7) else Color(0xFF15803D)
                    )
                )
            }
        }
    }
}

@Composable
private fun ReceivableFilterChip(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) CleanMinPrimary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isSelected) CleanMinPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = CircleShape,
                color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}
