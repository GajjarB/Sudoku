## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.
## 2024-05-15 - Enforce 1:1 topology and payload limits in Nearby API
**Vulnerability:** The application was vulnerable to session hijacking over the Google Nearby Connections API because it failed to explicitly reject secondary connections when one was already active, and it accepted payloads of any size.
**Learning:** Android Nearby APIs do not enforce 1:1 topologies by default, even when using the P2P_POINT_TO_POINT strategy, requiring explicit application-level connection state tracking and rejection. Additionally, while the API limits BYTES payloads, it's a good defense-in-depth practice to enforce payload limits in application logic to prevent DoS via large buffers.
**Prevention:** Track the active connection's endpoint ID and enforce explicit rejections for incoming connections when already connected. Enforce safe size limits for inbound byte payloads.
