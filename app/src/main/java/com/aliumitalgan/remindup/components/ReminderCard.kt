package com.aliumitalgan.remindup.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme
import com.aliumitalgan.remindup.R

@Composable
fun ReminderCard(reminderTitle: String, reminderTime: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = reminderTitle, style = MaterialTheme.typography.bodyLarge, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.time, reminderTime),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderCardPreview() {
    RemindUpTheme {
        ReminderCard(reminderTitle = "Take your Medicine", reminderTime = "08:00 AM")
    }
}
