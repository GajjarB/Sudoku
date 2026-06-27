## 2024-06-25 - Improve Loading State Visibility
**Learning:** Static text for loading states (like "Loading...") can be easily missed or perceived as a frozen screen. Adding an animated element, such as an indeterminate ProgressBar, significantly improves the perceived performance and provides clear feedback that a process is ongoing.
**Action:** Always pair loading text with a visual indicator of progress or activity (like an indeterminate spinner) using app theme colors (e.g., `indeterminateTint="@color/accent_primary"`) to maintain consistent UI.
