package com.crisos.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crisos.app.ui.components.*
import com.crisos.app.ui.theme.*
import com.crisos.app.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSendSos: () -> Unit,
    onNavigateToReceivedAlerts: () -> Unit,
    onNavigateToMessageHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val messageCount by viewModel.messageCount.collectAsState()
    val receivedCount by viewModel.receivedCount.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()
    val connectedDevices by viewModel.connectedDevices.collectAsState()
    val connectedPeerNames by viewModel.connectedPeerNames.collectAsState()
    val isNearbyActive by viewModel.isNearbyActive.collectAsState()

    // Refresh status when screen is shown
    LaunchedEffect(Unit) {
        viewModel.refreshDeviceStatus()
    }

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyDark
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // App branding
            Text(
                text = "🆘",
                fontSize = 36.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "CriSOS",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 2.sp
            )
            Text(
                text = "Crisis + SOS",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Emergency communication when networks fail",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Connection status bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .animateContentSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Device Status",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isNearbyActive) {
                                IconButton(
                                    onClick = { viewModel.rescanNow() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Re-scan peers",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            TextButton(
                                onClick = { viewModel.toggleNearby() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (isNearbyActive) StatusGreen else EmergencyRed
                                )
                            ) {
                                Text(
                                    text = if (isNearbyActive) "Active" else "Start",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusIndicator(
                            label = if (deviceStatus.isLocationPermissionGranted) "Location" else "No Location",
                            isActive = deviceStatus.isLocationPermissionGranted && deviceStatus.isLocationEnabled
                        )
                        StatusIndicator(
                            label = "Nearby",
                            isActive = isNearbyActive,
                            activeColor = StatusBlue
                        )
                        StatusIndicator(
                            label = "${connectedDevices.size} Connected",
                            isActive = connectedDevices.isNotEmpty(),
                            activeColor = StatusGreen
                        )
                    }

                    if (isNearbyActive) {
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = SurfaceBorder)
                        Spacer(Modifier.height(8.dp))
                        if (connectedPeerNames.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(StatusGreen, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                                Text(
                                    text = "Connected: ${connectedPeerNames.values.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else if (deviceStatus.discoveredDeviceCount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(StatusBlue, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                                Text(
                                    text = "Discovered ${deviceStatus.discoveredDeviceCount} nearby peer(s) — connecting...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusBlue
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(TextMuted, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                                Text(
                                    text = "Scanning continuously for nearby devices...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // SOS Button
            SosButton(
                onClick = onNavigateToSendSos,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(28.dp))

            // Quick actions
            SectionHeader(title = "Quick Actions")
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.MyLocation,
                    label = "My Location",
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Default.NotificationsActive,
                    label = "Received",
                    badge = if (receivedCount > 0) receivedCount.toString() else null,
                    onClick = onNavigateToReceivedAlerts,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Default.History,
                    label = "History",
                    badge = if (messageCount > 0) messageCount.toString() else null,
                    onClick = onNavigateToMessageHistory,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Safety notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = EmergencyRed.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = EmergencyRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Messages reach nearby CriSOS devices via Bluetooth & WiFi Direct. " +
                                "Coverage depends on participating devices in range.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
