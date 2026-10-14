## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.
## 2023-10-24 - Fix Session Hijacking & OOM via Payload Size Limit in Nearby connections
**Vulnerability:** Session Hijacking and Potential OOM/DoS via unbounded payloads in Nearby Connections.
**Learning:** The NearbyGameManager accepted secondary connections automatically, bypassing 1:1 topologies, allowing unauthenticated attackers to hijack the session. Furthermore, Android Nearby Connections API natively caps payloads but lacks application-level size enforcement by default on BYTES.
**Prevention:** Explicitly enforce a 1:1 topology by verifying `currentEndpointId` and actively calling `rejectConnection()` on new endpoints when an active connection exists. For payloads, implement an application-level size limit check (e.g., `<32KB`) as a defense-in-depth measure.
