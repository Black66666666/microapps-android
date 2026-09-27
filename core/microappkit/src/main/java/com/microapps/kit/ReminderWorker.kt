package com.microapps.kit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

private const val CHANNEL = "microapp_reminders"

class ReminderWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Напоминания", NotificationManager.IMPORTANCE_DEFAULT))
        val title = inputData.getString("title") ?: "Напоминание"
        val body = inputData.getString("body") ?: "Проверь приложение"
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()
        if (androidx.core.content.ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33) {
            NotificationManagerCompat.from(applicationContext).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
        }
        return Result.success()
    }
}

fun scheduleReminder(context: Context, uniqueName: String, whenMillis: Long, title: String, body: String) {
    val delay = (whenMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    val request = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(Data.Builder().putString("title", title).putString("body", body).build())
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(uniqueName, androidx.work.ExistingWorkPolicy.REPLACE, request)
}
