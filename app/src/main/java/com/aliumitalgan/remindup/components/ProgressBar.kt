package com.aliumitalgan.remindup.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme

@Composable
fun ProgressBar(progress: Float) {
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview(showBackground = true)
@Composable
fun ProgressBarPreview() {
    RemindUpTheme {
        ProgressBar(progress = 0.7f)  // %70 Progress
    }
}
