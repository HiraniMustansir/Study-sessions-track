package com.lessgomuss.tracker.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.lessgomuss.tracker.MainActivity
import com.lessgomuss.tracker.R
import com.lessgomuss.tracker.data.*
import java.time.LocalDate
import java.time.ZonedDateTime

object DailySummaryScheduler {
    private fun pending(context: Context, id: String, kind: String = "", day: String = ""): PendingIntent =
        PendingIntent.getBroadcast(context, 0, Intent(context, DailySummaryReceiver::class.java)
            .setAction("com.lessgomuss.reminder.$id").putExtra("kind", kind).putExtra("day", day),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun sync(context: Context, repository: StudyRepository) {
        val alarms = context.getSystemService(AlarmManager::class.java)
        val prefs = context.getSharedPreferences("reminder_alarms", Context.MODE_PRIVATE)
        prefs.getStringSet("ids", emptySet())!!.forEach { alarms.cancel(pending(context, it)) }
        val ids = mutableSetOf<String>()
        val state = repository.state
        val settings = state.settings
        if (settings.notificationsEnabled) {
            val reminders = listOf(StudyReminder("summary", "summary", settings.reminderHour, settings.reminderMinute)) + settings.reminders
            val now = ZonedDateTime.now()
            reminders.filter { it.enabled }.forEach { reminder ->
                StudyInsights.nextReminder(reminder, settings, now)?.let { target ->
                    val id = "scheduled-${reminder.id}"
                    alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.toInstant().toEpochMilli(), pending(context, id, reminder.kind, target.toLocalDate().toString()))
                    ids.add(id)
                }
            }
            state.timer?.takeIf { it.resumedAt != null && it.targetMinutes > 0 }?.let { timer ->
                val remaining = timer.targetMinutes * 60_000L - StudyMath.elapsed(timer, System.currentTimeMillis())
                if (remaining > 0) {
                    alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + remaining, pending(context, "focus", "focus", timer.startedAt.toString()))
                    ids.add("focus")
                }
            }
        }
        prefs.edit().putStringSet("ids", ids).apply()
    }
}

class DailySummaryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repository = StudyRepository(context)
        val state = repository.state
        if (!state.settings.notificationsEnabled) return
        val kind = intent.getStringExtra("kind") ?: return
        val day = runCatching { LocalDate.parse(intent.getStringExtra("day")) }.getOrDefault(LocalDate.now())
        val sessions = StudyMath.sessionsOn(state, day)
        val total = StudyMath.totalMs(sessions)
        val due = state.tasks.count { !it.completed && it.dueDate <= day.toString() }
        val timer = state.timer
        val validFocus = timer != null && timer.resumedAt != null && timer.startedAt.toString() == intent.getStringExtra("day") &&
            StudyMath.elapsed(timer, System.currentTimeMillis()) >= timer.targetMinutes * 60_000L
        val message = when (kind) {
            "focus" -> if (validFocus) "Focus block complete" to "Take a short break when you're ready. Your timer keeps running until you pause or finish." else null
            "goal" -> if (total < state.settings.dailyGoalMinutes * 60_000L && timer == null) "A little more focus" to "${StudyMath.formatDuration(state.settings.dailyGoalMinutes * 60_000L - total)} left to reach your daily goal." else null
            "plan" -> if (due > 0) "Your study plan" to "$due unfinished ${if (due == 1) "task is" else "tasks are"} due. Choose one small next step." else null
            "start" -> if (timer == null) "Time to make progress" to "Choose a subject, pick one task, and start a focus block." else null
            "summary" -> "Your study day at a glance" to "${StudyMath.formatDuration(total)} studied · ${sessions.size} sessions · $due tasks due."
            else -> null
        }
        if (message != null && !StudyInsights.isQuiet(ZonedDateTime.now().hour, state.settings)) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = if (kind == "focus") "focus_blocks" else "study_routine"
            manager.createNotificationChannel(NotificationChannel(channel, if (kind == "focus") "Focus blocks" else "Study routine", NotificationManager.IMPORTANCE_DEFAULT))
            if (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                val open = PendingIntent.getActivity(context, 2102, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                manager.notify((intent.action ?: kind).hashCode(), Notification.Builder(context, channel).setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(message.first).setContentText(message.second).setStyle(Notification.BigTextStyle().bigText(message.second))
                    .setContentIntent(open).setAutoCancel(true).build())
            }
        }
        DailySummaryScheduler.sync(context, repository)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in listOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED))
            DailySummaryScheduler.sync(context, StudyRepository(context))
    }
}
