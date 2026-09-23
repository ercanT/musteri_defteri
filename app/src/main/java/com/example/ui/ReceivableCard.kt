package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ReceivableEntity
import com.example.data.ReceivableItem
import com.example.data.ReceivablePayment
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.WhatsAppUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceivableCard(
    receivable: ReceivableEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onRemoveItemClick: (itemId: String) -> Unit,
    onMakePaymentClick: () -> Unit,
    onEditPaymentClick: (payment: ReceivablePayment) -> Unit,
    onRemovePaymentClick: (paymentId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items = receivable.getItems()
    val payments = receivable.getPayments()

    val totalAmount = receivable.calculateTotalAmount()
    val totalPaid = receivable.calculateTotalPaid()
    val remainingBalance = receivable.calculateRemainingBalance()
    val isFullyPaid = receivable.isFullyPaid()
    val hasBalance = remainingBalance > 0.001

    var isExpanded by remember { mutableStateOf(false) }
    var showItemsList by remember { mutableStateOf(true) }
    var showPaymentHistory by remember { mutableStateOf(true) }

    val statusColor = when {
        isFullyPaid -> Color(0xFF15803D) // Green: Fully collected
        hasBalance -> Color(0xFF0284C7)  // Blue/Cyan: Open receivable
        else -> Color(0xFF64748B)        // Neutral
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("receivable_card_${receivable.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = if (hasBalance) 1.5.dp else 1.dp,
            color = if (hasBalance) statusColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: İsim Soyisim / Firma Adı (Tam genişlikte) ve Sadece [+] / [-] Butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol: Avatar ve Firma/Kişi Adı (İsim Soyisim)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InitialsAvatar(name = receivable.personOrCompany)

                    Text(
                        text = receivable.personOrCompany.ifBlank { "İsimsiz Alacak" },
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
                        .testTag("btn_expand_receivable_${receivable.id}")
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

            // Alt Satır: Durum İfadesi ("Tahsil Edildi", "Açık Alacak", "Kısmi Tahsilat") ve Alacak Tutarı Rozeti
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp, end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Durum İfadesi Rozeti ve Vade Rozeti (İsmin altında, ferahça görünür)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            isFullyPaid -> Color(0xFFE8F5E9)
                            totalPaid > 0.001 -> Color(0xFFFFF8E1)
                            else -> Color(0xFFE0F2FE)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isFullyPaid -> Color(0xFF81C784)
                                totalPaid > 0.001 -> Color(0xFFFFD54F)
                                else -> Color(0xFF7DD3FC)
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
                                    isFullyPaid -> Icons.Default.CheckCircle
                                    totalPaid > 0.001 -> Icons.Default.Payments
                                    else -> Icons.Default.Payment
                                },
                                contentDescription = null,
                                tint = when {
                                    isFullyPaid -> Color(0xFF15803D)
                                    totalPaid > 0.001 -> Color(0xFFF57F17)
                                    else -> Color(0xFF0284C7)
                                },
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = when {
                                    isFullyPaid -> "Tahsil Edildi"
                                    totalPaid > 0.001 -> "Kısmi Tahsilat"
                                    else -> "Açık Alacak"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when {
                                        isFullyPaid -> Color(0xFF15803D)
                                        totalPaid > 0.001 -> Color(0xFFF57F17)
                                        else -> Color(0xFF0284C7)
                                    }
                                )
                            )
                        }
                    }

                    // Vade Tarihi Rozeti (Alacak kapanmadıysa ve vade tarihi belirlenmişse)
                    val dueStatus = DateUtils.getDueDateStatus(receivable.dueDate)
                    if (dueStatus != null && !isFullyPaid) {
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

                // Alacak Tutarı Rozeti
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isFullyPaid) Color(0xFFE8F5E9) else Color(0xFFE0F2FE),
                    border = BorderStroke(
                        1.dp,
                        if (isFullyPaid) Color(0xFF81C784) else Color(0xFF7DD3FC)
                    )
                ) {
                    Text(
                        text = if (isFullyPaid) "0,00 ₺" else CurrencyUtils.formatCurrency(remainingBalance),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = if (isFullyPaid) Color(0xFF15803D) else Color(0xFF0284C7)
                        ),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("text_receivable_balance_${receivable.id}")
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
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Durum Rozeti ve WhatsApp / Ara / Düzenle / Sil Hızlı Aksiyonları
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Durum Rozeti
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = statusColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullyPaid) Icons.Default.CheckCircle else Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isFullyPaid) "Tahsil Edildi" else if (hasBalance) "Açık Alacak" else "Bakiye Yok",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                )
                            }
                        }

                        // WhatsApp, Ara, Düzenle, Sil Butonları
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // WhatsApp Hatırlatma Bildirimi
                            IconButton(
                                onClick = {
                                    val message = WhatsAppUtils.buildReceivableWhatsAppMessage(
                                        personOrCompany = receivable.personOrCompany,
                                        remainingBalance = remainingBalance,
                                        notes = receivable.notes
                                    )
                                    WhatsAppUtils.openWhatsApp(context, receivable.phoneNumber, message)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("btn_whatsapp_${receivable.id}")
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp Ödeme Bildirimi",
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Telefon Ara
                            if (receivable.phoneNumber.isNotBlank()) {
                                IconButton(
                                    onClick = { dialPhone(context, receivable.phoneNumber) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("btn_call_${receivable.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Telefonla Ara",
                                        tint = CleanMinPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Düzenle
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("btn_edit_receivable_${receivable.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Düzenle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Sil
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("btn_delete_receivable_${receivable.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Sil",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Main Balance Highlight Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasBalance) {
                                statusColor.copy(alpha = 0.08f)
                            } else {
                                Color(0xFF15803D).copy(alpha = 0.08f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (hasBalance) statusColor.copy(alpha = 0.25f) else Color(0xFF15803D).copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Top: Total, Paid, and Remaining
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Toplam Alacak (KDV Dahil)",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatCurrency(totalAmount),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "KDV Hariç: ${CurrencyUtils.formatCurrency(CurrencyUtils.calculateKdvHaric(totalAmount))}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Tahsil Edilen Tutar",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatCurrency(totalPaid),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF15803D)
                                        )
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Remaining Balance (Kalan Alacak)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (hasBalance) statusColor else Color(0xFF15803D))
                                    )
                                    Text(
                                        text = "Kalan Alacak:",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                Text(
                                    text = CurrencyUtils.formatCurrency(remainingBalance),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (hasBalance) statusColor else Color(0xFF15803D),
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }
                    }

                    // Vade Tarihi Bilgi Kutusu (Eğer Vade Belirlenmişse)
                    if (receivable.dueDate != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val dueStatus = DateUtils.getDueDateStatus(receivable.dueDate)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                dueStatus?.isOverdue == true -> Color(0xFFFFEBEE)
                                dueStatus?.isDueToday == true -> Color(0xFFFFF3E0)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    dueStatus?.isOverdue == true -> Color(0xFFEF5350).copy(alpha = 0.6f)
                                    dueStatus?.isDueToday == true -> Color(0xFFFFB74D).copy(alpha = 0.6f)
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                }
                            ),
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
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = when {
                                            dueStatus?.isOverdue == true -> Color(0xFFC62828)
                                            dueStatus?.isDueToday == true -> Color(0xFFE65100)
                                            else -> CleanMinPrimary
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Tahsilat Vade Tarihi",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = DateUtils.formatDate(receivable.dueDate!!),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (dueStatus != null) {
                                                Text(
                                                    text = "(${dueStatus.badgeText})",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = when {
                                                        dueStatus.isOverdue -> Color(0xFFC62828)
                                                        dueStatus.isDueToday -> Color(0xFFE65100)
                                                        else -> CleanMinPrimary
                                                    }
                                                )
                                            }
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

                    // Notes / Ekstra Alan Section
                    if (receivable.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
                                        imageVector = Icons.Default.Notes,
                                        contentDescription = null,
                                        tint = CleanMinPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = receivable.notes,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (receivable.phoneNumber.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            val msg = "Merhaba ${receivable.personOrCompany.trim()},\n\n📌 Not / Açıklama:\n${receivable.notes.trim()}\n\nİyi günler dilerim."
                                            WhatsAppUtils.openWhatsApp(context, receivable.phoneNumber, msg)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_whatsapp),
                                            contentDescription = "Ekstra Notu WhatsApp ile Gönder",
                                            tint = Color(0xFF25D366),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Items (Malzemeler / Satışlar) Header & Add Item Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showItemsList = !showItemsList }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = CleanMinPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Verilen Malzeme / Kalemler (${items.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(
                                imageVector = if (showItemsList) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Quick Add Item Button
                        OutlinedButton(
                            onClick = onAddItemClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_add_item_${receivable.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CleanMinPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Kalem Ekle",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    // Expanded Items List
                    AnimatedVisibility(
                        visible = showItemsList,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (items.isEmpty()) {
                                Text(
                                    text = "Henüz kayıtlı malzeme veya hizmet kalemi yok.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                items.forEach { item ->
                                    ReceivableItemRow(
                                        item = item,
                                        onRemoveClick = { onRemoveItemClick(item.id) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Payments (Tahsilatlar) Header & Make Payment Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showPaymentHistory = !showPaymentHistory }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Tahsilatlar (${payments.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(
                                imageVector = if (showPaymentHistory) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Quick Make Payment (Tahsilat Yap) Button
                        Button(
                            onClick = onMakePaymentClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_make_payment_${receivable.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Tahsilat Ekle",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                        }
                    }

                    // Expanded Payment History List
                    AnimatedVisibility(
                        visible = showPaymentHistory,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (payments.isEmpty()) {
                                Text(
                                    text = "Henüz yapılmış bir tahsilat bulunmuyor.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                payments.forEach { payment ->
                                    ReceivablePaymentRow(
                                        payment = payment,
                                        onEditClick = { onEditPaymentClick(payment) },
                                        onRemoveClick = { onRemovePaymentClick(payment.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceivableItemRow(
    item: ReceivableItem,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.quantity} Adet × ${CurrencyUtils.formatCurrency(item.amount)} = ${CurrencyUtils.formatCurrency(item.totalLineAmount)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }

            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ReceivablePaymentRow(
    payment: ReceivablePayment,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(payment.paymentDate) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))
        sdf.format(Date(payment.paymentDate))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF15803D).copy(alpha = 0.08f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "+ ${CurrencyUtils.formatCurrency(payment.amount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.5.sp
                        )
                    )
                }
                if (payment.note.isNotBlank()) {
                    Text(
                        text = payment.note,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = CleanMinPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

private fun dialPhone(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.trim()}"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Arama yapılamadı", Toast.LENGTH_SHORT).show()
    }
}
