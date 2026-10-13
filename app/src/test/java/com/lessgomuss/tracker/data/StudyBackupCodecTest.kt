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

    @Test fun rejectsUnsupportedVersionBeforeReplacingAnything() {
        val backup = JSONObject(StudyBackupCodec.export(StudyState()))
        backup.put("version", 2)
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
