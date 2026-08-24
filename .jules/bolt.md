## 2024-05-23 - Prevented O(N) Memory Scaling in Stats ViewModel
**Learning:** In the `StatsViewModel`, the "MONTH" and "ALL" filters were loading the entire `GameHistory` list into memory (`database.gameDao().getGamesSince(since)`) just to populate a static 7-day chart view via nested iteration. This created a significant memory bottleneck for long-term users.
**Action:** Always ensure that data queried strictly matches UI needs. Introduced a specific `chartSince` 7-day lookback for the loop query, effectively decoupling chart population from the potentially unbounded `since` date used for aggregated statistic queries.

## 2024-06-20 - Optimized ProfileViewModel GameHistory loading
**Learning:** In the `ProfileViewModel`, checking for lifetime achievements (Perfect Game, Speed Solver) by querying the most recent 50 `GameHistory` objects into memory, then iterating over them, creates unnecessary O(N) allocation and serialization overhead. This also introduces a logic flaw where achievements could be 'forgotten' if the matching game falls outside the last 50.
**Action:** Replaced the in-memory object loop with highly optimized `SELECT EXISTS(...)` Room queries (`hasPerfectGame`, `hasFastGame`). This leverages SQLite efficiently, returning a single boolean and avoiding excessive JVM allocations.

## 2026-06-25 - Avoid Repeated Initialization Checks in LevelRepository
**Learning:** The `LevelRepository.ensureLevelsInitializedSync()` method executes a `SELECT COUNT(*)` on the database to check if levels need initialization. Because methods like `isLevelUnlockedSync` and `getHighestUnlockedLevelSync` both call it, simple sequential logic (like finding a playable level) triggers redundant synchronous database queries for an operation that only needs to happen once per app lifecycle.
**Action:** Introduced a `static volatile boolean` memory cache to bypass the database check once initialization is confirmed, reducing I/O overhead on common level loading paths.

## 2026-07-01 - Optimized GameViewModel lockedCellsFor string parsing
**Learning:** In the `GameViewModel`, `lockedCellsFor` was unnecessarily parsing an 81-character puzzle string into an `int[9][9]` 2D array by delegating to `GridUtils.parseGrid`, only to iterate over the entire array again to recreate an 81-character '0'/'1' string. This resulted in O(N) intermediate heap allocations via `new int[9][9]` for a simple string transform.
**Action:** Replaced the intermediate array logic with a direct 1-pass string iteration and `StringBuilder` conversion, eliminating the heavy allocations completely.

## 2026-07-28 - Optimized GameStateManager note encoding serialization
**Learning:** The `encodeNotes` method in `GameStateManager` was using `String.format(Locale.US, "%03X", mask)` repeatedly in a tight loop to encode a 9x9 board into a hex string for undo/redo stacking. In an environment with frequent board updates, this induced massive CPU overhead and garbage collection pressure due to string allocations.
**Action:** Replaced the intermediate allocations from `String.format` with a pre-allocated static character array (`HEX_ARRAY`) and direct arithmetic/bit-shifting lookup to construct the hex string via a `StringBuilder`. This vastly improved serialization throughput with near-zero allocation cost.
