package com.example.sudoku.data.local.dao;

import androidx.room.ColumnInfo;

public class GameStats {
    @ColumnInfo(name = "totalTime")
    public long totalTime;

    @ColumnInfo(name = "bestTime")
    public long bestTime;

    @ColumnInfo(name = "xpEarned")
    public int xpEarned;

    @ColumnInfo(name = "easyCount")
    public int easyCount;

    @ColumnInfo(name = "mediumCount")
    public int mediumCount;

    @ColumnInfo(name = "hardCount")
    public int hardCount;

    @ColumnInfo(name = "expertCount")
    public int expertCount;

    @ColumnInfo(name = "gameCount")
    public int gameCount;
}
