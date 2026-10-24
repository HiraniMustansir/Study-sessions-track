package com.lessgomuss.tracker.data

import org.junit.Assert.*
import org.junit.Test
import java.time.*

class StudyInsightsTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    @Test fun reminderRespectsMinutesWeekdaysAndPassedTimes() {
        val friday = ZonedDateTime.of(2026, 10, 23, 9, 31, 0, 0, zone)
        val reminder = StudyReminder("work", hour = 9, minute = 30, weekdays = setOf(1, 3, 5))
        assertEquals("2026-10-26T09:30+05:30[Asia/Kolkata]", StudyInsights.nextReminder(reminder, StudySettings(), friday).toString())
        assertEquals(friday.withHour(10).withMinute(15), StudyInsights.nextReminder(reminder.copy(hour = 10, minute = 15), StudySettings(), friday))
    }
    @Test fun quietHoursHandleMidnightAndEqualBoundaries() {
        val settings = StudySettings(quietEnabled = true, quietStartHour = 22, quietEndHour = 7)
        assertTrue(StudyInsights.isQuiet(23, settings))
        assertTrue(StudyInsights.isQuiet(6, settings))
        assertFalse(StudyInsights.isQuiet(7, settings))
        assertFalse(StudyInsights.isQuiet(21, settings))
        assertFalse(StudyInsights.isQuiet(22, settings.copy(quietEndHour = 22)))
        assertNull(StudyInsights.nextReminder(StudyReminder("night", hour = 23), settings, ZonedDateTime.now(zone)))
        assertTrue(StudyInsights.isQuiet(13, settings.copy(quietStartHour = 12, quietEndHour = 14)))
    }
    @Test fun longestStreakAndGoalsUseDistinctDaysAndSavedTime() {
        val today = LocalDate.of(2026, 10, 24)
        val sessions = listOf(0L, 1L, 2L, 4L, 4L).mapIndexed { index, days ->
            val end = today.minusDays(days).atTime(18, 0).atZone(zone).toInstant().toEpochMilli()
            StudySession("$index", "mathematics", end - 3600000, end, 3600000)
        }
        val state = StudyState(sessions = sessions, settings = StudySettings(dailyGoalMinutes = 90))
        assertEquals(3, StudyInsights.longestStreak(state))
        assertEquals(1, StudyInsights.goalDays(state, today.minusDays(7), today))
        assertEquals(0, StudyInsights.longestStreak(StudyState()))
    }
}
