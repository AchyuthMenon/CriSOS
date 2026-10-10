# CriSOS (Crisis + SOS) — Technical Architecture & Project Documentation

## 1. Executive Summary

**CriSOS** is an offline-first, peer-to-peer disaster communication application for Android. During catastrophic natural disasters, conflicts, or infrastructure blackouts, cellular towers and commercial internet services frequently collapse. CriSOS bridges this critical communication gap by transforming everyday Android smartphones into autonomous, decentralized mesh relay nodes.

Survivors can broadcast high-priority SOS emergency distress messages containing exact GPS coordinates, medical requirements, casualty counts, and situational descriptions. Messages hop device-to-device across an ad-hoc mesh network powered by **Bluetooth Low Energy (BLE)** and **Wi-Fi Direct** via Google Nearby Connections—requiring **zero cellular data, zero SIM card, and zero internet infrastructure**.

---

## 2. Technology Stack & Core Libraries

| Domain / Layer | Technology | Version / Specification | Rationale & Responsibility |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | 2.0.21 | Type-safe, expressive native Android development. |
| **UI Framework** | Jetpack Compose | Material 3 (BOM 2024.10.01) | Declarative UI, reactive state binding, modern dark-mode emergency theme. |
| **Architecture** | MVVM + Repository | Clean Architecture principles | Strict separation of UI, business logic, state management, and transport. |
| **Mesh Networking** | Google Nearby Connections API | `com.google.android.gms:play-services-nearby:19.3.0` | `P2P_CLUSTER` topology enabling multi-peer, many-to-many ad-hoc radio links. |
| **Persistence** | Android Room + SQLite | `androidx.room:room-*:2.6.1` with KSP | Offline-first persistence, transactional ACID storage, and Room Flow queries. |
| **Asynchronous Engine** | Kotlin Coroutines & Flow | `kotlinx.coroutines` 1.9.0 | Reactive StateFlow pipelines, non-blocking disk I/O, background monitoring loops. |
| **Location Services** | Google Fused Location Provider | `play-services-location:21.3.0` | Hardware GPS satellite coordinate acquisition with staleness tracking. |
| **Serialization** | Google Gson | 2.11.0 | Compact JSON payloads for over-the-air packet transmission. |
| **Testing** | JUnit 4 + Mockito | `junit:4.13.2`, `mockito-core:5.14.2` | Comprehensive JVM unit testing for relay logic, duplicate filters, and serialization. |

---

## 3. High-Level System Architecture

The application follows an **Offline-First Clean Architecture** with unidirectional data flow (UDF):

```
┌──────────────────────────────────────────────────────────────────────────┐
│                             Jetpack Compose UI                          │
│   HomeScreen  │  SendSosScreen  │  ReceivedAlerts  │  MessageHistory     │
│   SosDetails  │  SettingsScreen │  Theme & Reusable Components           │
└────────────────────────────────────▲─────────────────────────────────────┘
                                     │ (StateFlow / UI Events)
┌────────────────────────────────────▼─────────────────────────────────────┐
│                            ViewModel Layer                               │
│   HomeViewModel    │  SendSosViewModel   │  ReceivedAlertsViewModel      │
│   HistoryViewModel │  DetailsViewModel   │  SettingsViewModel            │
└────────────────────────────────────▲─────────────────────────────────────┘
                                     │ (Domain Models & Reactive Flows)
┌────────────────────────────────────▼─────────────────────────────────────┐
│                             Domain Layer                                 │
│   SosMessage       │  EmergencyType      │  UrgencyLevel                 │
│   DeliveryStatus   │  MessageDirection   │  DeviceStatus                 │
└────────────────────────────────────▲─────────────────────────────────────┘
                                     │
           ┌─────────────────────────┴─────────────────────────┐
           ▼                                                   ▼
┌────────────────────────────────────┐   ┌─────────────────────────────────┐
│        Data Persistence            │   │       Mesh Communication        │
│  SosRepository                     │   │  NearbyConnectionManager        │
│  SosMessageDao (Room)              │   │  SosRelayController             │
│  CrisosDatabase (SQLite)           │   │  LocationService (GPS)          │
└────────────────────────────────────┘   └─────────────────────────────────┘
```

---

## 4. How Things Work Under the Hood

### 4.1. Radio Networking: Bluetooth LE & Wi-Fi Direct Mesh

CriSOS operates using Google Nearby Connections configured with the `Strategy.P2P_CLUSTER` strategy:

