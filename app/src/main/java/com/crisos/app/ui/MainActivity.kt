package com.crisos.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.DeliveryStatus
import com.crisos.app.domain.model.MessageDirection
import com.crisos.app.ui.navigation.Screen
import com.crisos.app.ui.screens.*
import com.crisos.app.ui.theme.CrisosTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requiredPermissions: Array<String>
        get() = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Results handled reactively by refreshing device status */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request permissions
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }

        // Note: Incoming message reception, persistence, and auto-queue flushing
        // are centrally handled in CrisosApplication.

        setContent {
            CrisosTheme {
                CrisosNavHost()
            }
        }
    }
}

@Composable
fun CrisosNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSendSos = { navController.navigate(Screen.SendSos.route) },
                onNavigateToReceivedAlerts = { navController.navigate(Screen.ReceivedAlerts.route) },
                onNavigateToMessageHistory = { navController.navigate(Screen.MessageHistory.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.SendSos.route) {
            SendSosScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ReceivedAlerts.route) {
            ReceivedAlertsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { messageId ->
                    navController.navigate(Screen.SosDetails.createRoute(messageId))
                }
            )
        }

        composable(Screen.MessageHistory.route) {
            MessageHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { messageId ->
                    navController.navigate(Screen.SosDetails.createRoute(messageId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SosDetails.route) { backStackEntry ->
            val messageId = backStackEntry.arguments?.getString("messageId") ?: ""
            SosDetailsScreen(
                messageId = messageId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
