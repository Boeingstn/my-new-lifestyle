package com.suthinee.calorietracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.suthinee.calorietracker.CalorieTrackerApp
import com.suthinee.calorietracker.domain.model.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val typeName = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: return
        val type = runCatching { ReminderType.valueOf(typeName) }.getOrNull() ?: return

        val pendingResult = goAsync()
        val app = context.applicationContext as CalorieTrackerApp
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val reminder = app.container.reminderRepository.getByType(type)
                if (reminder != null && reminder.enabled) {
                    val quietHours = app.container.reminderRepository.observeSettings().first()
                    val now = LocalTime.now()
                    val inQuietHours = quietHours.quietHoursEnabled && isWithinQuietHours(
                        now,
                        quietHours.quietHoursStart,
                        quietHours.quietHoursEnd
                    )
                    if (!inQuietHours) {
                        NotificationHelper.showReminder(context, type)
                    }
                    // Reschedule tomorrow's occurrence regardless, so the reminder keeps recurring daily.
                    app.container.reminderScheduler.schedule(reminder)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun isWithinQuietHours(now: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return if (start.isBefore(end)) {
            !now.isBefore(start) && now.isBefore(end)
        } else {
            // Quiet hours wrap past midnight, e.g. 22:00 - 07:00.
            !now.isBefore(start) || now.isBefore(end)
        }
    }
}
