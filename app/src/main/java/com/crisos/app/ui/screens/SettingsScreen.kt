package com.crisos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crisos.app.ui.theme.*
import com.crisos.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val deviceStatus by viewModel.deviceStatus.collectAsState()
    val isNearbyActive by viewModel.isNearbyActive.collectAsState()
    val connectedPeerNames by viewModel.connectedPeerNames.collectAsState()
    val discoveredEndpoints by viewModel.discoveredEndpoints.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshStatus()
    }

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings & Status",
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
                actions = {
                    IconButton(onClick = { viewModel.refreshStatus() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Device capabilities
            SettingsSectionCard(title = "Device Capabilities") {
                SettingsStatusRow(
                    icon = Icons.Default.LocationOn,
                    label = "Location Permission",
                    isActive = deviceStatus.isLocationPermissionGranted,
                    activeText = "Granted",
                    inactiveText = "Not Granted — Required for GPS"
                )
                HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 6.dp))
                SettingsStatusRow(
                    icon = Icons.Default.GpsFixed,
                    label = "Location Services",
                    isActive = deviceStatus.isLocationEnabled,
                    activeText = "Enabled",
                    inactiveText = "Disabled — Enable GPS in system settings"
                )
                HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 6.dp))
                SettingsStatusRow(
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth",
                    isActive = deviceStatus.isBluetoothEnabled,
                    activeText = "Enabled",
                    inactiveText = "Disabled — Needed for Nearby"
                )
                HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 6.dp))
                SettingsStatusRow(
                    icon = Icons.Default.Wifi,
                    label = "Wi-Fi",
                    isActive = deviceStatus.isWifiEnabled,
                    activeText = "Enabled",
                    inactiveText = "Disabled — Needed for Nearby"
                )
            }

            Spacer(Modifier.height(16.dp))

            // Nearby Communication
            SettingsSectionCard(title = "Nearby Communication") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Nearby Mesh Networking",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            if (isNearbyActive) "Active — Continuous background scanning & auto-connect"
                            else "Inactive — Tap switch to start",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isNearbyActive) StatusGreen else TextMuted
                        )
                    }
                    Switch(
                        checked = isNearbyActive,
                        onCheckedChange = { viewModel.toggleNearby() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StatusGreen
                        )
                    )
                }

                if (isNearbyActive) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${deviceStatus.connectedDeviceCount}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Connected Peers",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${deviceStatus.discoveredDeviceCount}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = StatusBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Pending / In Range",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(Modifier.height(10.dp))

                    // Connected peers list
                    if (connectedPeerNames.isNotEmpty()) {
                        Text(
                            text = "Connected Devices:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        connectedPeerNames.forEach { (_, name) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Connected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusGreen
                                )
                            }
                        }
                    }

                    // Discovered but not yet connected
                    val pendingPeers = discoveredEndpoints.filterKeys { !connectedPeerNames.containsKey(it) }
                    if (pendingPeers.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Connecting to in-range peers:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        pendingPeers.forEach { (_, name) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = StatusBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Handshaking...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusBlue
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.refreshStatus() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Re-scan & Check Connections Now")
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "CriSOS checks continuously every few seconds for nearby devices and connects automatically using Bluetooth & Wi-Fi Direct.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Cloud Storage (MongoDB Atlas)
            SettingsSectionCard(title = "Cloud Storage (MongoDB Atlas)") {
                val pendingCount by viewModel.pendingCloudSyncCount.collectAsState()
                val syncedCount by viewModel.syncedCloudCount.collectAsState()
                val backendUrl by viewModel.backendUrl.collectAsState()
                var editingUrl by remember(backendUrl) { mutableStateOf(backendUrl) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Remote Atlas Sync",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            if (viewModel.isInternetAvailable) "Internet Connected — Synchronizing via WorkManager"
                            else "Offline — Messages retained locally until internet connects",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (viewModel.isInternetAvailable) StatusGreen else WarningAmber
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$syncedCount",
                            style = MaterialTheme.typography.headlineSmall,
                            color = StatusGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Synced to Atlas",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$pendingCount",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (pendingCount > 0) WarningAmber else StatusBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Pending Upload",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = SurfaceBorder)
                Spacer(Modifier.height(10.dp))

                Text(
                    "Backend API Base URL:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = editingUrl,
                    onValueChange = {
                        editingUrl = it
                        viewModel.updateBackendUrl(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    placeholder = { Text("http://10.0.2.2:3000", style = MaterialTheme.typography.bodySmall) }
                )

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.triggerCloudSyncNow() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBlue)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Trigger Cloud Sync Now")
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text = "CriSOS automatically queues all created and received SOS messages and uploads them to MongoDB Atlas via WorkManager whenever internet access is restored.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(Modifier.height(16.dp))

            // Privacy
            SettingsSectionCard(title = "Privacy") {
                Text(
                    "• Location data is only shared when you choose to include it in an SOS.\n" +
                            "• Messages are sent to nearby CriSOS devices only.\n" +
                            "• No data is sent to external servers in this version.\n" +
                            "• Your device ID is used to identify messages you send.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // About
            SettingsSectionCard(title = "About CriSOS") {
                DetailRow("Version", "0.1.0")
                DetailRow("Build", "Prototype")
                DetailRow("Architecture", "MVVM + Room + Nearby")
                Spacer(Modifier.height(8.dp))
                Text(
                    "CriSOS enables peer-to-peer emergency communication when mobile networks " +
                            "and internet are unavailable. Messages are exchanged directly between " +
                            "nearby devices using Bluetooth and Wi-Fi Direct.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsStatusRow(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeText: String,
    inactiveText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) StatusGreen else StatusGray,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = if (isActive) activeText else inactiveText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) StatusGreen else WarningAmber
            )
        }
    }
}
