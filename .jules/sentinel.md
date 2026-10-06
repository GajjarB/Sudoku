## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.
## 2026-10-06 - Session Hijacking and OOM Prevention in Nearby Connections
**Vulnerability:** Android Nearby APIs do not enforce 1:1 topologies natively, allowing secondary connections (session hijacking), and lack explicit application-level payload limits which could lead to OOM/DoS.
**Learning:** Secondary connections must be explicitly rejected using `connectionsClient.rejectConnection(endpointId)`, and payload sizes should be capped (e.g. 32KB) in `onPayloadReceived` to ensure connection state integrity and memory limits.
**Prevention:** Always check `currentEndpointId != null` in `onConnectionInitiated` to reject unwanted connections, and validate payload sizes before processing.
