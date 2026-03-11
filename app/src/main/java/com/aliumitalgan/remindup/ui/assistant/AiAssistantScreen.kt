package com.aliumitalgan.remindup.ui.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.domain.model.AiResponseSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: AiAssistantViewModel = viewModel(
        factory = AiAssistantViewModelFactory(LocalAppContainer.current)
    )
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Text(
                        text = stringResource(R.string.assistant_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.assistant_subtitle),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                OutlinedTextField(
                    value = state.goalInput,
                    onValueChange = { viewModel.onEvent(AiAssistantUiEvent.GoalChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text(stringResource(R.string.assistant_input_label)) }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.onEvent(AiAssistantUiEvent.GenerateClicked) }
                    ) {
                        Text(stringResource(R.string.assistant_generate))
                    }
                    TextButton(onClick = onNavigateToPremium) {
                        Text(stringResource(R.string.assistant_upgrade_cta))
                    }
                }
            }

            state.message?.let { message ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (state.isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            if (state.subtasks.isNotEmpty()) {
                item {
                    Text(
                        text = when (state.source) {
                            AiResponseSource.MODEL -> stringResource(R.string.assistant_source_ai)
                            AiResponseSource.FALLBACK -> stringResource(R.string.assistant_source_fallback)
                            null -> ""
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (!state.isLoading) {
                item {
                    Text(
                        text = stringResource(R.string.assistant_empty_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            itemsIndexed(state.subtasks) { index, item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
