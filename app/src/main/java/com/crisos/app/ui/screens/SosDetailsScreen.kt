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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crisos.app.domain.model.*
import com.crisos.app.ui.components.*
import com.crisos.app.ui.theme.*
import com.crisos.app.ui.viewmodel.SosDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SosDetailsScreen(
    messageId: String,
    onNavigateBack: () -> Unit,
    viewModel: SosDetailsViewModel = viewModel()
) {
    LaunchedEffect(messageId) {
        viewModel.loadMessage(messageId)
    }

    val message by viewModel.message.collectAsState()

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SOS Details",
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
        if (message == null) {
            EmptyState(
                icon = Icons.Default.SearchOff,
                title = "Message Not Found",
                subtitle = "This message may have been deleted.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            return@Scaffold
        }

        val msg = message!!

        val categoryColor = when (msg.emergencyCategory) {
            EmergencyCategory.FLOOD -> FloodBlue
            EmergencyCategory.MEDICAL -> MedicalGreen
            EmergencyCategory.FIRE -> FireOrange
            EmergencyCategory.TRAPPED -> TrappedBrown
            EmergencyCategory.EARTHQUAKE -> EarthquakeAmber
            EmergencyCategory.VIOLENCE -> ViolenceRed
            EmergencyCategory.OTHER -> StatusGray
        }

        val statusColor = when (msg.deliveryStatus) {
            DeliveryStatus.SENT, DeliveryStatus.RELAYED -> StatusGreen
            DeliveryStatus.RECEIVED, DeliveryStatus.ACKNOWLEDGED -> StatusBlue
            DeliveryStatus.FAILED, DeliveryStatus.EXPIRED -> EmergencyRed
            DeliveryStatus.QUEUED, DeliveryStatus.SENDING -> WarningAmber
            DeliveryStatus.DRAFT -> StatusGray
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Category header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = categoryColor.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(msg.emergencyCategory.icon, fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = msg.emergencyCategory.label,
                        style = MaterialTheme.typography.headlineSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    // Status chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = msg.deliveryStatus.label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (msg.direction == MessageDirection.OUTGOING) "Sent" else "Received",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Description
            if (msg.description.isNotBlank()) {
                DetailSection(title = "Description") {
                    Text(
                        text = msg.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Assistance needed
            if (msg.assistanceTypes.isNotEmpty()) {
                DetailSection(title = "Assistance Required") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        msg.assistanceTypes.forEach { type ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmergencyRed.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = type.label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmergencyRedLight
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Location
            DetailSection(title = "Location") {
                if (msg.latitude != null && msg.longitude != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = StatusGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "${formatCoordinate(msg.latitude, true)}, ${formatCoordinate(msg.longitude, false)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            if (msg.locationAccuracyMeters != null) {
                                Text(
                                    text = "Accuracy: ±${msg.locationAccuracyMeters.toInt()} meters",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            if (msg.locationTimestamp != null) {
                                val isStale = (System.currentTimeMillis() - msg.locationTimestamp) > 5 * 60 * 1000
                                Text(
                                    text = "Captured: ${formatFullTimestamp(msg.locationTimestamp)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isStale) WarningAmber else TextMuted
                                )
                                if (isStale) {
                                    Text(
                                        text = "⚠️ This location may be outdated",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WarningAmber
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Location not available for this alert",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarningAmber
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Message info
            DetailSection(title = "Message Information") {
                DetailRow("Message ID", msg.id.take(12) + "…")
                DetailRow("Sender", msg.senderDeviceId.take(16).ifBlank { "Unknown" })
                DetailRow("Created", formatFullTimestamp(msg.createdAt))
                DetailRow("Expires", formatFullTimestamp(msg.expiresAt))
                DetailRow("Direction", msg.direction.name)
                DetailRow("Hop Count", "${msg.hopCount} / ${msg.maxHopCount}")
                if (msg.incidentId != null) {
                    DetailRow("Incident ID", msg.incidentId.take(12) + "…")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

fun formatFullTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
