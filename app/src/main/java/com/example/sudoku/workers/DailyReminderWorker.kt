package com.example.sudoku.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sudoku.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("DailyReminderWorker", "Running daily reminder check")

        val dataStore = SettingsDataStore(applicationContext)
        val notificationsEnabled = dataStore.notificationsEnabledFlow.first()

        if (!notificationsEnabled) {
            return Result.success()
        }

        val lastPlayed = dataStore.dailyStreakTimestampFlow.first()
        val now = System.currentTimeMillis()
        val hoursSinceLastPlay = (now - lastPlayed) / (1000 * 60 * 60)

        // Only remind if it's been ~24 hours
        if (hoursSinceLastPlay >= 23) {
            sendNotification()
        } else {
            // Schedule dynamic next notification exactly 24 hours after last play
            val delayMillis = (24 * 60 * 60 * 1000) - (now - lastPlayed)
            val nextRequest = androidx.work.OneTimeWorkRequest.Builder(DailyReminderWorker::class.java)
                .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
                .addTag("daily_reminder")
                .build()
            androidx.work.WorkManager.getInstance(applicationContext).enqueueUniqueWork("daily_reminder", androidx.work.ExistingWorkPolicy.REPLACE, nextRequest)
        }

        return Result.success()
    }

    private fun sendNotification() {
        // Implementation for sending the local notification
        Log.d("DailyReminderWorker", "Time to play! Sending reminder notification.")
    }
}
