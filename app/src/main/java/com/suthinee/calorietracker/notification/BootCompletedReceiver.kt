package com.suthinee.calorietracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.suthinee.calorietracker.CalorieTrackerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarms don't survive a reboot, so reschedule every enabled reminder once the device comes back up. */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val app = context.applicationContext as CalorieTrackerApp
        CoroutineScope(Dispatchers.Default).launch {
            try {
                app.container.reminderScheduler.rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
