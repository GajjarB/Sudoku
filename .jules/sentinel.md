## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2026-06-23 - Sentinel Added Session Hijacking and DoS Protection in P2P
**Vulnerability:** Android Nearby Connections API does not enforce 1:1 topologies by default even with `P2P_POINT_TO_POINT`, allowing potential session hijacking via secondary connections. Furthermore, there was no payload size limit, risking OOM/DoS attacks from oversized payloads.
**Learning:** Application-level state tracking (checking `currentEndpointId != null` and explicitly calling `connectionsClient.rejectConnection(endpointId)`) is required to guarantee a 1:1 topology and prevent rogue actors from overtaking a multiplayer session. Additionally, defensively limiting payload size (e.g., 32KB) prevents malicious or malformed large byte arrays from causing OOM crashes.
**Prevention:** Always track active connections and reject subsequent requests if an endpoint is already engaged. Validate all incoming P2P payload sizes before attempting to deserialize them.
