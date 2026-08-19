## 2024-05-18 - String Creation in Game Engine Loops
**Learning:** Frequent grid serialization (`gridToString`) within tight algorithms like Sudoku generation, validation, and canonicalization causes significant garbage collection overhead when using `StringBuilder` or standard concatenation, degrading Android engine performance.
**Action:** Always use pre-allocated `char[]` arrays and direct character arithmetic `(char) ('0' + value)` to build strings in `O(N)` time without intermediate object allocations in hot algorithmic loops.
