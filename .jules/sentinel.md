## 2024-06-30 - Missing Cleartext Traffic Protection
**Vulnerability:** The application was missing `android:usesCleartextTraffic="false"` in the AndroidManifest.xml.
**Learning:** Despite architectural intent (as logged in memory) to enforce HTTPS/encrypted network communication, the actual manifest attribute was omitted.
**Prevention:** Always verify that security-related manifest flags are explicitly set rather than assuming they are present.
