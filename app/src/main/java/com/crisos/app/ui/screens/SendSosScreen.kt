package com.crisos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crisos.app.domain.model.*
import com.crisos.app.ui.components.*
import com.crisos.app.ui.theme.*
import com.crisos.app.ui.viewmodel.SendResult
import com.crisos.app.ui.viewmodel.SendSosViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SendSosScreen(
    onNavigateBack: () -> Unit,
    viewModel: SendSosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // If send is complete, show result
    if (uiState.sendResult != null) {
        SendResultScreen(
            result = uiState.sendResult!!,
            onDone = {
                viewModel.resetForm()
                onNavigateBack()
            },
            onSendAnother = {
                viewModel.resetForm()
            }
        )
        return
    }

    // Confirmation dialog
    if (uiState.showConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmation() },
            title = {
                Text("Confirm SOS Alert", color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        "You are about to send an emergency SOS alert.",
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Category: ${uiState.category.icon} ${uiState.category.label}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    if (uiState.description.isNotBlank()) {
                        Text(
                            "Description: ${uiState.description}",
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                    if (uiState.includeLocation && uiState.currentLocation != null) {
                        Text("📍 Location will be included", color = StatusGreen)
                    } else {
                        Text("📍 No location attached", color = WarningAmber)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAndSend() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Send SOS", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmation() }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NavyMedium
        )
    }

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Send SOS Alert",
                        style = MaterialTheme.typography.titleLarge,
                        color = EmergencyRed
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Emergency category selection
            SectionHeader(title = "Emergency Type")
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyCategory.entries.forEach { category ->
                    val isSelected = uiState.category == category
                    val categoryColor = when (category) {
                        EmergencyCategory.FLOOD -> FloodBlue
                        EmergencyCategory.MEDICAL -> MedicalGreen
                        EmergencyCategory.FIRE -> FireOrange
                        EmergencyCategory.TRAPPED -> TrappedBrown
                        EmergencyCategory.EARTHQUAKE -> EarthquakeAmber
                        EmergencyCategory.VIOLENCE -> ViolenceRed
                        EmergencyCategory.OTHER -> StatusGray
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.updateCategory(category) }
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp, categoryColor, RoundedCornerShape(10.dp)
                                ) else Modifier
                            ),
                        color = if (isSelected) categoryColor.copy(alpha = 0.15f) else SurfaceCard,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(category.icon, fontSize = 16.sp)
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) categoryColor else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Description
            SectionHeader(title = "Describe Your Situation")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("e.g., Building collapsed, 3 people trapped on 2nd floor",
                        color = TextMuted)
                },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmergencyRed,
                    unfocusedBorderColor = SurfaceBorder,
                    cursorColor = EmergencyRed,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Assistance needed
            SectionHeader(title = "Assistance Needed")
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistanceType.entries.forEach { type ->
                    val isSelected = type in uiState.selectedAssistance
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleAssistance(type) },
                        label = {
                            Text(
                                type.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceCard,
                            selectedContainerColor = EmergencyRed.copy(alpha = 0.15f),
                            labelColor = TextSecondary,
                            selectedLabelColor = EmergencyRedLight
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SurfaceBorder,
                            selectedBorderColor = EmergencyRed,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Location
            SectionHeader(title = "Location")
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Include Location",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Switch(
                            checked = uiState.includeLocation,
                            onCheckedChange = { viewModel.setIncludeLocation(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusGreen
                            )
                        )
                    }

                    AnimatedVisibility(visible = uiState.includeLocation) {
                        Column {
                            Spacer(Modifier.height(8.dp))

                            if (uiState.isLocationLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = StatusBlue
                                    )
                                    Text(
                                        "Acquiring GPS fix…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusBlue
                                    )
                                }
                            } else if (uiState.currentLocation != null) {
                                val loc = uiState.currentLocation!!
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "${formatCoordinate(loc.latitude, true)}, ${formatCoordinate(loc.longitude, false)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "Accuracy: ±${loc.accuracyMeters.toInt()}m · ${formatTimestamp(loc.timestamp)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (loc.isStale()) WarningAmber else TextMuted
                                        )
                                        if (loc.isStale()) {
                                            Text(
                                                "⚠️ Location may be outdated",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = WarningAmber
                                            )
                                        }
                                    }
                                    IconButton(onClick = { viewModel.fetchLocation() }) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Refresh location",
                                            tint = StatusBlue
                                        )
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Location unavailable. SOS can still be sent.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WarningAmber
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                TextButton(onClick = { viewModel.fetchLocation() }) {
                                    Text("Try Again", color = StatusBlue)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Send button
            Button(
                onClick = { viewModel.showConfirmation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isSending
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.SendAndArchive, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Send SOS Alert",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SendResultScreen(
    result: SendResult,
    onDone: () -> Unit,
    onSendAnother: () -> Unit
) {
    val (icon, title, subtitle, color) = when (result) {
        SendResult.SENT -> listOf(
            "✅", "SOS Sent",
            "Your emergency alert has been sent to a nearby device.",
            StatusGreen
        )
        SendResult.QUEUED -> listOf(
            "📤", "SOS Queued",
            "No nearby devices found. Your alert is saved and will be sent when a device is in range.",
            WarningAmber
        )
        SendResult.FAILED -> listOf(
            "⚠️", "Delivery Uncertain",
            "The send attempt failed but your alert is saved locally. It will retry when a device connects.",
            EmergencyRed
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon as String, fontSize = 64.sp)
        Spacer(Modifier.height(20.dp))
        Text(
            text = title as String,
            style = MaterialTheme.typography.headlineMedium,
            color = color as Color,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = subtitle as String,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Done", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSendAnother,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
        ) {
            Text("Send Another SOS", fontSize = 16.sp)
        }
    }
}
