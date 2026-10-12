## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2026-10-12 - [Session Hijacking Prevention in Nearby Connections]
**Vulnerability:** Android Nearby Connections API P2P_POINT_TO_POINT strategy does not enforce 1:1 topologies by default. This allowed a secondary attacker connection to hijack an active session by initiating a connection that was automatically accepted.
**Learning:** Application-level connection state tracking and rejection is required when using the Android Nearby Connections API to enforce 1:1 connection states and prevent session hijacking.
**Prevention:** Always verify if an existing connection is active (e.g., `currentEndpointId != null`) and explicitly reject unwanted secondary connections via `connectionsClient.rejectConnection(endpointId)`.
