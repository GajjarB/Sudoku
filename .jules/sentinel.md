## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2026-06-23 - Sentinel Fixed P2P Multiplayer DoS and Session Hijacking
**Vulnerability:** Google Nearby Connections API implementation in `NearbyGameManager` automatically accepted all connection requests without limits. This allowed an attacker to hijack an active session by sending a new connection request, or cause an OutOfMemoryError Denial of Service by sending exceptionally large payloads.
**Learning:** Even internal or local P2P network connections must implement input validation (size limits) and state-aware authorization checks (preventing secondary connection overrides). Unbounded payload acceptance on any interface is an exploitable vector.
**Prevention:** Implement payload size limits for all incoming P2P connections and verify existing connection states (`currentEndpointId`) before accepting new ones.
