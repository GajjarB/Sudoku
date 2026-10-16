## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2026-06-25 - Android Nearby Connections Session Hijacking and Payload DoS
**Vulnerability:** The `NearbyGameManager` automatically accepted all incoming connections (`onConnectionInitiated`) and processed raw byte payloads of any size (`onPayloadReceived`). This exposed the game to session hijacking (an attacker connecting and disrupting an active game) and potential Denial of Service or Out-of-Memory crashes from excessively large payloads.
**Learning:** Android Nearby APIs do not enforce 1:1 topologies by default, even with `P2P_POINT_TO_POINT`. Explicit application-level state tracking (e.g., rejecting if `currentEndpointId` is not null) and payload bounds checking are necessary defense-in-depth measures.
**Prevention:** Always track active connections and explicitly call `connectionsClient.rejectConnection()` for secondary connections. Enforce reasonable size limits (e.g., 32KB) on incoming bytes before attempting deserialization.
