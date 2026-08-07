## $(date +%Y-%m-%d) - Optimize GameValidator.gridToString
**Learning:** High-frequency game loop operations, such as serializing an 81-cell Sudoku grid to a String (often for hints or state caching), can cause excessive heap allocations and garbage collection pressure if intermediate objects like `StringBuilder` or regex are used.
**Action:** When serializing fixed-size simple arrays (like `int[][]` of single digits 0-9), use a pre-allocated `char[]` and perform direct character math (`(char) ('0' + value)`), avoiding the `StringBuilder` appending and integer-to-string conversion entirely.
