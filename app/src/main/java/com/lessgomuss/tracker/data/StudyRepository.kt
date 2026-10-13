package com.lessgomuss.tracker.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

class StudyRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("lessgomuss_v1", Context.MODE_PRIVATE)
    var state by mutableStateOf(load())
        private set

    private fun update(transform: (StudyState) -> StudyState) {
        val next = transform(state)
        if (next == state) return
        state = next
        preferences.edit().putString("data", encode(next).toString()).apply()
    }

    fun start(subjectId: String, now: Long = System.currentTimeMillis()) = update { current ->
        if (current.timer != null || current.subjects.none { it.id == subjectId }) current
        else current.copy(timer = StudyTimer(subjectId, now, now, 0))
    }

    fun pause(now: Long = System.currentTimeMillis()) = update { current ->
        val timer = current.timer ?: return@update current
        if (timer.resumedAt == null) current
        else current.copy(timer = timer.copy(resumedAt = null, elapsedBeforePauseMs = StudyMath.elapsed(timer, now)))
    }

    fun resume(now: Long = System.currentTimeMillis()) = update { current ->
        val timer = current.timer ?: return@update current
        if (timer.resumedAt != null) current else current.copy(timer = timer.copy(resumedAt = now))
    }

    fun finish(now: Long = System.currentTimeMillis()) = update { current ->
        val timer = current.timer ?: return@update current
        val duration = StudyMath.elapsed(timer, now)
        val session = StudySession(StudyMath.newId(), timer.subjectId, timer.startedAt, now, duration)
        current.copy(timer = null, sessions = if (duration >= 1_000) listOf(session) + current.sessions else current.sessions)
    }

    fun discard() = update { it.copy(timer = null) }

    fun addSubject(name: String, color: Long, icon: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank() || state.subjects.any { it.name.equals(trimmed, ignoreCase = true) }) return false
        update { it.copy(subjects = it.subjects + Subject(StudyMath.newId(), trimmed, color, icon)) }
        return true
    }

    fun deleteSubject(id: String) = update { current ->
        if (current.timer?.subjectId == id) current
        else current.copy(subjects = current.subjects.filterNot { it.id == id })
    }

    fun deleteSession(id: String) = update { it.copy(sessions = it.sessions.filterNot { session -> session.id == id }) }
    fun setGoal(minutes: Int) = update { it.copy(settings = it.settings.copy(dailyGoalMinutes = minutes.coerceIn(15, 720))) }
    fun setNotifications(enabled: Boolean) = update { it.copy(settings = it.settings.copy(notificationsEnabled = enabled)) }
    fun setReminderHour(hour: Int) = update { it.copy(settings = it.settings.copy(reminderHour = hour.coerceIn(0, 23))) }

    private fun load(): StudyState {
        val raw = preferences.getString("data", null) ?: return StudyState()
        return runCatching { decode(JSONObject(raw)) }.getOrElse { StudyState() }
    }

    private fun encode(state: StudyState) = JSONObject().apply {
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

    private fun decode(json: JSONObject): StudyState {
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
}
