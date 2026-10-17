## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.
## 2024-05-18 - [Prevent P2P Session Hijacking and DoS in Nearby Connections]
**Vulnerability:** Android Nearby Connections API does not enforce 1:1 topologies by default and permits large payloads, making P2P game endpoints vulnerable to session hijacking (secondary connections) and OOM/DoS attacks via oversized payloads.
**Learning:** Application-level validation is required for both connection lifecycle management (rejecting new connections if `currentEndpointId != null`) and payload sizes (even though API claims a 32KB limit for BYTES, defense-in-depth is necessary).
**Prevention:** Always track active connection state and explicitly reject unwanted connections via `rejectConnection()`. Add size boundary checks in `onPayloadReceived`.
