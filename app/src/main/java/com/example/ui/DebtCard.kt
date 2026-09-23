package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DebtEntity
import com.example.data.DebtItem
import com.example.data.DebtPayment
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.WhatsAppUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Single-box representation of a Debt record matching user specification:
 * - Header: Person/Company name + contact actions
 * - Center: List of purchased materials/items with + Add Item button
 * - Bottom: Balance calculation, Make Payment button, and remaining debt indicator
 */
@Composable
fun DebtCard(
    debt: DebtEntity,
    onAddItemClick: () -> Unit,
    onRemoveItemClick: (itemId: String) -> Unit,
    onMakePaymentClick: () -> Unit,
    onEditPaymentClick: (payment: DebtPayment) -> Unit,
    onRemovePaymentClick: (paymentId: String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false
) {
    var isExpanded by rememberSaveable { mutableStateOf(initialExpanded) }
    val context = LocalContext.current
    val items = debt.getItems()
    val payments = debt.getPayments()
    val totalAmount = debt.calculateTotalAmount()
    val totalPaid = debt.calculateTotalPaid()
    val remainingBalance = debt.calculateRemainingBalance()
    val isPaidOff = debt.isFullyPaid()

    var showPaymentHistory by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 200))
            .testTag("debt_card_${debt.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isPaidOff) Color(0xFF81C784).copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // TOP HEADER: Firma Adı (Tam genişlikte) ve Sadece [+] Genişletme Butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol: Avatar ve Firma/Kişi Adı
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InitialsAvatar(name = debt.personOrCompany)

                    Text(
                        text = debt.personOrCompany.ifBlank { "İsimsiz Firma" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Sağ: Sadece [+] / [-] Genişletme Butonu (Durum ve tutar ismi sıkıştırmasın diye alta alındı)
                Surface(
                    shape = CircleShape,
                    color = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else CleanMinPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        if (isExpanded) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else CleanMinPrimary.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { isExpanded = !isExpanded }
                        .testTag("btn_expand_debt_${debt.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Remove else Icons.Default.Add,
                            contentDescription = if (isExpanded) "Detayları Kapat" else "Tüm Detayları Göster",
                            tint = if (isExpanded) MaterialTheme.colorScheme.onSurfaceVariant else CleanMinPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Alt Satır: Durum İfadesi ("Borç Kapandı", "Açık Borç", "Kısmi Ödendi") ve Borç Bakiyesi Rozeti
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp, end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sol: Durum İfadesi Rozeti ve Vade Tarihi Rozeti
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Durum İfadesi Rozeti (İsmin altında, ferahça görünür)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            isPaidOff -> Color(0xFFE8F5E9)
                            totalPaid > 0.001 -> Color(0xFFFFF8E1)
                            else -> Color(0xFFFFEBEE)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isPaidOff -> Color(0xFF81C784)
                                totalPaid > 0.001 -> Color(0xFFFFD54F)
                                else -> Color(0xFFFFCDD2)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    isPaidOff -> Icons.Default.CheckCircle
                                    totalPaid > 0.001 -> Icons.Default.Payment
                                    else -> Icons.Default.HourglassTop
                                },
                                contentDescription = null,
                                tint = when {
                                    isPaidOff -> Color(0xFF2E7D32)
                                    totalPaid > 0.001 -> Color(0xFFF57F17)
                                    else -> Color(0xFFD32F2F)
                                },
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = when {
                                    isPaidOff -> "Borç Kapandı"
                                    totalPaid > 0.001 -> "Kısmi Ödendi"
                                    else -> "Açık Borç"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when {
                                        isPaidOff -> Color(0xFF2E7D32)
                                        totalPaid > 0.001 -> Color(0xFFF57F17)
                                        else -> Color(0xFFD32F2F)
                                    }
                                )
                            )
                        }
                    }

                    // Vade Tarihi Rozeti (Borç kapanmadıysa ve vade tarihi belirlenmişse)
                    val dueStatus = DateUtils.getDueDateStatus(debt.dueDate)
                    if (dueStatus != null && !isPaidOff) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when {
                                dueStatus.isOverdue -> Color(0xFFFFEBEE)
                                dueStatus.isDueToday -> Color(0xFFFFF3E0)
                                else -> Color(0xFFE3F2FD)
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    dueStatus.isOverdue -> Color(0xFFEF5350)
                                    dueStatus.isDueToday -> Color(0xFFFFB74D)
                                    else -> Color(0xFF90CAF9)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = when {
                                        dueStatus.isOverdue -> Color(0xFFC62828)
                                        dueStatus.isDueToday -> Color(0xFFE65100)
                                        else -> Color(0xFF1565C0)
                                    },
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = dueStatus.badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = when {
                                            dueStatus.isOverdue -> Color(0xFFC62828)
                                            dueStatus.isDueToday -> Color(0xFFE65100)
                                            else -> Color(0xFF1565C0)
                                        }
                                    )
                                )
                            }
                        }
                    }
                }

                // Borç Bakiyesi Rozeti
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPaidOff) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    border = BorderStroke(
                        1.dp,
                        if (isPaidOff) Color(0xFF81C784) else Color(0xFFFFCDD2)
                    )
                ) {
                    Text(
                        text = if (isPaidOff) "0,00 ₺" else CurrencyUtils.formatCurrency(remainingBalance),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = if (isPaidOff) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        ),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("text_debt_balance_${debt.id}")
                    )
                }
            }

            // AÇILABİLİR TÜM DETAYLAR (Sadece [+] butonuna basıldığında görünür)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )

                    // Vade Tarihi Detayı (Genişletildiğinde gösterilir)
                    if (debt.dueDate != null) {
                        val dueStatus = DateUtils.getDueDateStatus(debt.dueDate)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                dueStatus?.isOverdue == true -> Color(0xFFFFEBEE)
                                dueStatus?.isDueToday == true -> Color(0xFFFFF3E0)
                                else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    dueStatus?.isOverdue == true -> Color(0xFFFFCDD2)
                                    dueStatus?.isDueToday == true -> Color(0xFFFFCC80)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = when {
                                            dueStatus?.isOverdue == true -> Color(0xFFC62828)
                                            dueStatus?.isDueToday == true -> Color(0xFFE65100)
                                            else -> CleanMinPrimary
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Vade Tarihi: ${DateUtils.formatDate(debt.dueDate)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (dueStatus != null) {
                                            Text(
                                                text = dueStatus.badgeText,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = when {
                                                    dueStatus.isOverdue -> Color(0xFFC62828)
                                                    dueStatus.isDueToday -> Color(0xFFE65100)
                                                    else -> CleanMinPrimary
                                                }
                                            )
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = onEditClick,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Değiştir", fontSize = 11.sp, color = CleanMinPrimary)
                                }
                            }
                        }
                    }

                    // Ekstra Not / Açıklama Alanı (WhatsApp Gönderme Butonu ile)
                    if (debt.notes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Notes,
                                        contentDescription = null,
                                        tint = CleanMinPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Ekstra Not / Açıklama",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = CleanMinPrimary,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Text(
                                            text = debt.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // WhatsApp ile sadece bu ekstra alanı / notu paylaşma butonu
                                IconButton(
                                    onClick = { openWhatsAppDebtNote(context, debt) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("btn_whatsapp_note_${debt.id}")
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_whatsapp),
                                        contentDescription = "Ekstra Alanı WhatsApp ile Gönder",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quick actions (WhatsApp, Phone Call, Edit, Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // WhatsApp Button (Ekstra alan ve tüm hesap özeti dahil)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF25D366).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        openWhatsAppDebt(
                                            context = context,
                                            debt = debt
                                        )
                                    }
                                    .testTag("btn_whatsapp_debt_${debt.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_whatsapp),
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF15803D),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "WhatsApp",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            if (debt.phoneNumber.isNotBlank()) {
                                // Call button
                                IconButton(
                                    onClick = { dialPhoneNumberDebt(context, debt.phoneNumber) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Ara",
                                        tint = CleanMinPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = debt.phoneNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_edit_debt_${debt.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Düzenle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_delete_debt_${debt.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Sil",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. CENTER: ALINAN MALZEMELER (MATERIALS/ITEMS)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
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
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = CleanMinPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Alınan Malzemeler (${items.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            // + Malzeme Ekle Button
                            OutlinedButton(
                                onClick = onAddItemClick,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CleanMinPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_add_item_${debt.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = CleanMinPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+ Ürün Ekle",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = CleanMinPrimary
                                    )
                                )
                            }
                        }

                        // Material Items List
                        if (items.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Henüz eklenmiş malzeme yok. Ürün eklemek için '+ Ürün Ekle' butonuna dokunun.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items.forEach { item ->
                                    DebtItemRow(
                                        item = item,
                                        onDelete = { onRemoveItemClick(item.id) }
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )

                                // Items subtotal
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Malzemeler Toplamı:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = CurrencyUtils.formatCurrency(totalAmount),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    // 3. PAYMENT SECTION & BOTTOM BALANCE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Payment history toggle (if payments exist)
                            if (payments.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showPaymentHistory = !showPaymentHistory }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Ödemeler (${CurrencyUtils.formatCurrency(totalPaid)})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    )
                                    Icon(
                                        imageVector = if (showPaymentHistory) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Henüz ödeme yapılmadı",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // "Ödeme Yap" Action Button
                            Button(
                                onClick = onMakePaymentClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPaidOff) Color(0xFF757575) else Color(0xFF2E7D32)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("btn_make_payment_${debt.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+ Ödeme Yap",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Collapsible Payment History
                        AnimatedVisibility(visible = showPaymentHistory && payments.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFE8F5E9).copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")) }

                                payments.forEach { payment ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = CurrencyUtils.formatCurrency(payment.amount),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32)
                                                ),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            val noteText = if (payment.note.isNotBlank()) " • ${payment.note}" else ""
                                            Text(
                                                text = "${dateFormat.format(Date(payment.paymentDate))}$noteText",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onEditPaymentClick(payment) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Ödemeyi Düzenle",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onRemovePaymentClick(payment.id) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Ödemeyi Sil",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 4. ALTA BAKİYE TUTARI (BOTTOM BALANCE BAR - Tek satırda kalır, taşma/kayma yapmaz)
                        Surface(
                            color = if (isPaidOff) Color(0xFFE8F5E9) else CleanMinPrimaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isPaidOff) Color(0xFF81C784) else CleanMinPrimary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "KALAN BAKİYE / BORÇ",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = if (isPaidOff) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (totalPaid > 0) {
                                        Text(
                                            text = "Ödenen: ${CurrencyUtils.formatCurrency(totalPaid)} / Toplam: ${CurrencyUtils.formatCurrency(totalAmount)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    text = if (isPaidOff) "0,00 ₺ (KAPANDI)" else CurrencyUtils.formatCurrency(remainingBalance),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.5.sp,
                                        color = if (isPaidOff) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.testTag("text_expanded_debt_balance_${debt.id}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single row showing an item/material name and price with delete button
 * e.g. "100 cm wallwasher" -> "250,00 ₺"
 */
@Composable
private fun DebtItemRow(
    item: DebtItem,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(CleanMinPrimary, CircleShape)
            )
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.quantity > 1) {
                    Text(
                        text = "${item.quantity} adet x ${CurrencyUtils.formatCurrency(item.amount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = CurrencyUtils.formatCurrency(item.totalLineAmount),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CleanMinPrimary
                )
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Ürünü Sil",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun openWhatsAppDebt(
    context: Context,
    debt: DebtEntity
) {
    val items = debt.getItems()
    val message = WhatsAppUtils.buildDebtSummaryMessage(
        personOrCompany = debt.personOrCompany,
        totalItemsAmount = debt.calculateTotalAmount(),
        totalPaidAmount = debt.calculateTotalPaid(),
        remainingBalance = debt.calculateRemainingBalance(),
        notes = debt.notes,
        items = items
    )
    WhatsAppUtils.openWhatsApp(context, debt.phoneNumber, message)
}

private fun openWhatsAppDebtNote(
    context: Context,
    debt: DebtEntity
) {
    val message = WhatsAppUtils.buildDebtNoteOnlyMessage(
        personOrCompany = debt.personOrCompany,
        notes = debt.notes,
        remainingBalance = debt.calculateRemainingBalance()
    )
    WhatsAppUtils.openWhatsApp(context, debt.phoneNumber, message)
}

private fun dialPhoneNumberDebt(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.replace(" ", "")}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Arama başlatılamadı", Toast.LENGTH_SHORT).show()
    }
}
