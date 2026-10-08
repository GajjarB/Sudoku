## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.
## 2026-10-08 - Prevent Session Hijacking in Nearby Connections
**Vulnerability:** Android Nearby Connections API P2P_POINT_TO_POINT strategy does not enforce 1:1 topologies natively, allowing unauthorized connections to hijack an active session.
**Learning:** Application-level state tracking (e.g. currentEndpointId) is required to explicitly reject secondary connections via connectionsClient.rejectConnection(). Automatically accepting all connections exposes the app to session hijacking.
**Prevention:** Always verify existing connection state before accepting a new connection in onConnectionInitiated, and enforce payload size limits (e.g. 32KB) in onPayloadReceived as defense-in-depth against DoS attacks.
