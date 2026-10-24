package com.lessgomuss.tracker.data

import org.json.JSONArray
import org.json.JSONObject

object StudyBackupCodec {
    fun export(state: StudyState, exportedAt: Long = System.currentTimeMillis()): String {
        require(state.timer == null) { "Finish or discard the active session before exporting." }
        return JSONObject().apply {
            put("format", "lessgomuss-backup")
            put("version", 2)
            put("exportedAt", exportedAt)
            put("data", encode(state))
        }.toString(2)
    }

    fun import(raw: String): StudyState {
        require(raw.length <= 10_000_000) { "Backup file is too large." }
        val wrapper = JSONObject(raw)
        require(wrapper.getString("format") == "lessgomuss-backup") { "This is not a Lessgo muss! backup." }
        require(wrapper.getInt("version") in 1..2) { "This backup version is not supported." }
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
            put("note", session.note); put("focusRating", session.focusRating); put("taskId", session.taskId)
        }) } })
        put("tasks", JSONArray().apply { state.tasks.forEach { task -> put(JSONObject().apply {
            put("id", task.id); put("title", task.title); put("subjectId", task.subjectId); put("dueDate", task.dueDate)
            put("estimateMinutes", task.estimateMinutes); put("priority", task.priority); put("completed", task.completed)
        }) } })
        put("timer", state.timer?.let { timer -> JSONObject().apply {
            put("subjectId", timer.subjectId); put("startedAt", timer.startedAt)
            put("resumedAt", timer.resumedAt); put("elapsedBeforePauseMs", timer.elapsedBeforePauseMs)
            put("targetMinutes", timer.targetMinutes); put("taskId", timer.taskId); put("note", timer.note)
        } } ?: JSONObject.NULL)
        put("settings", JSONObject().apply {
            put("dailyGoalMinutes", state.settings.dailyGoalMinutes)
            put("notificationsEnabled", state.settings.notificationsEnabled)
            put("reminderHour", state.settings.reminderHour)
            put("reminderMinute", state.settings.reminderMinute)
            put("weeklyGoalMinutes", state.settings.weeklyGoalMinutes)
            put("focusMinutes", state.settings.focusMinutes)
            put("quietEnabled", state.settings.quietEnabled)
            put("quietStartHour", state.settings.quietStartHour)
            put("quietEndHour", state.settings.quietEndHour)
            put("reminders", JSONArray().apply { state.settings.reminders.forEach { reminder -> put(JSONObject().apply {
                put("id", reminder.id); put("kind", reminder.kind); put("hour", reminder.hour); put("minute", reminder.minute)
                put("weekdays", JSONArray(reminder.weekdays.sorted())); put("enabled", reminder.enabled)
            }) } })
        })
    }

    fun decode(json: JSONObject): StudyState {
        val subjects = json.getJSONArray("subjects").let { array -> (0 until array.length()).map { i ->
            array.getJSONObject(i).let { Subject(it.getString("id"), it.getString("name"), it.getLong("color"), it.getString("icon")) }
        } }
        val sessions = json.getJSONArray("sessions").let { array -> (0 until array.length()).map { i ->
            array.getJSONObject(i).let { StudySession(it.getString("id"), it.getString("subjectId"), it.getLong("startedAt"), it.getLong("endedAt"), it.getLong("durationMs"), it.optString("note", ""), it.optInt("focusRating", 0), it.optionalId("taskId")) }
        } }
        val timer = json.optJSONObject("timer")?.let {
            StudyTimer(it.getString("subjectId"), it.getLong("startedAt"), if (it.isNull("resumedAt")) null else it.getLong("resumedAt"), it.getLong("elapsedBeforePauseMs"), it.optInt("targetMinutes", 0), it.optionalId("taskId"), it.optString("note", ""))
        }
        val settings = json.getJSONObject("settings").let {
            StudySettings(it.getInt("dailyGoalMinutes"), it.getBoolean("notificationsEnabled"), it.getInt("reminderHour"),
                it.optInt("reminderMinute", 0), it.optInt("weeklyGoalMinutes", 900), it.optInt("focusMinutes", 25),
                it.optBoolean("quietEnabled", false), it.optInt("quietStartHour", 22), it.optInt("quietEndHour", 7),
                it.optJSONArray("reminders").objects().map { r -> StudyReminder(r.getString("id"), r.getString("kind"), r.getInt("hour"), r.getInt("minute"),
                    r.getJSONArray("weekdays").let { days -> (0 until days.length()).map { i -> days.getInt(i) }.toSet() }, r.getBoolean("enabled")) })
        }
        val tasks = json.optJSONArray("tasks").objects().map { t -> StudyTask(t.getString("id"), t.getString("title"), t.getString("subjectId"), t.getString("dueDate"), t.getInt("estimateMinutes"), t.getInt("priority"), t.getBoolean("completed")) }
        return StudyState(subjects, sessions, timer, settings, tasks)
    }

    private fun JSONObject.optionalId(key: String): String? = if (isNull(key)) null else optString(key).takeIf { it.isNotEmpty() }
    private fun JSONArray?.objects(): List<JSONObject> = if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }

    private fun validate(imported: StudyState) {
        require(imported.subjects.size <= 1_000 && imported.sessions.size <= 200_000) { "Backup has too many entries." }
        require(imported.subjects.all { it.id.isNotBlank() && it.name.isNotBlank() && it.name.length <= 100 }) { "Backup has an invalid subject." }
        require(imported.subjects.map { it.id }.toSet().size == imported.subjects.size) { "Backup has duplicate subjects." }
        require(imported.sessions.all { it.id.isNotBlank() && it.startedAt >= 0 && it.endedAt >= it.startedAt && it.durationMs > 0 }) { "Backup has an invalid session." }
        require(imported.sessions.map { it.id }.toSet().size == imported.sessions.size) { "Backup has duplicate sessions." }
        require(imported.sessions.all { it.note.length <= 2000 && it.focusRating in 0..5 }) { "Backup has invalid session details." }
        require(imported.tasks.size <= 10000 && imported.tasks.map { it.id }.toSet().size == imported.tasks.size) { "Backup has invalid tasks." }
        require(imported.tasks.all { it.id.isNotBlank() && it.title.isNotBlank() && it.title.length <= 200 && it.estimateMinutes in 5..720 && it.priority in 0..2 && runCatching { java.time.LocalDate.parse(it.dueDate) }.isSuccess }) { "Backup has an invalid task." }
        val settings = imported.settings
        require(settings.reminderMinute in 0..59 && settings.weeklyGoalMinutes in 60..5040 && settings.focusMinutes in 5..180 && settings.quietStartHour in 0..23 && settings.quietEndHour in 0..23) { "Backup has invalid preferences." }
        require(settings.reminders.size <= 20 && settings.reminders.map { it.id }.toSet().size == settings.reminders.size && settings.reminders.all {
            it.id.isNotBlank() && it.kind in listOf("start", "goal", "plan") && it.hour in 0..23 && it.minute in 0..59 && it.weekdays.isNotEmpty() && it.weekdays.all { day -> day in 1..7 }
        }) { "Backup has invalid reminders." }
        require(imported.settings.dailyGoalMinutes in 15..720 && imported.settings.reminderHour in 0..23) { "Backup has invalid settings." }
    }
}
