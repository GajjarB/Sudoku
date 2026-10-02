## 2024-05-24 - [Nearby Connections Session Hijacking]
**Vulnerability:** The NearbyGameManager blindly accepted all incoming connections via `connectionsClient.acceptConnection` without verifying if an active connection already existed, exposing the game to session hijacking from secondary malicious connections.
**Learning:** P2P connection callbacks in Android Nearby APIs do not enforce 1:1 topologies by default, even if the strategy is `P2P_POINT_TO_POINT`.
**Prevention:** Always track the current connection state and explicitly invoke `connectionsClient.rejectConnection(endpointId)` for any subsequent `onConnectionInitiated` events when already connected.

## 2024-05-24 - [Nearby Connections Payloads Limit]
**Vulnerability:** The application was not restricting the maximum payload size, which leaves the application vulnerable to DoS (Denial of Service) and Out-Of-Memory (OOM) attacks from adversaries sending extremely large payloads.
**Learning:** Android Nearby Connections API receives payloads over P2P dynamically, meaning excessive sizes aren't stopped by standard REST size limitations or API gateways.
**Prevention:** Always implement a sane explicit size limit (like 32KB) inside `onPayloadReceived` to discard abnormally large binary data before processing or un-marshaling it.
