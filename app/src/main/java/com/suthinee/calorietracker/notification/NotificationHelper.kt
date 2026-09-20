package com.suthinee.calorietracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.suthinee.calorietracker.MainActivity
import com.suthinee.calorietracker.R
import com.suthinee.calorietracker.domain.model.ReminderType

object NotificationHelper {
    const val CHANNEL_ID = "reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Meal & water reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Local reminders to log meals and drink water"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminder(context: Context, type: ReminderType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val (title, text) = messageFor(type)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            type.ordinal,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID_BASE + type.ordinal, notification)
    }

    private fun messageFor(type: ReminderType): Pair<String, String> = when (type) {
        ReminderType.BREAKFAST -> "🍽 Meal reminder" to "Have you logged your breakfast?"
        ReminderType.LUNCH -> "🍽 Meal reminder" to "Have you logged your lunch?"
        ReminderType.DINNER -> "🍽 Meal reminder" to "Have you logged your dinner?"
        ReminderType.SNACK -> "🍽 Meal reminder" to "Have you logged your snack?"
        ReminderType.WATER -> "💧 Water reminder" to "Time to drink some water!"
    }

    private const val NOTIFICATION_ID_BASE = 1000
}
