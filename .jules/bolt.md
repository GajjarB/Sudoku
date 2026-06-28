## 2024-05-23 - Prevented O(N) Memory Scaling in Stats ViewModel
**Learning:** In the `StatsViewModel`, the "MONTH" and "ALL" filters were loading the entire `GameHistory` list into memory (`database.gameDao().getGamesSince(since)`) just to populate a static 7-day chart view via nested iteration. This created a significant memory bottleneck for long-term users.
**Action:** Always ensure that data queried strictly matches UI needs. Introduced a specific `chartSince` 7-day lookback for the loop query, effectively decoupling chart population from the potentially unbounded `since` date used for aggregated statistic queries.
## 2024-06-20 - Optimized ProfileViewModel GameHistory loading
**Learning:** In the `ProfileViewModel`, checking for lifetime achievements (Perfect Game, Speed Solver) by querying the most recent 50 `GameHistory` objects into memory, then iterating over them, creates unnecessary O(N) allocation and serialization overhead. This also introduces a logic flaw where achievements could be 'forgotten' if the matching game falls outside the last 50.
**Action:** Replaced the in-memory object loop with highly optimized `SELECT EXISTS(...)` Room queries (`hasPerfectGame`, `hasFastGame`). This leverages SQLite efficiently, returning a single boolean and avoiding excessive JVM allocations.

## 2024-07-02 - Prevented Redundant DB Queries in LevelRepository
**Learning:** The `ensureLevelsInitializedSync()` method in `LevelRepository` was running `SELECT COUNT(*) FROM level_progress` on almost every method call (e.g., `isLevelUnlockedSync`, `getHighestUnlockedLevelSync`, `completeLevel`). This created a significant bottleneck, especially during initialization checks in game and home screen workflows.
**Action:** Introduced a `volatile boolean isInitializedCache` memory cache. Always check this fast in-memory flag before executing expensive database queries like `COUNT(*)` to check if a large initial state is set up.