1. **Simultaneous Advertising & Discovery**:
   * Every device acts as both an **advertiser** (broadcasting its presence) and a **discoverer** (listening for nearby devices).
   * Handshakes use Bluetooth Low Energy (BLE) to establish mutual awareness, then automatically upgrade to high-bandwidth **Wi-Fi Direct** or local peer-to-peer Wi-Fi channels when payloads are transferred.
2. **Race-Condition & Tie-Breaking Resolution**:
   * When two devices (e.g. Phone A and Phone B) discover each other at the exact same millisecond, both issue `requestConnection()` calls.
   * Google Nearby Connections resolves the tie: one device receives `STATUS_OK` (0), while the peer receives `STATUS_ALREADY_CONNECTED_TO_ENDPOINT` (8003).
   * **CriSOS handles both codes as successful connections**, ensuring neither device drops into an unconnected state.
3. **Hardware Loopback Suppression**:
   * Certain Android chipsets (e.g. MediaTek, Vivo Funtouch OS) do not filter their own BLE transmissions at the hardware controller level.
   * CriSOS encodes advertisements as `"${android.os.Build.MODEL}::${localDeviceId}"`.
   * When a discovery beacon arrives, the system compares `peerDeviceId` with `localDeviceId`. If they match, the beacon is immediately discarded, preventing phones from connecting in a loopback to themselves.
4. **Continuous Background Monitoring & Auto-Healing**:
   * An autonomous coroutine runs every **6 seconds** to check for discovered peers that have not yet connected, automatically re-attempting connection.
   * If zero peers are connected for **24 seconds**, the manager automatically cycles discovery to prevent Android radio chips from sleeping or reducing BLE scan duty cycles.

---

### 4.2. Multi-Hop SOS Relaying Engine (`SosRelayController`)

To extend range beyond direct line-of-sight (e.g., reaching rescuers 500 meters away across several intermediate phones), CriSOS implements an **Epidemic Flooding with Controlled Relay** algorithm:

```
[Phone A: Victim]  ──────(Hop 0)──────►  [Phone B: Relay Node]
                                                  │
                                               (Hop 1)
                                                  ▼
                                         [Phone C: Rescuer]
```

#### Relay Logic Pipeline:
1. **Message Origination (Phone A)**:
   * The user composes an SOS. The message is assigned a unique UUID, timestamp, GPS coordinates, `hopCount = 0`, and `MAX_HOPS = 5`.
   * It is immediately written to Room SQLite and queued for delivery.
2. **Relay Verification on Intermediate Nodes (Phone B)**:
   * When Phone B receives the packet, `SosRelayController.handleIncomingMessage()` executes the following validation pipeline:
     * **Originator Check**: If `message.senderDeviceId == localDeviceId`, discard (prevents echoing own message).
     * **Atomic Duplicate Check**: An in-memory concurrent thread-safe cache (`ConcurrentHashMap.newKeySet()`) and a Room database existence check ensure that an SOS is processed exactly once.
     * **Expiration Check**: Messages older than 24 hours (`System.currentTimeMillis() - timestamp > 86,400,000 ms`) are dropped.
     * **Hop Limit Check**: If `message.hopCount >= MAX_HOPS` (5), the message is displayed locally to the user but not forwarded further.
3. **Forwarding to Next Hops (Phone C)**:
   * Phone B saves the message to Room as `INCOMING` with delivery status `RELAYED`.
   * Phone B increments `hopCount = message.hopCount + 1`.
   * Phone B forwards the packet to all connected peers **except the immediate sender** (`endpointId != immediateSenderEndpointId`).
4. **Infinite Loop & Broadcast Storm Prevention**:
   * If Phone C sends the packet back to Phone B, Phone B detects the message ID in its duplicate cache and silently drops it.

---

### 4.3. Store-and-Forward (Auto-Queue Flushing)

In disaster environments, survivors are often completely isolated initially:
1. When an SOS is sent while 0 peers are in range, the message is persisted in Room with `DeliveryStatus.QUEUED`.
2. CriSOS registers an `onEndpointConnected` lifecycle hook in `CrisosApplication`.
3. The moment any device comes within physical radio range and completes the handshake, the queue engine queries `sosRepository.getPendingOutgoingMessagesOnce()` and flushes all queued emergency messages to the newly connected peer.

---

### 4.4. Offline Geolocation & Staleness Tracking

