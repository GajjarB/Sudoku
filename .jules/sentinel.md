## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.
## 2026-09-28 - [Payload limits and Session Hijacking in Nearby API]
**Vulnerability:** The Android Nearby Connections API natively caps 'BYTES' payloads at 32KB, but lacking application-level payload size limits could allow for OOM/DoS attacks. Also, accepting secondary connections could allow for session hijacking.
**Learning:** Add defense-in-depth size limits for payloads (e.g. check for size > 32KB in onPayloadReceived) and actively check and reject secondary connections using connectionsClient.rejectConnection(endpointId).
**Prevention:** Explicitly enforce max payload limits and validate current connections when a new endpoint attempts to connect to avoid unwanted state changes.
