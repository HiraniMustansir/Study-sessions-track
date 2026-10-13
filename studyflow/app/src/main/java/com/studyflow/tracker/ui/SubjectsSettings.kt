package com.studyflow.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.tracker.data.StudyMath
import com.studyflow.tracker.data.StudyRepository
import com.studyflow.tracker.data.StudyState

private val palette = listOf(0xFFF3A57DL, 0xFF82B8ABL, 0xFF9A9BCBL, 0xFFE5B46CL, 0xFF80A9C9L, 0xFFCF94A4L)
private val subjectIcons = listOf("book", "calculate", "science", "code", "language", "palette")

@Composable
fun SubjectsScreen(state: StudyState, repository: StudyRepository) {
    var adding by remember { mutableStateOf(false) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 24.dp)) {
        ScreenTitle("STAY ORGANIZED", "Your subjects", "Track where your time goes.")
        Spacer(Modifier.height(25.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${state.subjects.size} ${if (state.subjects.size == 1) "subject" else "subjects"}", Modifier.weight(1f), color = AppColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Button(onClick = { adding = true }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Orange), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Outlined.Add, null); Text("Add subject", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
        }
        Spacer(Modifier.height(15.dp))
        if (state.subjects.isEmpty()) EmptyCard("Make your first subject", "Create a subject to keep each study session organized.")
        else state.subjects.forEach { subject ->
            WhiteCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectAvatar(subject, 51.dp)
                    Column(Modifier.weight(1f).padding(start = 13.dp)) {
                        Text(subject.name, color = AppColors.Ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        val sessions = state.sessions.filter { it.subjectId == subject.id }
                        Text("${StudyMath.formatDuration(StudyMath.totalMs(sessions))} total · ${sessions.size} ${if (sessions.size == 1) "session" else "sessions"}", color = AppColors.Muted, fontSize = 11.sp)
                    }
                    IconButton(onClick = { if (state.timer?.subjectId == subject.id) error = "Finish or discard the active session first." else deleteId = subject.id }) { Icon(Icons.Outlined.DeleteOutline, "Delete subject", tint = AppColors.Muted, modifier = Modifier.size(20.dp)) }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().background(Color(0xFFFFF0E8), RoundedCornerShape(16.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, null, tint = AppColors.Orange, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(10.dp))
            Text("Use subjects for courses, topics, or different kinds of self-study.", color = Color(0xFF9A765F), fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
    if (adding) AddSubjectDialog(onDismiss = { adding = false }, onSave = { name, color, icon ->
        if (repository.addSubject(name, color, icon)) adding = false else error = "Enter a unique subject name."
    })
    if (deleteId != null) ConfirmDialog("Delete subject?", "Past sessions will stay in your totals, but their subject name will no longer appear.", "Delete", onDismiss = { deleteId = null }, onConfirm = { deleteId?.let(repository::deleteSubject); deleteId = null })
    if (error != null) AlertDialog(onDismissRequest = { error = null }, title = { Text("Could not continue") }, text = { Text(error ?: "") }, confirmButton = { TextButton(onClick = { error = null }) { Text("OK") } })
}

@Composable
private fun AddSubjectDialog(onDismiss: () -> Unit, onSave: (String, Long, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf(palette.first()) }
    var icon by rememberSaveable { mutableStateOf(subjectIcons.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New subject", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                OutlinedTextField(name, { if (it.length <= 28) name = it }, label = { Text("Subject name") }, placeholder = { Text("e.g. Physics") }, singleLine = true)
                Spacer(Modifier.height(18.dp)); Text("COLOR", color = AppColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(9.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { item -> Box(Modifier.size(34.dp).background(Color(item), RoundedCornerShape(11.dp)).clickable { color = item }, contentAlignment = Alignment.Center) { if (color == item) Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold) } }
                }
                Spacer(Modifier.height(18.dp)); Text("ICON", color = AppColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(9.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    subjectIcons.forEach { item -> Box(Modifier.size(38.dp).background(if (icon == item) Color(0xFFFFF0E8) else AppColors.Background, RoundedCornerShape(10.dp)).clickable { icon = item }, contentAlignment = Alignment.Center) { Icon(subjectIcon(item), item, tint = if (icon == item) AppColors.Orange else AppColors.Muted, modifier = Modifier.size(21.dp)) } }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, color, icon) }) { Text("Create", color = AppColors.Orange) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun SettingsScreen(state: StudyState, repository: StudyRepository, onNotificationToggle: (Boolean) -> Unit, onReminderHour: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 24.dp)) {
        ScreenTitle("MAKE IT YOURS", "Settings", "Shape a study routine that fits you.")
        Spacer(Modifier.height(27.dp))
        Text("DAILY TARGET", color = AppColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        WhiteCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(Color(0xFFFFF0E8), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Flag, null, tint = AppColors.Orange) }
                Column(Modifier.padding(start = 12.dp)) { Text("Daily study goal", color = AppColors.Ink, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold); Text("Currently ${StudyMath.formatDuration(state.settings.dailyGoalMinutes * 60_000L)} per day", color = AppColors.Muted, fontSize = 11.sp) }
            }
            Spacer(Modifier.height(19.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(60, 120, 180, 240, 300).forEach { minutes ->
                    Box(Modifier.weight(1f).background(if (state.settings.dailyGoalMinutes == minutes) AppColors.Orange else AppColors.Background, RoundedCornerShape(10.dp)).clickable { repository.setGoal(minutes) }.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
                        Text("${minutes / 60}h", color = if (state.settings.dailyGoalMinutes == minutes) Color.White else AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
        Spacer(Modifier.height(27.dp))
        Text("NOTIFICATIONS", color = AppColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        WhiteCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(Color(0xFFE9F2EE), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Notifications, null, tint = AppColors.Green) }
                Column(Modifier.weight(1f).padding(start = 12.dp)) { Text("Daily summary", color = AppColors.Ink, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold); Text("Your time and sessions at the end of the day", color = AppColors.Muted, fontSize = 11.sp, lineHeight = 16.sp) }
                Switch(checked = state.settings.notificationsEnabled, onCheckedChange = onNotificationToggle)
            }
            if (state.settings.notificationsEnabled) {
                Spacer(Modifier.height(17.dp)); Text("SEND AT", color = AppColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(11.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(19, 20, 21, 22).forEach { hour ->
                        Box(Modifier.weight(1f).background(if (state.settings.reminderHour == hour) Color(0xFFFFF0E8) else AppColors.Background, RoundedCornerShape(9.dp)).clickable { onReminderHour(hour) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text("${hour - 12}:00 PM", color = if (state.settings.reminderHour == hour) AppColors.Orange else AppColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(17.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.Info, null, tint = AppColors.Muted, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
            Text("Your sessions are saved on this device. Daily reminders may arrive later than the chosen time due to Android battery settings.", color = AppColors.Muted, fontSize = 11.sp, lineHeight = 17.sp)
        }
        Spacer(Modifier.height(56.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✦", color = AppColors.Orange, fontSize = 30.sp)
            Text("Studyflow", color = AppColors.Ink, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text("Version 1.0", color = AppColors.Muted, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp)); Text("Small steps, steady progress.", color = AppColors.Muted, fontSize = 11.sp)
        }
    }
}
