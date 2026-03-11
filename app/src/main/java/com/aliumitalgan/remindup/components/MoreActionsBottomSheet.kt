package com.aliumitalgan.remindup.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.appCardColor
import com.aliumitalgan.remindup.ui.theme.appTextPrimary
import com.aliumitalgan.remindup.ui.theme.appTextSecondary
import com.aliumitalgan.remindup.ui.theme.themedColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreActionsBottomSheet(
    onDismiss: () -> Unit,
    onShareProfile: () -> Unit,
    onExportData: () -> Unit,
    onHelpCenter: () -> Unit,
    onReportProblem: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = appCardColor,
        tonalElevation = 0.dp,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = themedColor(Color(0xFFDCE4F0), Color(0xFF334155))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "More Actions",
                color = appTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            MoreActionCard(
                icon = Icons.Filled.Share,
                iconTint = Color(0xFFF97316),
                iconBackground = Color(0xFFFFE7D9),
                title = "Share Profile",
                subtitle = "Let others see your productivity",
                onClick = onShareProfile
            )
            MoreActionCard(
                icon = Icons.Filled.Download,
                iconTint = Color(0xFF3B82F6),
                iconBackground = Color(0xFFE4EEFF),
                title = "Export My Data",
                subtitle = "Download your task history (JSON/CSV)",
                onClick = onExportData
            )
            MoreActionCard(
                icon = Icons.AutoMirrored.Filled.Help,
                iconTint = Color(0xFF22A559),
                iconBackground = Color(0xFFDDF6E6),
                title = "Help Center",
                subtitle = "Tutorials and FAQ for RemindUp",
                onClick = onHelpCenter
            )
            MoreActionCard(
                icon = Icons.Filled.ReportProblem,
                iconTint = Color(0xFFEF4444),
                iconBackground = Color(0xFFFFE3E3),
                title = "Report a Problem",
                subtitle = "Let us know if something is broken",
                titleColor = Color(0xFFEF4444),
                backgroundColor = Color(0xFFFFF0F0),
                arrowColor = Color(0xFFEF4444),
                onClick = onReportProblem
            )
        }
    }
}

@Composable
private fun MoreActionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = themedColor(Color(0xFF374151), appTextPrimary),
    backgroundColor: Color = themedColor(Color(0xFFFFF7F4), Color(0xFF1F2937)),
    arrowColor: Color = themedColor(Color(0xFF94A3B8), Color(0xFFAEB6C5))
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = subtitle,
                color = appTextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = arrowColor
        )
    }
}
