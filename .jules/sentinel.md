## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2026-06-25 - Sentinel Secures Multiplayer Connections and Payloads
**Vulnerability:** The application was vulnerable to session hijacking by automatically accepting secondary connections and DoS/OOM attacks by not validating payload sizes.
**Learning:** `NearbyGameManager` accepted all `onConnectionInitiated` requests blindly, replacing the `currentEndpointId` and hijacking the active session. The `onPayloadReceived` processed incoming byte payloads into full `GamePayload` objects without size boundaries, risking OOM from malicious large payloads.
**Prevention:** Reject secondary connections when an active endpoint already exists using `connectionsClient.rejectConnection(endpointId)`. Validate payload length against a hardcoded limit (e.g., 32KB) before processing or serializing binary data from network peers.