* **Pure Hardware Satellite GPS**:
  The `LocationService` binds directly to Android's `FusedLocationProviderClient` with `PRIORITY_HIGH_ACCURACY`.
  * Operates with cellular and Wi-Fi disabled using satellite constellations (GPS, GLONASS, Galileo, BeiDou).
* **Cached Fallback & Staleness**:
  * If a live fix cannot be acquired immediately (e.g. indoors or under rubble), the service retrieves the last known location.
  * The UI calculates age: if older than 15 minutes, it flags the coordinate as **Stale (cached)** to inform rescuers of its age.

---

## 5. Data Models & Schemas

### 5.1. `SosMessage` Entity (Room + Wire Format)

```kotlin
@Entity(tableName = "sos_messages")
data class SosMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val senderDeviceId: String,          // Unique device identifier
    val timestamp: Long,                 // Epoch millisecond timestamp
    val emergencyType: EmergencyType,    // MEDICAL, TRAPPED, EVACUATION, etc.
    val urgencyLevel: UrgencyLevel,      // CRITICAL, HIGH, MEDIUM, LOW
    val description: String,             // User text description
    val assistanceNeeded: String,        // First aid, evacuation, food, water
    val peopleCount: Int,                // Number of individuals needing help
    val latitude: Double? = null,        // GPS latitude (-90.0 to 90.0)
    val longitude: Double? = null,       // GPS longitude (-180.0 to 180.0)
    val accuracy: Float? = null,         // Accuracy in meters
    val isLocationStale: Boolean = false,// Flag if cached location was used
    val hopCount: Int = 0,               // Current relay hop distance
    val maxHops: Int = 5,                // Relay radius cutoff
    val deliveryStatus: DeliveryStatus,  // DRAFT, QUEUED, SENT, RELAYED, FAILED
    val direction: MessageDirection      // OUTGOING (sent by me) vs INCOMING
)
```

### 5.2. Enums
* **`EmergencyType`**: `MEDICAL`, `TRAPPED`, `FIRE`, `FLOOD`, `EVACUATION`, `SUPPLIES`, `OTHER`
* **`UrgencyLevel`**: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`
* **`DeliveryStatus`**: `DRAFT`, `QUEUED`, `SENT`, `RELAYED`, `FAILED`
* **`MessageDirection`**: `OUTGOING`, `INCOMING`

---

## 6. Permissions & Radio Requirements

### 6.1. Android Manifest Permissions
* `ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`: Required for hardware GPS coordinates and for Android OS Bluetooth/Wi-Fi scanning.
* `BLUETOOTH_ADVERTISE`, `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` (Android 12+ / API 31+): Required for BLE peer discovery.
* `NEARBY_WIFI_DEVICES` (Android 13+ / API 33+): Required for Wi-Fi Direct peer-to-peer data links.
* `ACCESS_WIFI_STATE` & `CHANGE_WIFI_STATE`: Required for P2P Wi-Fi socket negotiation.

### 6.2. Physical Device Operating Checklist
* **Wi-Fi Toggle**: **ON** (does *not* need to be connected to any Wi-Fi router).
* **Bluetooth Toggle**: **ON** (no manual device pairing needed in Android settings).
* **Location (GPS)**: **ON** in Android quick settings.
* **Mobile Data**: **OFF** or disconnected (100% offline).
* **Personal Hotspot**: **OFF** (manual tethering blocks Nearby Connections Wi-Fi Direct sockets).

---

## 7. Testing & Verification Guide

### 7.1. Automated Unit Tests
CriSOS includes unit tests validating message integrity, Room mapping, coordinate formatting, and loop suppression:
```powershell
.\gradlew.bat testDebugUnitTest
```
* `SosRelayControllerTest`: Validates hop incrementation, 24h expiration drops, loop suppression, and immediate sender exclusion.
* `SosMessageTest`: Validates JSON serialization/deserialization consistency and Room entity transitions.

### 7.2. Multi-Hop Field Test Procedure (3 Devices)
1. Install `CriSOS.apk` on **Phone A**, **Phone B**, and **Phone C**.
2. Turn ON **Wi-Fi** and **Bluetooth** on all 3 phones. Turn **Mobile Data OFF** or enable **Airplane Mode**.
3. Position **Phone B** in the middle between Phone A and Phone C, such that Phone A cannot reach Phone C directly.
4. On **Phone A**, broadcast an SOS alert.
5. **Phone B** receives the alert, displays it, increments `hopCount`, and automatically relays it to **Phone C**.
6. **Phone C** receives the alert, showing `Hop Count: 1 / 5` with Phone A's original device ID and GPS location.
