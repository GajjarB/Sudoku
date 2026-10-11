## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2024-05-27 - [Fix Session Hijacking and Payload DoS in Nearby Connections]
**Vulnerability:** The Android Nearby Connections API natively caps BYTES payloads at 32KB but doesn't inherently reject oversized maliciously fragmented/crafted packets without application logic. Also, it doesn't enforce 1:1 topologies natively, so an existing active session could be hijacked if a secondary connection was automatically accepted.
**Learning:** Application-level payload size limits (e.g., in `onPayloadReceived`) and active connection state tracking with explicit rejection (e.g., via `connectionsClient.rejectConnection(endpointId)` in `onConnectionInitiated`) are strictly necessary to provide defense-in-depth against OOM/DoS and prevent session hijacking.
**Prevention:** Always check active connection state before accepting new ones, explicitly rejecting them if busy. Validate packet sizes at the entry point of network interfaces before deserializing.
