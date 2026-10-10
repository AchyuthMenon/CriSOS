package com.crisos.app.ui.navigation

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object SendSos : Screen("send_sos")
    data object ReceivedAlerts : Screen("received_alerts")
    data object MessageHistory : Screen("message_history")
    data object Settings : Screen("settings")
    data object SosDetails : Screen("sos_details/{messageId}") {
        fun createRoute(messageId: String) = "sos_details/$messageId"
    }
}
