## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.
## 2024-10-24 - [CRITICAL] Nearby Connections Session Hijacking and Payload DoS
**Vulnerability:** The Nearby Connections setup automatically accepted all incoming P2P connections and dynamically processed arbitrary byte payloads without limits.
**Learning:** This permitted session hijacking if a secondary malicious connection attempt succeeded while an active session was already ongoing. Similarly, a lack of bounds checking allowed excessively large payloads to be ingested leading to Potential OOM/DoS scenarios on Android.
**Prevention:** In Android Nearby Connections, always verify `currentEndpointId` state to reject subsequent incoming connections (`connectionsClient.rejectConnection(endpointId)`) when the system expects 1:1 topologies. To avoid DoS attacks natively, add application-level payload limits in `onPayloadReceived` (e.g., `< 32KB`) as a defense-in-depth measure.
