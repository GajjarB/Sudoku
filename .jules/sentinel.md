## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.
## 2026-10-03 - Prevent Session Hijacking & DoS in P2P Multiplayer
**Vulnerability:** NearbyGameManager accepted unauthenticated secondary connections that could hijack an active session, and accepted unbound byte payloads which could lead to DoS/OOM.
**Learning:** Android Nearby Connections API requires explicit application-level tracking and rejection of connections, even when using P2P_POINT_TO_POINT strategy. Payloads must also be bounded at the application layer.
**Prevention:** Always enforce topological constraints (e.g., 1:1 active connection limit) using connectionsClient.rejectConnection(), and enforce payload size validations as soon as bytes are received.
