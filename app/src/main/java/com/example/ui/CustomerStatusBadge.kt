package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerStatus
import com.example.ui.theme.StatusCompletedBadgeBg
import com.example.ui.theme.StatusCompletedBadgeBgDark
import com.example.ui.theme.StatusCompletedBadgeText
import com.example.ui.theme.StatusCompletedBadgeTextDark
import com.example.ui.theme.StatusPaidBadgeBg
import com.example.ui.theme.StatusPaidBadgeBgDark
import com.example.ui.theme.StatusPaidBadgeText
import com.example.ui.theme.StatusPaidBadgeTextDark
import com.example.ui.theme.StatusPreparingBadgeBg
import com.example.ui.theme.StatusPreparingBadgeBgDark
import com.example.ui.theme.StatusPreparingBadgeText
import com.example.ui.theme.StatusPreparingBadgeTextDark

data class StatusStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val label: String
)

@Composable
fun getStatusStyle(status: CustomerStatus): StatusStyle {
    val isDark = false
    return when (status) {
        CustomerStatus.PARASI_GELDI -> StatusStyle(
            backgroundColor = if (isDark) StatusPaidBadgeBgDark else StatusPaidBadgeBg,
            textColor = if (isDark) StatusPaidBadgeTextDark else StatusPaidBadgeText,
            icon = Icons.Default.CheckCircle,
            label = "PARASI GELDİ"
        )
        CustomerStatus.HAZIRLANIYOR -> StatusStyle(
            backgroundColor = if (isDark) StatusPreparingBadgeBgDark else StatusPreparingBadgeBg,
            textColor = if (isDark) StatusPreparingBadgeTextDark else StatusPreparingBadgeText,
            icon = Icons.Default.HourglassTop,
            label = "HAZIRLANIYOR"
        )
        CustomerStatus.TAMAMLANDI -> StatusStyle(
            backgroundColor = if (isDark) StatusCompletedBadgeBgDark else StatusCompletedBadgeBg,
            textColor = if (isDark) StatusCompletedBadgeTextDark else StatusCompletedBadgeText,
            icon = Icons.Default.DoneAll,
            label = "TAMAMLANDI"
        )
    }
}

@Composable
fun CustomerStatusBadge(
    status: CustomerStatus,
    modifier: Modifier = Modifier
) {
    val style = getStatusStyle(status)
    Box(
        modifier = modifier
            .testTag("status_badge_${status.name.lowercase()}")
            .clip(RoundedCornerShape(50))
            .background(style.backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = style.label,
                color = style.textColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

