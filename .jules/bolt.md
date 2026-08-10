## 2026-07-04 - [Optimize gridToString serialization]
**Learning:** In highly called serialization methods like `gridToString` (used heavily during hint tracking, validation, or network transport), using `StringBuilder` incurs overhead from intermediate allocations and underlying array resizing.
**Action:** Replace `StringBuilder` with a pre-allocated `char[]` and perform direct character math mapping to build the string in one step without object generation garbage.
