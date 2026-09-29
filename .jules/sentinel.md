## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2026-06-23 - Sentinel Mitigated Nearby Connections Session Hijacking and DoS
**Vulnerability:** Secondary connections could overwrite the existing active endpoint hijacking the session, and large payloads could be sent causing an OOM or DoS attack.
**Learning:** `Nearby Connections API` inherently lacks strict connection state enforcement in a single-session scenario. We must explicitly call `connectionsClient.rejectConnection(endpointId)` to ignore subsequent connections. Additionally, since payload maximum sizes are only suggested by documentation, defense in depth via checking byte array length locally before deserializing (`if (bytes.size > MAX_PAYLOAD_SIZE)`) is required.
**Prevention:** In multiplayer integration, ALWAYS limit received network payload sizes and explicitly enforce connection state exclusivity by rejecting unexpected connections.
