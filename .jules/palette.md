
## 2026-06-25 - Improve Static Loading Screens
**Learning:** Static text alone (e.g., "Loading...") for asynchronous operations like game initialization can feel uncomfortably like a frozen app state. This is especially true on screens where users expect dynamic interaction.
**Action:** Always pair loading text with an indeterminate progress indicator (`ProgressBar` spinner in Android) for screens that block interaction while initializing. Use `indeterminateTint` that aligns with the app's `accent_primary` theme.
