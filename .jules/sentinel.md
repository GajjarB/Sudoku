## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2025-02-14 - [P2P Session Hijacking and DoS Prevention]
**Vulnerability:** Android Nearby Connections API allows secondary connection attempts even if a point-to-point connection is established, which enables session hijacking if auto-accepted. Oversized `BYTES` payloads also risk memory exhaustion (DoS).
**Learning:** Nearby API topologies are advisory (e.g., `P2P_POINT_TO_POINT` does not enforce a strict 1:1 network state internally). Explicit application-layer application state tracking (`currentEndpointId`) and connection rejection (`connectionsClient.rejectConnection`) are required to prevent session takeovers.
**Prevention:** Always maintain connection state and explicitly reject new endpoints while active. Add static size limit bounds on incoming `BYTES` payloads directly in `onPayloadReceived` before deserialization.
