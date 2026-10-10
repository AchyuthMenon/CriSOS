# CriSOS — Crisis + SOS

**Offline-first emergency communication for Android**

CriSOS enables people to send emergency SOS messages containing their location and immediate needs when mobile networks and internet connectivity are unavailable. Messages are exchanged directly between nearby Android devices using Bluetooth and Wi-Fi Direct via Google Nearby Connections.

> 📖 **Full Technical Architecture & Internal Mechanics Guide**: See [PROJECT_DOCUMENTATION.md](file:///c:/CriSOS/PROJECT_DOCUMENTATION.md) for in-depth details on how the offline mesh networking, multi-hop relaying, loop suppression, and radio hardware filtering work.

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

### Testing Multi-Hop Relay (Phone A → Phone B → Phone C)
Verify multi-hop forwarding without direct communication between Phone A and Phone C:
1. **Prepare 3 physical Android phones** (Phone A, Phone B, Phone C) with `CriSOS.apk` installed.
2. Put all 3 phones in **Airplane Mode**, then turn **ON Bluetooth and Wi-Fi** on each.
3. Launch **CriSOS** and grant all permissions on all 3 phones. In **Settings**, ensure "Nearby Connections" is **Active**.
4. **Physical Placement**:
   - Place **Phone B** in the middle (e.g. hallway/living room).
   - Place **Phone A** in Room 1 (within range of Phone B, but out of Bluetooth/Wi-Fi range of Phone C).
   - Place **Phone C** in Room 2 (within range of Phone B, but out of range of Phone A).
   - Confirm in Settings: Phone A shows 1 connected device (B); Phone B shows 2 connected devices (A & C); Phone C shows 1 connected device (B).
5. **Send SOS from Phone A**:
   - Select emergency category, enter a description, and tap **Send SOS Alert**.
   - Phone A saves the message with `hopCount = 0` and sends it to Phone B.
6. **Observe Phone B (Relay Node)**:
   - Phone B receives the alert, displays it in **Received Alerts** (with `hopCount = 0`), and automatically forwards a copy with `hopCount = 1` to Phone C (excluding immediate sender Phone A).
   - Phone B's local message status updates to `Relayed through intermediate device`.
7. **Observe Phone C (Destination Node)**:
   - Phone C receives the SOS alert and alerts the user.
   - Inspect **SOS Details** on Phone C: it displays the **exact same SOS ID**, original sender device ID, location coordinates, and shows `Hop Count: 1 / 5`.
8. **Loop Suppression Test**:
   - Phone C attempts to forward, but B rejects it as an atomic duplicate, preventing endless echo loops.

## Current Limitations

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
- [x] **Milestone 5** — Reliability: persist-before-send, duplicate detection, status tracking, auto-queue flush
- [x] **Milestone 6** — Multi-hop relay forwarding: peer exclusion, loop & duplicate suppression, MAX_HOPS (5) limit, expiry enforcement

## Future Features

1. Message acknowledgements (received → read → responding)
2. Community web dashboard (React + map)
3. Backend synchronization when internet returns
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
