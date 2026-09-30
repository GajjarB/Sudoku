## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2026-09-30 - [Session Hijacking & DoS in P2P Multiplayer]
**Vulnerability:** Found a lack of connection state verification during `onConnectionInitiated` and missing payload limits in `NearbyGameManager`. A malicious actor could hijack an active session by initiating a new connection that automatically gets accepted, overriding the current endpoint. Additionally, unrestricted payload size processing posed a DoS/OOM risk.
**Learning:** Automatically accepting connections without checking existing state (`currentEndpointId != null`) is dangerous for long-lived sessions. Similarly, relying only on OS-level payload limits is insufficient; app-level validation is required for defense in depth.
**Prevention:** Reject secondary incoming connections (`connectionsClient.rejectConnection`) when already connected to prevent state overrides. Implement max size checks on incoming byte payloads (e.g., 32KB) before processing or deserializing to prevent OOM/DoS attacks.
