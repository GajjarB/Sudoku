# Palette's Journal

## Critical UX/Accessibility Learnings
## $(date +%Y-%m-%d) - ProgressBar Accessibility
**Learning:** ProgressBars (both determinate and indeterminate) in Android layout XML files must have `contentDescription` attributes so that screen readers can communicate their purpose (e.g., "Game progress" vs "Easy difficulty progress"), as these views are not intrinsically self-describing.
**Action:** Always add string resources and `contentDescription` attributes to `<ProgressBar>` elements when creating or updating layouts.
