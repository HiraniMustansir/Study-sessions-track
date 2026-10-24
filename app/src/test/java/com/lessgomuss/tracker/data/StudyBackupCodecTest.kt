package com.lessgomuss.tracker.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StudyBackupCodecTest {
    @Test fun backupRoundTripKeepsHistoryAndSettings() {
        val state = StudyState(
            subjects = listOf(Subject("physics", "Physics", 0xFF82B8ABL, "science")),
            sessions = listOf(StudySession("one", "physics", 1_000, 61_000, 60_000)),
            settings = StudySettings(240, true, 20),
        )
        assertEquals(state, StudyBackupCodec.import(StudyBackupCodec.export(state, 123L)))
    }

    @Test fun versionOneBackupMigratesWithoutLosingHistory() {
        val backup = JSONObject("""{"format":"lessgomuss-backup","version":1,"data":{"subjects":[{"id":"math","name":"Math","color":123,"icon":"book"}],"sessions":[{"id":"old","subjectId":"math","startedAt":1000,"endedAt":61000,"durationMs":60000}],"timer":null,"settings":{"dailyGoalMinutes":120,"notificationsEnabled":true,"reminderHour":20}}}""")
        val state = StudyBackupCodec.import(backup.toString())
        assertEquals(120, state.settings.dailyGoalMinutes)
        assertEquals(20, state.settings.reminderHour)
        assertEquals(0, state.settings.reminderMinute)
        assertEquals(60000L, state.sessions.single().durationMs)
        assertEquals(emptyList<StudyTask>(), state.tasks)
        assertEquals(state, StudyBackupCodec.import(StudyBackupCodec.export(state)))
    }

    @Test fun versionTwoRoundTripIncludesPlansNotesAndReminderDays() {
        val state = StudyState(
            sessions = listOf(StudySession("s", "mathematics", 1000, 61000, 60000, "Reviewed algebra", 4, "task")),
            tasks = listOf(StudyTask("task", "Review algebra", "mathematics", "2026-10-24", 50, 2, true)),
            settings = StudySettings(reminderMinute = 35, weeklyGoalMinutes = 1200, quietEnabled = true,
                reminders = listOf(StudyReminder("weekday", "goal", 18, 45, setOf(1, 3, 5)))),
        )
        assertEquals(state, StudyBackupCodec.import(StudyBackupCodec.export(state)))
    }

    @Test fun persistedTimerRetainsPausedStateTargetAndTaskAfterRestart() {
        val state = StudyState(timer = StudyTimer("mathematics", 1000, null, 125000, 25, "task"))
        val restored = StudyBackupCodec.decode(JSONObject(StudyBackupCodec.encode(state).toString()))
        assertEquals(state, restored)
        assertEquals(125000L, StudyMath.elapsed(restored.timer!!, 9000000))
        val running = state.copy(timer = state.timer!!.copy(resumedAt = 200000))
        val restarted = StudyBackupCodec.decode(JSONObject(StudyBackupCodec.encode(running).toString()))
        assertEquals(185000L, StudyMath.elapsed(restarted.timer!!, 260000))
    }

    @Test fun rejectsMalformedNewPreferencesAndTasks() {
        listOf(
            StudyState(settings = StudySettings(reminderMinute = 60)),
            StudyState(settings = StudySettings(reminders = listOf(StudyReminder("bad", weekdays = emptySet())))),
            StudyState(tasks = listOf(StudyTask("bad", "Task", "mathematics", "not-a-date"))),
            StudyState(sessions = listOf(StudySession("bad", "mathematics", 1000, 61000, 60000, focusRating = 9))),
        ).forEach { state -> assertThrows(IllegalArgumentException::class.java) { StudyBackupCodec.import(StudyBackupCodec.export(state)) } }
    }

    @Test fun rejectsUnsupportedVersionBeforeReplacingAnything() {
        val backup = JSONObject(StudyBackupCodec.export(StudyState()))
        backup.put("version", 3)
        assertThrows(IllegalArgumentException::class.java) { StudyBackupCodec.import(backup.toString()) }
    }

    @Test fun rejectsDuplicateSessionIdsAndActiveTimers() {
        val state = StudyState(sessions = listOf(StudySession("same", "mathematics", 1_000, 2_000, 1_000)))
        val duplicate = JSONObject(StudyBackupCodec.export(state))
        val sessions = duplicate.getJSONObject("data").getJSONArray("sessions")
        sessions.put(JSONObject(sessions.getJSONObject(0).toString()))
        assertThrows(IllegalArgumentException::class.java) { StudyBackupCodec.import(duplicate.toString()) }
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupCodec.export(StudyState(timer = StudyTimer("mathematics", 1_000, 1_000, 0)))
        }
    }
}
