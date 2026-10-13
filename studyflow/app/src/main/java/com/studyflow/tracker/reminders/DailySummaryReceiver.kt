package com.studyflow.tracker.reminders

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
import com.studyflow.tracker.MainActivity
import com.studyflow.tracker.R
import com.studyflow.tracker.data.StudyMath
import com.studyflow.tracker.data.StudyRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object DailySummaryScheduler {
    private const val REQUEST_CODE = 2101
    private const val EXTRA_DAY = "summary_day"

    fun sync(context: Context, repository: StudyRepository) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pending = pendingIntent(context, LocalDate.now())
        alarmManager.cancel(pending)
        if (!repository.state.settings.notificationsEnabled) return

        val hour = repository.state.settings.reminderHour
        val now = LocalDateTime.now()
        var target = LocalDate.now().atTime(hour, 0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        val whenMs = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMs, pendingIntent(context, target.toLocalDate()))
    }

    private fun pendingIntent(context: Context, day: LocalDate): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, DailySummaryReceiver::class.java).putExtra(EXTRA_DAY, day.toString()),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    fun day(intent: Intent): LocalDate = runCatching {
        LocalDate.parse(intent.getStringExtra(EXTRA_DAY))
    }.getOrDefault(LocalDate.now())
}

class DailySummaryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repository = StudyRepository(context)
        if (!repository.state.settings.notificationsEnabled) return
        val day = DailySummaryScheduler.day(intent)
        val sessions = StudyMath.sessionsOn(repository.state, day)
        val body = if (sessions.isEmpty()) {
            "No sessions logged today. A little study time still counts."
        } else {
            val count = sessions.size
            "You studied for ${StudyMath.formatDuration(StudyMath.totalMs(sessions))} across $count ${if (count == 1) "session" else "sessions"}. Nice work!"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("daily_summary", "Daily study summary", NotificationManager.IMPORTANCE_DEFAULT))
        if (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val openApp = PendingIntent.getActivity(
                context, 2102, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = Notification.Builder(context, "daily_summary")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Your study day at a glance")
                .setContentText(body)
                .setStyle(Notification.BigTextStyle().bigText(body))
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build()
            manager.notify(2101, notification)
        }
        DailySummaryScheduler.sync(context, repository)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            DailySummaryScheduler.sync(context, StudyRepository(context))
        }
    }
}
