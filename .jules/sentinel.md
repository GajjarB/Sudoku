## 2024-06-28 - Explicitly Disable Cleartext Traffic
**Vulnerability:** Cleartext traffic was not explicitly disabled in the application manifest. While API 28+ disables it by default, older devices or misconfigurations could still permit unencrypted network requests, violating the defense-in-depth principle.
**Learning:** Adding explicit security configurations in AndroidManifest.xml (like `android:usesCleartextTraffic="false"`) is critical to enforce secure communication (HTTPS) universally across all SDK versions supported by the app (minSdk=24).
**Prevention:** Always include explicit security-hardening flags in AndroidManifest.xml rather than relying on Android OS default behaviors, which can vary by API level.
