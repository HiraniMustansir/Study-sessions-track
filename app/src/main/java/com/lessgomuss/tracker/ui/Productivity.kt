package com.lessgomuss.tracker.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lessgomuss.tracker.data.*
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun ChoiceRow(items: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, label -> FilterChip(selected == index, { onSelect(index) }, label = { Text(label) }) }
    }
}

@Composable
fun DateButton(date: String, onDate: (String) -> Unit) {
    val context = LocalContext.current
    TextButton(onClick = {
        val day = LocalDate.parse(date)
        DatePickerDialog(context, { _, year, month, number -> onDate(LocalDate.of(year, month + 1, number).toString()) }, day.year, day.monthValue - 1, day.dayOfMonth).show()
    }) { Icon(Icons.Outlined.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text(LocalDate.parse(date).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))) }
}

@Composable
fun PlanScreen(state: StudyState, repository: StudyRepository, onFocus: () -> Unit, onSubjects: () -> Unit) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<StudyTask?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<StudyTask?>(null) }
    val today = LocalDate.now().toString()
    val pending = state.tasks.filter { !it.completed && it.dueDate <= today }
    val tasks = state.tasks.filter { when (filter) { 0 -> !it.completed && it.dueDate <= today; 1 -> !it.completed; else -> it.completed } }
        .sortedWith(compareBy<StudyTask> { it.dueDate }.thenByDescending { it.priority })
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        ScreenTitle("ONE STEP AT A TIME", "Your study plan", "Turn intentions into a clear next step.")
        Spacer(Modifier.height(20.dp))
        WhiteCard(Modifier.fillMaxWidth()) {
            Text("${pending.size} tasks to focus on", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("${pending.sumOf { it.estimateMinutes }} min planned · ${pending.count { it.dueDate < today }} overdue", color = AppColors.Muted)
            Spacer(Modifier.height(8.dp))
            Text("Estimates help you plan. Only saved sessions count as study time.", fontSize = 12.sp, color = AppColors.Muted)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { adding = true }, enabled = state.subjects.isNotEmpty()) { Icon(Icons.Outlined.Add, null); Text("New task") }
            TextButton(onClick = onSubjects) { Text("Subjects") }
        }
        if (state.subjects.isEmpty()) Text("Add a subject before creating a task.", color = AppColors.Muted)
        ChoiceRow(listOf("Today", "All open", "Completed"), filter) { filter = it }
        Spacer(Modifier.height(12.dp))
        if (tasks.isEmpty()) EmptyCard(if (filter == 0) "Room to focus" else "No tasks here", "Add a small, specific task such as ‘Review chapter 3’.")
        tasks.forEach { task ->
            WhiteCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(task.completed, { repository.toggleTask(task.id) })
                    Column(Modifier.weight(1f).clickable { editing = task }) {
                        Text(task.title, fontWeight = FontWeight.SemiBold, color = AppColors.Ink)
                        Text("${state.subjects.find { it.id == task.subjectId }?.name ?: "Archived subject"} · ${task.estimateMinutes} min", color = AppColors.Muted, fontSize = 12.sp)
                        Text("${listOf("Low", "Normal", "High")[task.priority]} priority · ${task.dueDate}", color = if (!task.completed && task.dueDate < today) MaterialTheme.colorScheme.error else AppColors.Muted, fontSize = 11.sp)
                    }
                    IconButton(onClick = { editing = task }) { Icon(Icons.Outlined.Edit, "Edit task") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { deleting = task }) { Text("Delete") }
                    if (!task.completed) TextButton(onClick = { repository.start(task.subjectId, targetMinutes = state.settings.focusMinutes, taskId = task.id); onFocus() }, enabled = state.timer == null && state.subjects.any { it.id == task.subjectId }) {
                        Icon(Icons.Outlined.PlayArrow, null); Text("Focus")
                    }
                }
                val time = StudyMath.totalMs(state.sessions.filter { it.taskId == task.id })
                if (time > 0) Text("${StudyMath.formatDuration(time)} invested", color = AppColors.Green, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
    if (adding || editing != null) TaskDialog(state, editing, onDismiss = { adding = false; editing = null }, onSave = { repository.saveTask(it); adding = false; editing = null })
    deleting?.let { task -> ConfirmDialog("Delete task?", "Linked study sessions will stay in your history.", "Delete", { deleting = null }) { repository.deleteTask(task.id); deleting = null } }
}

