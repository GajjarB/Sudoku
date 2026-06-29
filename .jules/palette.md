
## 2024-03-24 - Visual Loading States for Sudoku App
**Learning:** Static "Preparing puzzle..." text loading screens in `GameActivity` lack interactive feedback, making users wonder if the app is frozen during complex puzzle generation. This creates poor UX when generation takes longer than a few hundred milliseconds.
**Action:** Replaced static loading `TextView` with a `LinearLayout` containing a `ProgressBar` (indeterminate spinner) tinted with `accent_primary`. This pattern ensures immediate visual feedback while puzzles are generating asynchronously, easing user anxiety and improving perceived performance.
