## 2024-05-23 - Prevented O(N) Memory Scaling in Stats ViewModel
**Learning:** In the `StatsViewModel`, the "MONTH" and "ALL" filters were loading the entire `GameHistory` list into memory (`database.gameDao().getGamesSince(since)`) just to populate a static 7-day chart view via nested iteration. This created a significant memory bottleneck for long-term users.
**Action:** Always ensure that data queried strictly matches UI needs. Introduced a specific `chartSince` 7-day lookback for the loop query, effectively decoupling chart population from the potentially unbounded `since` date used for aggregated statistic queries.

## 2024-05-24 - Further Prevented O(N) Memory Scaling in Stats ViewModel
**Learning:** The nested loops for `StatsViewModel`'s 7-day chart populated its values by loading actual `GameHistory` entities from Room. For high-volume users, querying these objects and looping over them is still slower than letting SQLite do the aggregation.
**Action:** Replaced object queries with a direct database COUNT aggregation, `getGameCountInRange`, for each of the 7 days to eliminate JVM loop overhead and entity instantiation entirely.

## 2024-05-24 - Eliminated JVM Memory Bottleneck in StatsViewModel
**Learning:** For users with extensive game history, iterating over large datasets populated from Room into the JVM was leading to memory overhead in `StatsViewModel` (specifically populating a fixed 7-day chart).
**Action:** Replaced retrieving full object lists (`database.gameDao().getGamesSince(chartSince)`) and JVM looping overhead with direct database aggregation queries: `getGameCountInRange`. SQLite optimized COUNT queries scale vastly better than instantiating and filtering entities in Android view models.
