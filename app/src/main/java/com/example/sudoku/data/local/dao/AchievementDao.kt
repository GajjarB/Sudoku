package com.example.sudoku.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sudoku.data.local.entities.Achievement

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1")
    fun getUnlockedAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    fun getAchievementById(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAchievements(achievements: List<Achievement>)

    @Update
    fun updateAchievement(achievement: Achievement)
}
