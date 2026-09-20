package com.suthinee.calorietracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.suthinee.calorietracker.data.local.entity.ReminderEntity
import com.suthinee.calorietracker.data.repository.ReminderRepository
import com.suthinee.calorietracker.domain.model.ReminderType
import java.time.LocalDateTime
import java.time.ZoneId

const val EXTRA_REMINDER_TYPE = "extra_reminder_type"

/** Schedules/cancels local (device-only) alarms for meal & water reminders. No server involved. */
class ReminderScheduler(
    private val appContext: Context,
    private val reminderRepository: ReminderRepository
) {
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        NotificationHelper.ensureChannel(appContext)
    }

    suspend fun rescheduleAll() {
        reminderRepository.ensureDefaults()
        ReminderType.entries.forEach { type ->
            val reminder = reminderRepository.getByType(type)
            if (reminder != null && reminder.enabled) {
                schedule(reminder)
            } else {
                cancel(type)
            }
        }
    }

    fun schedule(reminder: ReminderEntity) {
        if (!reminder.enabled) {
            cancel(reminder.type)
            return
        }
        val pendingIntent = pendingIntentFor(reminder.type)
        val triggerAt = nextTriggerMillis(reminder)

        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(type: ReminderType) {
        alarmManager.cancel(pendingIntentFor(type))
    }

    private fun nextTriggerMillis(reminder: ReminderEntity): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var trigger = now.toLocalDate().atTime(reminder.time)
        if (!trigger.isAfter(now)) {
            trigger = trigger.plusDays(1)
        }
        return trigger.atZone(zone).toInstant().toEpochMilli()
    }

    private fun pendingIntentFor(type: ReminderType): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, type.name)
        }
        return PendingIntent.getBroadcast(
            appContext,
            type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
