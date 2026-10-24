package com.lessgomuss.tracker.data

import java.time.LocalDate
import java.time.ZonedDateTime

object StudyInsights {
    fun nextReminder(reminder: StudyReminder, settings: StudySettings, now: ZonedDateTime): ZonedDateTime? =
        (0L..7L).map { now.toLocalDate().plusDays(it).atTime(reminder.hour, reminder.minute).atZone(now.zone) }
            .firstOrNull { it.isAfter(now) && it.dayOfWeek.value in reminder.weekdays && !isQuiet(it.hour, settings) }

    fun isQuiet(hour: Int, settings: StudySettings): Boolean = settings.quietEnabled &&
        if (settings.quietStartHour == settings.quietEndHour) false
        else if (settings.quietStartHour < settings.quietEndHour) hour in settings.quietStartHour until settings.quietEndHour
        else hour >= settings.quietStartHour || hour < settings.quietEndHour

    fun goalDays(state: StudyState, start: LocalDate, end: LocalDate): Int =
        state.sessions.groupBy { StudyMath.dayOf(it) }.count { (day, sessions) ->
            !day.isBefore(start) && !day.isAfter(end) && StudyMath.totalMs(sessions) >= state.settings.dailyGoalMinutes * 60_000L
        }

    fun longestStreak(state: StudyState): Int {
        var best = 0
        var run = 0
        var previous: LocalDate? = null
        state.sessions.map { StudyMath.dayOf(it) }.distinct().sorted().forEach { day ->
            run = if (previous?.plusDays(1) == day) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }
        return best
    }
}
