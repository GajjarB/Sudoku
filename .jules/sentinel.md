## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.

## 2025-02-27 - [Session Hijacking / DoS in Nearby Connections]
**Vulnerability:** Android Nearby Connections API accepts multiple connections leading to session hijacking, and accepts large payloads leading to DoS.
**Learning:** Explicitly reject unwanted connections and validate payload size.
**Prevention:** Ensure 1:1 topologies track `currentEndpointId` and reject new connections, and cap payload bytes.
