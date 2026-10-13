package com.lessgomuss.tracker.data

import org.json.JSONArray
import org.json.JSONObject

object StudyBackupCodec {
    fun export(state: StudyState, exportedAt: Long = System.currentTimeMillis()): String {
        require(state.timer == null) { "Finish or discard the active session before exporting." }
        return JSONObject().apply {
            put("format", "lessgomuss-backup")
            put("version", 1)
            put("exportedAt", exportedAt)
            put("data", encode(state))
        }.toString(2)
    }

    fun import(raw: String): StudyState {
        require(raw.length <= 10_000_000) { "Backup file is too large." }
        val wrapper = JSONObject(raw)
        require(wrapper.getString("format") == "lessgomuss-backup") { "This is not a Lessgo muss! backup." }
        require(wrapper.getInt("version") == 1) { "This backup version is not supported." }
        val imported = decode(wrapper.getJSONObject("data"))
        validate(imported)
        require(imported.timer == null) { "The backup contains an active timer. Finish it on the old phone and export again." }
        return imported
    }

    fun encode(state: StudyState) = JSONObject().apply {
        put("subjects", JSONArray().apply { state.subjects.forEach { subject -> put(JSONObject().apply {
            put("id", subject.id); put("name", subject.name); put("color", subject.color); put("icon", subject.icon)
        }) } })
        put("sessions", JSONArray().apply { state.sessions.forEach { session -> put(JSONObject().apply {
            put("id", session.id); put("subjectId", session.subjectId); put("startedAt", session.startedAt)
            put("endedAt", session.endedAt); put("durationMs", session.durationMs)
        }) } })
        put("timer", state.timer?.let { timer -> JSONObject().apply {
            put("subjectId", timer.subjectId); put("startedAt", timer.startedAt)
            put("resumedAt", timer.resumedAt); put("elapsedBeforePauseMs", timer.elapsedBeforePauseMs)
        } } ?: JSONObject.NULL)
        put("settings", JSONObject().apply {
            put("dailyGoalMinutes", state.settings.dailyGoalMinutes)
            put("notificationsEnabled", state.settings.notificationsEnabled)
            put("reminderHour", state.settings.reminderHour)
        })
    }

    fun decode(json: JSONObject): StudyState {
        val subjects = json.getJSONArray("subjects").let { array -> (0 until array.length()).map { i ->
            array.getJSONObject(i).let { Subject(it.getString("id"), it.getString("name"), it.getLong("color"), it.getString("icon")) }
        } }
        val sessions = json.getJSONArray("sessions").let { array -> (0 until array.length()).map { i ->
            array.getJSONObject(i).let { StudySession(it.getString("id"), it.getString("subjectId"), it.getLong("startedAt"), it.getLong("endedAt"), it.getLong("durationMs")) }
        } }
        val timer = json.optJSONObject("timer")?.let {
            StudyTimer(it.getString("subjectId"), it.getLong("startedAt"), if (it.isNull("resumedAt")) null else it.getLong("resumedAt"), it.getLong("elapsedBeforePauseMs"))
        }
        val settings = json.getJSONObject("settings").let {
            StudySettings(it.getInt("dailyGoalMinutes"), it.getBoolean("notificationsEnabled"), it.getInt("reminderHour"))
        }
        return StudyState(subjects, sessions, timer, settings)
    }

    private fun validate(imported: StudyState) {
        require(imported.subjects.size <= 1_000 && imported.sessions.size <= 200_000) { "Backup has too many entries." }
        require(imported.subjects.all { it.id.isNotBlank() && it.name.isNotBlank() && it.name.length <= 100 }) { "Backup has an invalid subject." }
        require(imported.subjects.map { it.id }.toSet().size == imported.subjects.size) { "Backup has duplicate subjects." }
        require(imported.sessions.all { it.id.isNotBlank() && it.startedAt >= 0 && it.endedAt >= it.startedAt && it.durationMs > 0 }) { "Backup has an invalid session." }
        require(imported.sessions.map { it.id }.toSet().size == imported.sessions.size) { "Backup has duplicate sessions." }
        require(imported.settings.dailyGoalMinutes in 15..720 && imported.settings.reminderHour in 0..23) { "Backup has invalid settings." }
    }
}
