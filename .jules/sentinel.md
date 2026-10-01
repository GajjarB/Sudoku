## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2024-05-25 - Fix Session Hijacking and DoS Risk in NearbyGameManager
**Vulnerability:** Secondary connections could overwrite the existing opponent session (session hijacking), and excessively large byte payloads could cause Out-of-Memory (OOM) or Denial of Service (DoS) issues since payload size wasn't restricted.
**Learning:** The Android Nearby Connections API natively caps `BYTES` payloads at 32KB, but adding application-level payload size limits provides defense-in-depth against OOM/DoS attacks. Rejecting unwanted connections via `connectionsClient.rejectConnection(endpointId)` is the officially supported flow to enforce connection states and prevent session hijacking.
**Prevention:** Always restrict payload sizes for incoming P2P connections and reject unexpected endpoints instead of silently overwriting existing sessions.
