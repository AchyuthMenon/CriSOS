# CriSOS — Crisis + SOS

**Offline-first emergency communication for Android**

CriSOS enables people to send emergency SOS messages containing their location and immediate needs when mobile networks and internet connectivity are unavailable. Messages are exchanged directly between nearby Android devices using Bluetooth and Wi-Fi Direct via Google Nearby Connections.

## Architecture

```
MVVM + Repository Pattern
├── ui/              → Jetpack Compose screens, components, theme, navigation
├── viewmodel/       → ViewModels (one per screen)
├── domain/model/    → Core data classes (SosMessage, enums)
├── data/local/      → Room database, entities, DAOs
├── data/repository/ → Repository implementations
├── location/        → Fused Location Provider wrapper
├── communication/   → Google Nearby Connections manager
└── CrisosApplication.kt → Manual DI (Hilt-ready)
```

## Technology Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Database | Room + SQLite |
| Location | Fused Location Provider (works offline via GPS) |
| Device-to-device | Google Nearby Connections API (P2P_CLUSTER) |
| Serialization | Gson |
| Async | Kotlin Coroutines + Flow |

## Screens

1. **Home** — SOS button, device status, quick actions
2. **Send SOS** — Emergency category, description, assistance, optional location, confirmation
3. **Received Alerts** — List of incoming SOS messages
4. **Message History** — All/Sent/Received/Pending tabs
5. **SOS Details** — Full message info with location, accuracy, timestamps
6. **Settings** — Device capabilities, Nearby toggle, privacy, diagnostics

## Required Permissions

| Permission | Purpose |
|---|---|
| `ACCESS_FINE_LOCATION` | GPS coordinates for SOS messages |
| `ACCESS_COARSE_LOCATION` | Fallback location |
| `BLUETOOTH_ADVERTISE` | Nearby Connections advertising (API 31+) |
| `BLUETOOTH_CONNECT` | Nearby Connections (API 31+) |
| `BLUETOOTH_SCAN` | Nearby Connections discovery (API 31+) |
| `NEARBY_WIFI_DEVICES` | Wi-Fi Direct for Nearby (API 33+) |
| `ACCESS_WIFI_STATE` | Nearby Connections |
| `CHANGE_WIFI_STATE` | Nearby Connections |

## Setup

### Prerequisites
- Android Studio Ladybug (2024.2.x) or newer
- Android SDK 35
- JDK 17
- Two physical Android phones (API 28+) for testing Nearby Connections

### Build & Run
1. Open the `CriSOS` folder in Android Studio.
2. Sync Gradle.
3. Connect a physical Android device (API 28+).
4. Run the `app` configuration.

### Running Unit Tests
To run local JVM unit tests without physical devices:
```bash
.\gradlew.bat testDebugUnitTest
```
Tests verify JSON message serialization, Room entity mapping, coordinate formatting, and location staleness checks.

### Testing Nearby Connections on Physical Devices
1. Install the APK (`app/build/outputs/apk/debug/app-debug.apk`) on **two physical phones**.
2. Grant all requested permissions on both devices (Location, Nearby devices/Bluetooth).
3. On both phones, go to Settings → toggle "Nearby Connections" ON.
4. Put both phones in **airplane mode**, then re-enable **Bluetooth** and **Wi-Fi** (not mobile data or internet Wi-Fi).
5. Send an SOS from Phone A — it is persisted locally and transferred directly to Phone B.
6. Check Phone B's **Received Alerts** tab to view the incoming alert with category, timestamp, description, and GPS coordinates.
7. **Offline Queue Test (Store-and-Forward)**: Turn Nearby Connections OFF on Phone B, send an SOS from Phone A (status becomes `QUEUED`). Re-enable Nearby on Phone B; when the devices reconnect, Phone A automatically flushes and delivers the queued message to Phone B.

> **Note:** Nearby Connections requires physical Bluetooth/Wi-Fi Direct hardware and does NOT work on standard Android emulators. Two physical devices are required.

## Current Limitations

- **No multi-hop relay** — messages go directly between two connected devices only.
- **No message acknowledgements** — the sender knows "sent" vs "queued" but not "read."
- **No offline maps** — coordinates are shown as text, not on a map.
- **No backend sync** — messages stay on-device only.
- **No message signatures** — messages are not cryptographically verified.
- **Auto-accept connections** — all CriSOS-to-CriSOS connections are auto-accepted (no auth).

## Completed Milestones

- [x] **Milestone 1** — Project foundation: theme, navigation, all 6 screens, bundled assets
- [x] **Milestone 2** — Local SOS: Room persistence, message model, send/receive history
- [x] **Milestone 3** — Geolocation: GPS integration, accuracy/staleness indicators
- [x] **Milestone 4** — Direct offline communication: Nearby Connections integration
- [x] **Milestone 5** — Reliability: persist-before-send, duplicate detection, status tracking
- [x] **Milestone 6** — Relay preparation: hop count/max fields, expiry timestamps

## Future Features

1. Multi-hop store-and-forward relay
2. Message acknowledgements (received → read → responding)
3. Community web dashboard (React + map)
4. Backend synchronization when internet returns
5. Incident grouping
6. Emergency bulletins
7. Volunteer coordination
8. Offline maps (vector tiles)
9. Message signatures and trusted identities
10. Internationalization and accessibility
11. Incident updates
12. Local resource directory (shelters, contacts)

## License

Hackathon project — not yet licensed for production use.
