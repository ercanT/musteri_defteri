package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CustomerEntity
import com.example.data.CustomerStatus
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.ui.theme.CleanMinPrimaryDark
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.StatusCompletedBadgeBgDark
import com.example.ui.theme.StatusCompletedBadgeTextDark
import com.example.ui.theme.StatusCompletedBorder
import com.example.ui.theme.StatusCompletedCardBg
import com.example.ui.theme.StatusCompletedCardBgDark
import com.example.ui.theme.StatusCompletedTitle
import com.example.ui.theme.StatusPaidBadgeBgDark
import com.example.ui.theme.StatusPaidBorder
import com.example.ui.theme.StatusPaidCardBg
import com.example.ui.theme.StatusPaidCardBgDark
import com.example.ui.theme.StatusPaidTitle
import com.example.ui.theme.StatusPreparingBadgeBgDark
import com.example.ui.theme.StatusPreparingBorder
import com.example.ui.theme.StatusPreparingCardBg
import com.example.ui.theme.StatusPreparingCardBgDark
import com.example.ui.theme.StatusPreparingTitle
import com.example.util.CurrencyUtils
import com.example.util.WhatsAppUtils

private data class CardStyleConfig(
    val backgroundColor: Color,
    val borderColor: Color,
    val titleColor: Color,
    val alpha: Float,
    val noteBackground: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerCard(
    customer: CustomerEntity,
    onStatusChange: (CustomerStatus) -> Unit,
    onToggleFavorite: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false
) {
    var isExpanded by rememberSaveable { mutableStateOf(initialExpanded) }
    val context = LocalContext.current
    val currentStatus = CustomerStatus.fromString(customer.status)
    val isDark = LocalThemeIsDark.current

    val styleConfig = when (currentStatus) {
        CustomerStatus.PARASI_GELDI -> CardStyleConfig(
            backgroundColor = if (isDark) StatusPaidCardBgDark else StatusPaidCardBg,
            borderColor = if (isDark) StatusPaidBadgeBgDark.copy(alpha = 0.4f) else StatusPaidBorder,
            titleColor = if (isDark) CleanMinPrimaryDark else StatusPaidTitle,
            alpha = 1.0f,
            noteBackground = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.5f)
        )
        CustomerStatus.HAZIRLANIYOR -> CardStyleConfig(
            backgroundColor = if (isDark) StatusPreparingCardBgDark else StatusPreparingCardBg,
            borderColor = if (isDark) StatusPreparingBadgeBgDark.copy(alpha = 0.4f) else StatusPreparingBorder,
            titleColor = if (isDark) Color(0xFFF0F0F7) else StatusPreparingTitle,
            alpha = 1.0f,
            noteBackground = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.4f)
        )
        CustomerStatus.TAMAMLANDI -> CardStyleConfig(
            backgroundColor = if (isDark) StatusCompletedCardBgDark else StatusCompletedCardBg,
            borderColor = if (isDark) StatusCompletedBadgeBgDark.copy(alpha = 0.35f) else StatusCompletedBorder,
            titleColor = if (isDark) StatusCompletedBadgeTextDark else StatusCompletedTitle,
            alpha = 0.75f,
            noteBackground = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF8FAFC)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(styleConfig.alpha)
            .animateContentSize(animationSpec = tween(durationMillis = 200))
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            if (customer.isFavorite) 1.5.dp else 1.dp,
            if (customer.isFavorite) Color(0xFFFFB300) else styleConfig.borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (customer.isFavorite) Color(0xFFFFFDF5) else styleConfig.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (customer.isFavorite) 2.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row: Customer Name (Tam genişlikte) & Sadece Expand [+] Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sol: Avatar ve Müşteri Adı + Favori Yıldızı
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    InitialsAvatar(name = customer.fullName)

                    Text(
                        text = customer.fullName.ifBlank { "İsimsiz Müşteri" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = styleConfig.titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Favorite Star Button (Yellow when selected)
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("btn_favorite_customer_${customer.id}")
                    ) {
                        Icon(
                            imageVector = if (customer.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (customer.isFavorite) "Favorilerden Çıkar" else "Favoriye Ekle",
                            tint = if (customer.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sağ: SADECE [+] / [-] Açma-Kapama Butonu (Durum ifadesi ismi sıkıştırmasın diye alta alındı)
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
                        .testTag("btn_expand_customer_${customer.id}")
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

            // Alt Satır: Telefon Numarası ve Durum İfadesi Rozeti (Örn: Parası Geldi, Hazırlanıyor vs.)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp, end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Telefon Numarası
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (customer.phoneNumber.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = CleanMinPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = customer.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Tel yok • ID: #${customer.id}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        )
                    }

                    if (customer.isFavorite) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFF8E1),
                            border = BorderStroke(0.5.dp, Color(0xFFFFD54F))
                        ) {
                            Text(
                                text = "★ Önemli",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF57F17)
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Durum İfadesi Rozeti (Kişinin adını engellememesi için alta yerleştirildi)
                CustomerStatusBadge(status = currentStatus)
            }

            // Expandable Full Details Section (+ butonuna basıldığında açılır)
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

                    // Gelen Para Tutarı / KDV Dahil & KDV Hariç Bölümü
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (customer.amount > 0) CleanMinPrimaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            1.dp,
                            if (customer.amount > 0) CleanMinPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_section_${customer.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = if (customer.amount > 0) CleanMinPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "KDV DAHİL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = if (customer.amount > 0) CleanMinPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Text(
                                    text = CurrencyUtils.formatCurrency(customer.amount),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = styleConfig.titleColor
                                    ),
                                    modifier = Modifier.testTag("text_kdv_dahil_${customer.id}")
                                )
                            }

                            // KDV Hariç ve KDV Miktarı Kutucuğu
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = "KDV Hariç (%20)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = CurrencyUtils.formatCurrency(CurrencyUtils.calculateKdvHaric(customer.amount)),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.testTag("text_kdv_haric_${customer.id}")
                                    )
                                }
                            }
                        }
                    }

                    // Phone & Communication Actions Row (WhatsApp Business + Call)
                    if (customer.phoneNumber.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Telefon",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = customer.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // WhatsApp Business link button
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF25D366).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            openCustomerWhatsApp(
                                                context = context,
                                                phoneNumber = customer.phoneNumber,
                                                fullName = customer.fullName,
                                                address = customer.address,
                                                extraNotes = customer.extraNotes
                                            )
                                        }
                                        .testTag("btn_whatsapp_${customer.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_whatsapp),
                                            contentDescription = "WhatsApp Business",
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(16.dp)
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

                                // Call button
                                IconButton(
                                    onClick = { dialPhoneNumber(context, customer.phoneNumber) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("btn_call_${customer.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Ara",
                                        tint = CleanMinPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Address Row
                    if (customer.address.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openMapAddress(context, customer.address) }
                                .padding(vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Adres",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Text(
                                text = customer.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Clean Minimalist Note Box
                    if (customer.extraNotes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = styleConfig.noteBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Not: ${customer.extraNotes}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Quick Status Switch Section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Durumu Değiştir:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CustomerStatus.entries.forEach { statusOption ->
                                val isSelected = statusOption == currentStatus
                                QuickStatusChip(
                                    status = statusOption,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (!isSelected) {
                                            onStatusChange(statusOption)
                                        }
                                    },
                                    modifier = Modifier.testTag("chip_status_${statusOption.name.lowercase()}_${customer.id}")
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )

                    // Bottom Actions: Edit & Delete (and WhatsApp Share if phone is empty)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (customer.phoneNumber.isBlank()) {
                            IconButton(
                                onClick = {
                                    openCustomerWhatsApp(
                                        context = context,
                                        phoneNumber = "",
                                        fullName = customer.fullName,
                                        address = customer.address,
                                        extraNotes = customer.extraNotes
                                    )
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_whatsapp_share_${customer.id}")
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp ile Paylaş",
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_edit_customer_${customer.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Düzenle",
                                tint = CleanMinPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_delete_customer_${customer.id}")
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
            }
        }
    }
}

@Composable
fun QuickStatusChip(
    status: CustomerStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = getStatusStyle(status)
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) style.backgroundColor else Color.Transparent,
        label = "chip_bg"
    )
    val textColor = if (isSelected) style.textColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) style.textColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        color = bgColor,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = style.label,
                color = textColor,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val initials = name.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(CleanMinPrimary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = CleanMinPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

private fun openCustomerWhatsApp(
    context: Context,
    phoneNumber: String,
    fullName: String,
    address: String,
    extraNotes: String
) {
    val message = WhatsAppUtils.buildCustomerCargoMessage(
        fullName = fullName,
        address = address,
        extraNotes = extraNotes
    )
    WhatsAppUtils.openWhatsApp(context, phoneNumber, message)
}

private fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.replace(" ", "")}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Arama başlatılamadı", Toast.LENGTH_SHORT).show()
    }
}

private fun openMapAddress(context: Context, address: String) {
    try {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Harita açılamadı", Toast.LENGTH_SHORT).show()
    }
}


