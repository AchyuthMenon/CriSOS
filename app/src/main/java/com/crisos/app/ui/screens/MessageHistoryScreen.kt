package com.crisos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crisos.app.domain.model.MessageDirection
import com.crisos.app.ui.components.*
import com.crisos.app.ui.theme.*
import com.crisos.app.ui.viewmodel.MessageListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    viewModel: MessageListViewModel = viewModel()
) {
    val allMessages by viewModel.allMessages.collectAsState()
    val sentMessages by viewModel.sentMessages.collectAsState()
    val receivedMessages by viewModel.receivedMessages.collectAsState()
    val pendingMessages by viewModel.pendingMessages.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Sent", "Received", "Pending")

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Message History",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavyMedium,
                contentColor = EmergencyRed,
                divider = { HorizontalDivider(color = SurfaceBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = EmergencyRed,
                        unselectedContentColor = TextMuted
                    )
                }
            }

            val displayMessages = when (selectedTab) {
                0 -> allMessages
                1 -> sentMessages
                2 -> receivedMessages
                3 -> pendingMessages
                else -> allMessages
            }

            if (displayMessages.isEmpty()) {
                val (emptyIcon, emptyTitle, emptySubtitle) = when (selectedTab) {
                    1 -> Triple(Icons.Default.CallMade, "No Sent Messages", "Your sent SOS alerts will appear here.")
                    2 -> Triple(Icons.Default.CallReceived, "No Received Messages", "Received SOS alerts will appear here.")
                    3 -> Triple(Icons.Default.Schedule, "No Pending Messages", "Messages waiting for delivery will appear here.")
                    else -> Triple(Icons.Default.Inbox, "No Messages", "SOS messages you send or receive will appear here.")
                }
                EmptyState(
                    icon = emptyIcon,
                    title = emptyTitle,
                    subtitle = emptySubtitle,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Summary row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${displayMessages.size} message${if (displayMessages.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = displayMessages,
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
}
