package com.studyflow.tracker.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StudyMathTest {
    @Test fun timerCountsRunningTimeAndStopsDuringPause() {
        val running = StudyTimer("math", 1_000, 2_000, 30_000)
        assertEquals(35_000, StudyMath.elapsed(running, 7_000))
        assertEquals(35_000, StudyMath.elapsed(running.copy(resumedAt = null, elapsedBeforePauseMs = 35_000), 900_000))
    }

    @Test fun sessionBelongsToCompletionDayInLocalZone() {
        val zone = ZoneId.of("Asia/Kolkata")
        val end = LocalDate.of(2026, 10, 14).atStartOfDay(zone).plusMinutes(5).toInstant().toEpochMilli()
        val session = StudySession("1", "math", end - 20 * 60_000, end, 20 * 60_000)
        assertEquals(LocalDate.of(2026, 10, 14), StudyMath.dayOf(session, zone))
    }

    @Test fun streakIncludesYesterdayWhenTodayIsNotStudiedYet() {
        val zone = ZoneId.of("UTC")
        val today = LocalDate.of(2026, 10, 13)
        val sessions = listOf(1L, 2L, 4L).map { daysAgo ->
            val end = today.minusDays(daysAgo).atStartOfDay(zone).toInstant().toEpochMilli()
            StudySession("$daysAgo", "math", end - 60_000, end, 60_000)
        }
        assertEquals(2, StudyMath.streak(StudyState(sessions = sessions), today, zone))
    }

    @Test fun totalsUseOnlySavedDuration() {
        val sessions = listOf(
            StudySession("1", "math", 0, 1_000, 30_000),
            StudySession("2", "science", 0, 1_000, 60_000),
        )
        assertEquals(90_000, StudyMath.totalMs(sessions))
    }
}
