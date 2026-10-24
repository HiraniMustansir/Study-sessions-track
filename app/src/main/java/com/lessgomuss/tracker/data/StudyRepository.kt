package com.lessgomuss.tracker.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class StudyRepository(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = context.applicationContext.getSharedPreferences("lessgomuss_v1", Context.MODE_PRIVATE)
    var state by mutableStateOf(load())
        private set

    private fun update(transform: (StudyState) -> StudyState) {
        val next = transform(state)
        if (next == state) return
        persist(next)
        state = next
        com.lessgomuss.tracker.reminders.DailySummaryScheduler.sync(appContext, this)
    }

    fun start(subjectId: String, now: Long = System.currentTimeMillis(), targetMinutes: Int = 0, taskId: String? = null) = update { current ->
        if (current.timer != null || current.subjects.none { it.id == subjectId }) current
        else current.copy(timer = StudyTimer(subjectId, now, now, 0, targetMinutes.coerceIn(0, 180), taskId))
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

    fun finish(now: Long = System.currentTimeMillis(), note: String = "", focusRating: Int = 0) = update { current ->
        val timer = current.timer ?: return@update current
        val duration = StudyMath.elapsed(timer, now)
        val session = StudySession(StudyMath.newId(), timer.subjectId, timer.startedAt, now.coerceAtLeast(timer.startedAt), duration, note.trim().take(2000), focusRating.coerceIn(0, 5), timer.taskId)
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

    fun setSettings(settings: StudySettings) = update { it.copy(settings = settings) }

    fun saveTask(task: StudyTask) {
        require(task.title.isNotBlank() && task.title.length <= 200)
        java.time.LocalDate.parse(task.dueDate)
        require(task.estimateMinutes in 5..720 && task.priority in 0..2)
        update { it.copy(tasks = it.tasks.filterNot { old -> old.id == task.id } + task.copy(title = task.title.trim())) }
    }
    fun toggleTask(id: String) = update { it.copy(tasks = it.tasks.map { task -> if (task.id == id) task.copy(completed = !task.completed) else task }) }
    fun deleteTask(id: String) = update { it.copy(tasks = it.tasks.filterNot { task -> task.id == id }) }
    fun addManualSession(subjectId: String, endedAt: Long, minutes: Int, note: String) {
        require(state.subjects.any { it.id == subjectId })
        require(minutes in 1..720 && endedAt <= System.currentTimeMillis() && endedAt >= minutes * 60_000L)
        val session = StudySession(StudyMath.newId(), subjectId, endedAt - minutes * 60_000L, endedAt, minutes * 60_000L, note.trim().take(2000))
        update { it.copy(sessions = (it.sessions + session).sortedByDescending { session -> session.endedAt }) }
    }

    fun exportBackup(): String = StudyBackupCodec.export(state)

    fun importBackup(raw: String) {
        require(state.timer == null) { "Finish or discard the active session before importing." }
        val imported = StudyBackupCodec.import(raw)
        persist(imported)
        state = imported
    }

    private fun persist(next: StudyState) {
        check(preferences.edit().putString("data", StudyBackupCodec.encode(next).toString()).commit()) {
            "Could not save study data on this device."
        }
    }

    private fun load(): StudyState {
        val raw = preferences.getString("data", null) ?: return StudyState()
        return runCatching { StudyBackupCodec.decode(org.json.JSONObject(raw)) }.getOrElse { StudyState() }
    }

}
