package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomerEntity
import com.example.data.CustomerStatus
import com.example.ui.theme.CleanMinOnPrimaryContainer
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.ui.theme.StatusCompletedBadgeBg
import com.example.ui.theme.StatusCompletedBadgeText
import com.example.ui.theme.StatusPaidBadgeBg
import com.example.ui.theme.StatusPaidBadgeText
import com.example.ui.theme.StatusPreparingBadgeBg
import com.example.ui.theme.StatusPreparingBadgeText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onOpenBackupDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val customers by viewModel.customerList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val statusCounts by viewModel.statusCounts.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("customer_list"),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Clean Minimalism Top Header Bar
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
                            text = "Müşteri Rehberi",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search toggle button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable { isSearchExpanded = !isSearchExpanded }
                                .testTag("btn_toggle_search")
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

            // Expandable or active Search Input Field
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
                            placeholder = {
                                Text(
                                    "Müşteri adı, telefon, adres veya not ara…",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Ara",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.onSearchQueryChange("") },
                                        modifier = Modifier.testTag("btn_clear_search")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Temizle",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = CleanMinPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_search_query")
                        )
                    }
                }
            }

            // Horizontal Add Button (spans the screen width directly below search)
            item(key = "add_button") {
                Button(
                    onClick = {
                        customerToEdit = null
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
                        .testTag("btn_add_customer")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Yeni Müşteri Ekle",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Status Filter Chips (Pill styling: Parası Geldi first/default, Tümü at the end)
            item(key = "filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusFilterChip(
                        label = "Parası Geldi",
                        count = statusCounts.parasiGeldi,
                        isSelected = selectedFilter == CustomerStatus.PARASI_GELDI,
                        icon = Icons.Default.CheckCircle,
                        activeBg = StatusPaidBadgeBg,
                        activeText = StatusPaidBadgeText,
                        onClick = { viewModel.onStatusFilterChange(CustomerStatus.PARASI_GELDI) },
                        modifier = Modifier.testTag("filter_parasi_geldi")
                    )

                    StatusFilterChip(
                        label = "Hazırlanıyor",
                        count = statusCounts.hazirlaniyor,
                        isSelected = selectedFilter == CustomerStatus.HAZIRLANIYOR,
                        icon = Icons.Default.HourglassTop,
                        activeBg = StatusPreparingBadgeBg,
                        activeText = StatusPreparingBadgeText,
                        onClick = { viewModel.onStatusFilterChange(CustomerStatus.HAZIRLANIYOR) },
                        modifier = Modifier.testTag("filter_hazirlaniyor")
                    )

                    StatusFilterChip(
                        label = "Tamamlandı",
                        count = statusCounts.tamamlandi,
                        isSelected = selectedFilter == CustomerStatus.TAMAMLANDI,
                        icon = Icons.Default.DoneAll,
                        activeBg = StatusCompletedBadgeBg,
                        activeText = StatusCompletedBadgeText,
                        onClick = { viewModel.onStatusFilterChange(CustomerStatus.TAMAMLANDI) },
                        modifier = Modifier.testTag("filter_tamamlandi")
                    )

                    StatusFilterChip(
                        label = "Tümü",
                        count = statusCounts.total,
                        isSelected = selectedFilter == null,
                        icon = Icons.Default.People,
                        activeBg = CleanMinPrimaryContainer,
                        activeText = CleanMinOnPrimaryContainer,
                        onClick = { viewModel.onStatusFilterChange(null) },
                        modifier = Modifier.testTag("filter_all")
                    )
                }
            }

            // Customer List or Empty Views
            if (customers.isEmpty()) {
                item(key = "empty_state") {
                    if (statusCounts.total == 0) {
                        EmptyView(
                            title = "Henüz kayıt bulunmuyor",
                            description = "Müşterilerinizi, siparişlerini ve durumlarını yerel olarak cihazınızda tutun.",
                            buttonText = "İlk Müşteriyi Ekle",
                            onButtonClick = {
                                customerToEdit = null
                                showAddEditDialog = true
                            }
                        )
                    } else {
                        EmptyView(
                            title = "Eşleşen müşteri bulunamadı",
                            description = "Arama veya filtre kriterlerinize uygun kayıt yok.",
                            buttonText = "Filtreleri Temizle",
                            onButtonClick = {
                                viewModel.onSearchQueryChange("")
                                viewModel.onStatusFilterChange(null)
                            }
                        )
                    }
                }
            } else {
                items(
                    items = customers,
                    key = { it.id }
                ) { customer ->
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                        CustomerCard(
                            customer = customer,
                            onStatusChange = { newStatus ->
                                viewModel.updateStatus(customer.id, newStatus)
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(customer.id, customer.isFavorite)
                            },
                            onEditClick = {
                                customerToEdit = customer
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                customerToDelete = customer
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditCustomerDialog(
            customerToEdit = customerToEdit,
            onDismiss = {
                showAddEditDialog = false
                customerToEdit = null
            },
            onSave = { fullName, phone, address, notes, status, amount ->
                if (customerToEdit != null) {
                    viewModel.updateCustomer(
                        id = customerToEdit!!.id,
                        fullName = fullName,
                        phoneNumber = phone,
                        address = address,
                        extraNotes = notes,
                        status = status,
                        amount = amount,
                        isFavorite = customerToEdit!!.isFavorite
                    )
                } else {
                    viewModel.addCustomer(
                        fullName = fullName,
                        phoneNumber = phone,
                        address = address,
                        extraNotes = notes,
                        status = status,
                        amount = amount,
                        isFavorite = false
                    )
                }
                showAddEditDialog = false
                customerToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (customerToDelete != null) {
        val target = customerToDelete!!
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(text = "Müşteriyi Sil", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "${target.fullName} (ID: #${target.id}) kaydını kalıcı olarak silmek istediğinize emin misiniz? Bu işlem cihazınızdaki veritabanından kalıcı olarak kaldıracaktır."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(target.id)
                        customerToDelete = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("dialog_btn_confirm_delete")
                ) {
                    Text("Evet, Sil")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { customerToDelete = null },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("dialog_btn_cancel_delete")
                ) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
fun StatusFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    icon: ImageVector,
    activeBg: Color,
    activeText: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) activeBg else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) activeBg else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeText else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "$label ($count)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                color = if (isSelected) activeText else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyView(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(CleanMinPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = CleanMinOnPrimaryContainer,
                    modifier = Modifier.size(38.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CleanMinPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("empty_view_action_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

