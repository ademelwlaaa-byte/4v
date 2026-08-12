package com.example.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.repository.EmochiRepository
import java.util.concurrent.TimeUnit

class SummarizationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = EmochiRepository(db, applicationContext)

        val botsNeeding = db.botDao().getBotsNeedingSummarization()

        for (bot in botsNeeding) {
            // Skip if user is actively chatting with this bot right now
            if (bot.id == EmochiRepository.activeBotId) {
                continue
            }
            repository.performBackgroundSummarization(bot.id)
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "emochi_background_summarization_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SummarizationWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
