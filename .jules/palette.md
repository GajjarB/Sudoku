## 2025-02-18 - Visual Indicators for Loading States
**Learning:** Static "Loading..." text in Android views (like `loading_panel` in `activity_game.xml`) provides poor UX because it offers no visual indication that the application is actively processing.
**Action:** Always pair loading text with a visual indicator, such as an indeterminate `ProgressBar` (spinner), matching the app's primary accent color (`@color/accent_primary`) to provide immediate feedback that the app hasn't frozen.