@Composable
private fun TaskDialog(state: StudyState, task: StudyTask?, onDismiss: () -> Unit, onSave: (StudyTask) -> Unit) {
    var title by rememberSaveable { mutableStateOf(task?.title ?: "") }
    var subject by rememberSaveable { mutableStateOf(task?.subjectId ?: state.subjects.first().id) }
    var date by rememberSaveable { mutableStateOf(task?.dueDate ?: LocalDate.now().toString()) }
    var minutes by rememberSaveable { mutableStateOf((task?.estimateMinutes ?: 25).toString()) }
    var priority by rememberSaveable { mutableIntStateOf(task?.priority ?: 1) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (task == null) "New study task" else "Edit task") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(title, { title = it.take(200) }, label = { Text("What will you work on?") }, modifier = Modifier.fillMaxWidth())
            ChoiceRow(state.subjects.map { it.name }, state.subjects.indexOfFirst { it.id == subject }) { subject = state.subjects[it].id }
            Text("Due date", fontWeight = FontWeight.SemiBold); DateButton(date) { date = it }
            OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(3) }, label = { Text("Estimated minutes (5–720)") }, singleLine = true)
            ChoiceRow(listOf("Low", "Normal", "High"), priority) { priority = it }
        }
    }, confirmButton = { TextButton(enabled = title.isNotBlank() && minutes.toIntOrNull() in 5..720 && state.subjects.any { it.id == subject }, onClick = {
        onSave(StudyTask(task?.id ?: StudyMath.newId(), title.trim(), subject, date, minutes.toInt(), priority, task?.completed ?: false))
    }) { Text("Save task") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun SaveSessionDialog(onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var note by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableIntStateOf(0) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Wrap up your session") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(note, { note = it.take(2000) }, label = { Text("What did you learn? (optional)") }, minLines = 3)
            Spacer(Modifier.height(12.dp)); Text("How focused did you feel? (optional)")
            ChoiceRow(listOf("Skip", "1", "2", "3", "4", "5"), rating) { rating = it }
            Text("1 = distracted · 5 = deeply focused", color = AppColors.Muted, fontSize = 12.sp)
            Text("Timer is paused while you reflect.", color = AppColors.Muted, fontSize = 12.sp)
        }
    }, confirmButton = { TextButton(onClick = { onSave(note, rating) }) { Text("Save session") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Back to timer") } })
}

@Composable
fun ManualSessionDialog(state: StudyState, repository: StudyRepository, onDismiss: () -> Unit) {
    var subject by rememberSaveable { mutableIntStateOf(0) }
    var minutes by rememberSaveable { mutableStateOf("25") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var note by rememberSaveable { mutableStateOf("") }
    val validDate = !LocalDate.parse(date).isAfter(LocalDate.now())
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Log study time") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text("Record a session you studied away from the timer.", color = AppColors.Muted)
            ChoiceRow(state.subjects.map { it.name }, subject) { subject = it }
            DateButton(date) { date = it }
            if (!validDate) Text("Choose today or an earlier date.", color = MaterialTheme.colorScheme.error)
            OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(3) }, label = { Text("Study minutes (1–720)") }, singleLine = true)
            OutlinedTextField(note, { note = it.take(2000) }, label = { Text("Notes (optional)") })
            Text("Saved on the selected date; the end time uses the current time of day.", fontSize = 12.sp, color = AppColors.Muted)
        }
    }, confirmButton = { TextButton(enabled = validDate && minutes.toIntOrNull() in 1..720 && state.subjects.isNotEmpty(), onClick = {
        val end = LocalDate.parse(date).atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli().coerceAtMost(System.currentTimeMillis())
        repository.addManualSession(state.subjects[subject].id, end, minutes.toInt(), note); onDismiss()
    }) { Text("Save time") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun RoutineSettings(state: StudyState, repository: StudyRepository, onNotificationToggle: (Boolean) -> Unit) {
    val settings = state.settings
    val context = LocalContext.current
    var editing by remember { mutableStateOf<StudyReminder?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var goalText by rememberSaveable(settings.dailyGoalMinutes) { mutableStateOf(settings.dailyGoalMinutes.toString()) }
    var weeklyText by rememberSaveable(settings.weeklyGoalMinutes) { mutableStateOf(settings.weeklyGoalMinutes.toString()) }
    WhiteCard(Modifier.fillMaxWidth()) {
        SectionTitle("Your targets")
        OutlinedTextField(goalText, { goalText = it.filter(Char::isDigit).take(3) }, label = { Text("Daily minutes (15–720)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(weeklyText, { weeklyText = it.filter(Char::isDigit).take(4) }, label = { Text("Weekly minutes (60–5040)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextButton(enabled = goalText.toIntOrNull() in 15..720 && weeklyText.toIntOrNull() in 60..5040 && (goalText != settings.dailyGoalMinutes.toString() || weeklyText != settings.weeklyGoalMinutes.toString()), onClick = {
            repository.setSettings(settings.copy(dailyGoalMinutes = goalText.toInt(), weeklyGoalMinutes = weeklyText.toInt()))
        }) { Text("Save targets") }
        Text("Default focus block", fontWeight = FontWeight.SemiBold)
        ChoiceRow(listOf("15 min", "25 min", "50 min", "90 min"), listOf(15, 25, 50, 90).indexOf(settings.focusMinutes)) { repository.setSettings(settings.copy(focusMinutes = listOf(15, 25, 50, 90)[it])) }
    }
    Spacer(Modifier.height(16.dp))
    WhiteCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { SectionTitle("Reminders"); Text("Routine + focus alerts", color = AppColors.Muted, fontSize = 12.sp) }
            Switch(settings.notificationsEnabled, onNotificationToggle)
        }
        if (settings.notificationsEnabled) {
            TextButton(onClick = { TimePickerDialog(context, { _, h, m -> repository.setSettings(settings.copy(reminderHour = h, reminderMinute = m)) }, settings.reminderHour, settings.reminderMinute, true).show() }) {
                Text("Daily summary · %02d:%02d".format(settings.reminderHour, settings.reminderMinute))
            }
            settings.reminders.forEach { reminder ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).clickable { editing = reminder }.padding(vertical = 10.dp)) {
                        Text("${reminderName(reminder.kind)} · %02d:%02d".format(reminder.hour, reminder.minute), fontWeight = FontWeight.SemiBold)
                        Text(reminder.weekdays.sorted().joinToString(" ") { DayOfWeek.of(it).getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()) }, fontSize = 11.sp, color = AppColors.Muted)
                        if (StudyInsights.isQuiet(reminder.hour, settings)) Text("Silenced by quiet hours", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                    Switch(reminder.enabled, { enabled -> repository.setSettings(settings.copy(reminders = settings.reminders.map { if (it.id == reminder.id) it.copy(enabled = enabled) else it })) })
                }
            }
            TextButton(enabled = settings.reminders.size < 20, onClick = { adding = true }) { Icon(Icons.Outlined.Add, null); Text("Add reminder") }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Quiet hours", Modifier.weight(1f)); Switch(settings.quietEnabled, { repository.setSettings(settings.copy(quietEnabled = it)) }) }
            if (settings.quietEnabled) {
                Row {
                    TextButton(onClick = { TimePickerDialog(context, { _, h, _ -> repository.setSettings(settings.copy(quietStartHour = h)) }, settings.quietStartHour, 0, true).show() }) { Text("From %02d:00".format(settings.quietStartHour)) }
                    TextButton(onClick = { TimePickerDialog(context, { _, h, _ -> repository.setSettings(settings.copy(quietEndHour = h)) }, settings.quietEndHour, 0, true).show() }) { Text("Until %02d:00".format(settings.quietEndHour)) }
                }
                Text("Quiet hours use whole hours. Matching start and end means no quiet period. All alerts in this window are silenced.", fontSize = 11.sp, color = AppColors.Muted)
            }
        }
        TextButton(onClick = { context.startActivity(android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)) }) { Text("Android notification settings") }
        Text("Android battery settings can delay alerts. Focus alerts never stop or save your timer automatically.", color = AppColors.Muted, fontSize = 12.sp)
    }
    if (adding || editing != null) ReminderDialog(editing, onDismiss = { adding = false; editing = null }, onDelete = {
        repository.setSettings(settings.copy(reminders = settings.reminders.filterNot { it.id == editing?.id })); editing = null
    }, onSave = { reminder -> repository.setSettings(settings.copy(reminders = settings.reminders.filterNot { it.id == reminder.id } + reminder)); editing = null; adding = false })
}

private fun reminderName(kind: String) = when (kind) { "goal" -> "Goal check-in"; "plan" -> "Tasks due"; else -> "Start studying" }

@Composable
private fun ReminderDialog(reminder: StudyReminder?, onDismiss: () -> Unit, onDelete: () -> Unit, onSave: (StudyReminder) -> Unit) {
    val context = LocalContext.current
    var kind by rememberSaveable { mutableStateOf(reminder?.kind ?: "start") }
    var hour by rememberSaveable { mutableIntStateOf(reminder?.hour ?: 9) }
    var minute by rememberSaveable { mutableIntStateOf(reminder?.minute ?: 0) }
    var days by remember { mutableStateOf(reminder?.weekdays ?: (1..7).toSet()) }
    val kinds = listOf("start", "goal", "plan")
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Study reminder") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            ChoiceRow(kinds.map(::reminderName), kinds.indexOf(kind)) { kind = kinds[it] }
            Text(when (kind) { "goal" -> "Only alerts when your saved study time is below your daily goal and no timer is active."; "plan" -> "Only alerts when unfinished tasks are due or overdue."; else -> "A gentle invitation to focus; skipped if a timer is active." }, color = AppColors.Muted, fontSize = 12.sp)
            TextButton(onClick = { TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, true).show() }) { Text("Send at %02d:%02d".format(hour, minute)) }
            (1..7).forEach { day -> Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(day in days, { days = if (day in days) days - day else days + day })
                Text(DayOfWeek.of(day).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()))
            } }
            if (reminder != null) TextButton(onClick = onDelete) { Text("Delete reminder", color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(enabled = days.isNotEmpty(), onClick = { onSave(StudyReminder(reminder?.id ?: StudyMath.newId(), kind, hour, minute, days, reminder?.enabled ?: true)) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun DeeperInsights(state: StudyState) {
    val today = LocalDate.now()
    val monday = today.with(java.time.DayOfWeek.MONDAY)
    val current = state.sessions.filter { StudyMath.dayOf(it) in monday..today }
    val previous = state.sessions.filter { StudyMath.dayOf(it) in monday.minusWeeks(1)..today.minusWeeks(1) }
    val total = StudyMath.totalMs(current)
    val old = StudyMath.totalMs(previous)
    val rated = current.filter { it.focusRating > 0 }
    val hours = state.sessions.groupBy { Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).hour }
    val bestHour = hours.maxByOrNull { StudyMath.totalMs(it.value) }?.key
    WhiteCard(Modifier.fillMaxWidth()) {
        SectionTitle("Weekly direction")
        Spacer(Modifier.height(12.dp))
        Text("${StudyMath.formatDuration(total)} / ${StudyMath.formatDuration(state.settings.weeklyGoalMinutes * 60_000L)}", fontWeight = FontWeight.Bold)
        ThinProgress((total.toFloat() / (state.settings.weeklyGoalMinutes * 60_000L)).coerceIn(0f, 1f))
        Spacer(Modifier.height(12.dp))
        Text(if (old == 0L) "No study time in the matching days last week." else "${if (total >= old) "+" else ""}${((total - old) * 100.0 / old).toInt()}% vs matching days last week", color = AppColors.Muted, fontSize = 12.sp)
        Text("${StudyInsights.goalDays(state, monday, today)} daily goals reached this week", color = AppColors.Muted, fontSize = 12.sp)
        Text("${StudyInsights.longestStreak(state)} days · personal best streak", color = AppColors.Muted, fontSize = 12.sp)
        if (rated.isNotEmpty()) Text("%.1f / 5 average focus · %d rated sessions this week".format(rated.map { it.focusRating }.average(), rated.size), color = AppColors.Muted, fontSize = 12.sp)
        if (bestHour != null) Text("Most study time starts at %02d:00 · all saved sessions".format(bestHour), color = AppColors.Muted, fontSize = 12.sp)
    }
    Spacer(Modifier.height(16.dp))
}
