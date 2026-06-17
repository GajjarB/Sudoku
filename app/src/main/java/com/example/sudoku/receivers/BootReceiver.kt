package com.example.sudoku.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.sudoku.utils.Constants
import com.example.sudoku.workers.DailyReminderWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val workManager = WorkManager.getInstance(context)

            // Re-schedule daily reminder worker
            val reminderRequest = androidx.work.OneTimeWorkRequest.Builder(
                DailyReminderWorker::class.java)
                .addTag("daily_reminder")
                .build()

            workManager.enqueueUniqueWork(
                "daily_reminder",
                androidx.work.ExistingWorkPolicy.KEEP,
                reminderRequest
            )
        }
    }
}
