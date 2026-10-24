package com.lessgomuss.tracker.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class Subject(val id: String, val name: String, val color: Long, val icon: String)
data class StudySession(
    val id: String,
    val subjectId: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationMs: Long,
    val note: String = "",
    val focusRating: Int = 0,
    val taskId: String? = null,
)
data class StudyTimer(
    val subjectId: String,
    val startedAt: Long,
    val resumedAt: Long?,
    val elapsedBeforePauseMs: Long,
    val targetMinutes: Int = 0,
    val taskId: String? = null,
    val note: String = "",
)
data class StudyTask(
    val id: String,
    val title: String,
    val subjectId: String,
    val dueDate: String,
    val estimateMinutes: Int = 25,
    val priority: Int = 1,
    val completed: Boolean = false,
)
data class StudyReminder(
    val id: String,
    val kind: String = "start",
    val hour: Int = 9,
    val minute: Int = 0,
    val weekdays: Set<Int> = (1..7).toSet(),
    val enabled: Boolean = true,
)
data class StudySettings(
    val dailyGoalMinutes: Int = 180,
    val notificationsEnabled: Boolean = false,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,
    val weeklyGoalMinutes: Int = 900,
    val focusMinutes: Int = 25,
    val quietEnabled: Boolean = false,
    val quietStartHour: Int = 22,
    val quietEndHour: Int = 7,
    val reminders: List<StudyReminder> = emptyList(),
)
data class StudyState(
    val subjects: List<Subject> = listOf(
        Subject("mathematics", "Mathematics", 0xFFF3A57D, "calculate"),
        Subject("science", "Science", 0xFF82B8AB, "science"),
        Subject("reading", "Reading", 0xFF9A9BCB, "book"),
    ),
    val sessions: List<StudySession> = emptyList(),
    val timer: StudyTimer? = null,
    val settings: StudySettings = StudySettings(),
    val tasks: List<StudyTask> = emptyList(),
)

object StudyMath {
    fun elapsed(timer: StudyTimer, now: Long): Long = timer.elapsedBeforePauseMs +
        (timer.resumedAt?.let { (now - it).coerceAtLeast(0) } ?: 0)

    fun dayOf(session: StudySession, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(session.endedAt).atZone(zone).toLocalDate()

    fun sessionsOn(state: StudyState, day: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<StudySession> =
        state.sessions.filter { dayOf(it, zone) == day }

    fun totalMs(sessions: List<StudySession>): Long = sessions.sumOf { it.durationMs }

    fun streak(state: StudyState, today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): Int {
        val studied = state.sessions.map { dayOf(it, zone) }.toSet()
        var cursor = if (today in studied) today else today.minusDays(1)
        var count = 0
        while (cursor in studied) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    fun formatDuration(ms: Long): String {
        val minutes = (ms / 60_000.0).toLong()
        if (minutes < 1) return "<1 min"
        val hours = minutes / 60
        return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m"
    }

    fun formatClock(ms: Long): String {
        val seconds = (ms.coerceAtLeast(0) / 1000)
        return "%02d:%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60, seconds % 60)
    }

    fun newId(): String = UUID.randomUUID().toString()
}
