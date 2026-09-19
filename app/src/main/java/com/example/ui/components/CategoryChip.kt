package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SkillCategory

@Composable
fun CategoryChip(
    category: SkillCategory,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = getCategoryIcon(category.iconName)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category.name,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = category.name,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Code" -> Icons.Default.Code
        "Language" -> Icons.Default.Language
        "Smartphone" -> Icons.Default.PhoneAndroid
        "Palette" -> Icons.Default.Palette
        "Brush" -> Icons.Default.Brush
        "CameraAlt" -> Icons.Default.CameraAlt
        "MusicNote" -> Icons.Default.MusicNote
        "Piano" -> Icons.Default.Piano
        "RecordVoiceOver" -> Icons.Default.RecordVoiceOver
        "Translate" -> Icons.Default.Translate
        "Psychology" -> Icons.Default.Psychology
        "Analytics" -> Icons.Default.Analytics
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Restaurant" -> Icons.Default.Restaurant
        "TrendingUp" -> Icons.Default.TrendingUp
        "Category" -> Icons.Default.Category
        else -> Icons.Default.MoreHoriz
    }
}
