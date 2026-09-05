## 2026-06-23 - Sentinel Added Network Security Config
**Vulnerability:** Cleartext traffic was permitted by default.
**Learning:** By adding `android:usesCleartextTraffic="false"` to the AndroidManifest, the application forces HTTPS encryption on all outgoing HTTP requests, a critical defense-in-depth step against MitM attacks, especially if future 3rd-party libraries initiate HTTP connections.
**Prevention:** Keep `usesCleartextTraffic="false"` in the application manifest.
## 2023-10-27 - Predictable PRNG Usage
**Vulnerability:** The codebase was using `Math.random()` to generate random integer values for difficulty and puzzle parameters in `DifficultyAdapter` and `IntelligentPuzzleGenerator`. While the impact is low in a Sudoku game, using insecure PRNGs is a classic vulnerability that can lead to predictable random sequences and potential game manipulation.
**Learning:** `Math.random()` and `java.util.Random` are insecure and should not be used for random generation when sequence predictability could impact the application's integrity or mechanics.
**Prevention:** Always use `java.security.SecureRandom` to generate random values to ensure sequences are secure and unpredictable.
