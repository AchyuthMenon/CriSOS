package com.crisos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crisos.app.ui.components.*
import com.crisos.app.ui.theme.*
import com.crisos.app.ui.viewmodel.MessageListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivedAlertsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    viewModel: MessageListViewModel = viewModel()
) {
    val receivedMessages by viewModel.receivedMessages.collectAsState()

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Received SOS Alerts",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        }
    ) { paddingValues ->
        if (receivedMessages.isEmpty()) {
            EmptyState(
                icon = Icons.Default.NotificationsNone,
                title = "No Received Alerts",
                subtitle = "SOS messages from nearby devices will appear here.\nMake sure Nearby Communication is active.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(
                    items = receivedMessages,
                    key = { it.id }
                ) { message ->
                    SosMessageCard(
                        message = message,
                        onClick = { onNavigateToDetails(message.id) }
                    )
                }
            }
        }
    }
}
