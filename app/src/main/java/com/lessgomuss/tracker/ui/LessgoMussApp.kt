package com.lessgomuss.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lessgomuss.tracker.data.StudyMath
import com.lessgomuss.tracker.data.StudyRepository
import com.lessgomuss.tracker.data.StudyState
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class Tab(val label: String, val icon: ImageVector)
private val tabs = listOf(
    Tab("Today", Icons.Outlined.Home), Tab("Calendar", Icons.Outlined.CalendarMonth),
    Tab("Insights", Icons.Outlined.BarChart), Tab("Subjects", Icons.Outlined.GridView),
    Tab("Settings", Icons.Outlined.Settings),
)

@Composable
fun LessgoMussApp(
    repository: StudyRepository,
    onNotificationToggle: (Boolean) -> Unit,
    onReminderHour: (Int) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    transferMessage: String?,
    onDismissTransferMessage: () -> Unit,
) {
    val state = repository.state
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1_000); now = System.currentTimeMillis() } }
    MaterialTheme(colorScheme = lightColorScheme(primary = AppColors.Orange, background = AppColors.Background, surface = Color.White, onSurface = AppColors.Ink)) {
        Column(Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding().navigationBarsPadding()) {
            Box(Modifier.weight(1f)) {
                when (tab) {
                    0 -> TodayScreen(state, now, repository, onSubjects = { tab = 3 }, onCalendar = { tab = 1 })
                    1 -> CalendarScreen(state)
                    2 -> InsightsScreen(state)
                    3 -> SubjectsScreen(state, repository)
                    else -> SettingsScreen(state, repository, onNotificationToggle, onReminderHour, onExport, onImport)
                }
            }
            Row(Modifier.fillMaxWidth().background(Color.White).padding(top = 8.dp, bottom = 4.dp, start = 5.dp, end = 5.dp)) {
                tabs.forEachIndexed { index, item ->
                    Column(Modifier.weight(1f).clickable { tab = index }.padding(vertical = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(width = 39.dp, height = 31.dp).background(if (tab == index) Color(0xFFFFF0EA) else Color.Transparent, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = item.label, tint = if (tab == index) AppColors.Orange else Color(0xFF9BA2A9), modifier = Modifier.size(21.dp))
                        }
                        Text(item.label, color = if (tab == index) AppColors.Orange else Color(0xFF9BA2A9), fontSize = 10.sp, fontWeight = if (tab == index) FontWeight.ExtraBold else FontWeight.Medium)
                    }
                }
            }
        }
        if (transferMessage != null) AlertDialog(
            onDismissRequest = onDismissTransferMessage,
            title = { Text("Study data") },
            text = { Text(transferMessage) },
            confirmButton = { TextButton(onClick = onDismissTransferMessage) { Text("OK") } },
        )
    }
}

@Composable
private fun TodayScreen(state: StudyState, now: Long, repository: StudyRepository, onSubjects: () -> Unit, onCalendar: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf("mathematics") }
    val selectedSubject = state.subjects.find { it.id == selected } ?: state.subjects.firstOrNull()
    val active = state.timer
    val elapsed = active?.let { StudyMath.elapsed(it, now) } ?: 0
    val today = LocalDate.now()
    val sessions = StudyMath.sessionsOn(state, today)
    val todayMs = StudyMath.totalMs(sessions) + elapsed
    val goalMs = state.settings.dailyGoalMinutes * 60_000L
    val progress = (todayMs.toFloat() / goalMs).coerceIn(0f, 1f)
    var confirmDiscard by remember { mutableStateOf(false) }
    var deleteSessionId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 24.dp)) {
        ScreenTitle("YOUR STUDY SPACE", "Make today count.", today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")))
        Spacer(Modifier.height(23.dp))
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF253545), Color(0xFF1D2936))), RoundedCornerShape(27.dp)).padding(23.dp)) {
            Text(if (active == null) "●  READY WHEN YOU ARE" else if (active.resumedAt == null) "●  SESSION PAUSED" else "●  FOCUS SESSION IN PROGRESS", color = Color(0xFFB8C3CD), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
            Spacer(Modifier.height(33.dp))
            Text(StudyMath.formatClock(elapsed), Modifier.fillMaxWidth(), color = Color.White, fontSize = 45.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, letterSpacing = 1.sp)
            Text(if (active == null) "Settle in, choose a subject, and begin." else "Studying ${state.subjects.find { it.id == active.subjectId }?.name ?: "a subject"}", Modifier.fillMaxWidth(), color = Color(0xFFB8C3CD), fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(29.dp))
            if (active == null) {
                Button(onClick = { selectedSubject?.let { repository.start(it.id) } }, enabled = selectedSubject != null, modifier = Modifier.fillMaxWidth().height(53.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5AE89), contentColor = AppColors.Ink), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Outlined.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Start studying", fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { if (active.resumedAt == null) repository.resume() else repository.pause() }, modifier = Modifier.weight(1f).height(53.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF435364)), shape = RoundedCornerShape(16.dp)) {
                        Icon(if (active.resumedAt == null) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, null); Text(if (active.resumedAt == null) "Resume" else "Pause")
                    }
                    Button(onClick = repository::finish, modifier = Modifier.weight(1.35f).height(53.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5AE89), contentColor = AppColors.Ink), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Outlined.Check, null); Text("Finish", fontWeight = FontWeight.ExtraBold)
                    }
                }
                TextButton(onClick = { confirmDiscard = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Discard session", color = Color(0xFFBAC4CE), fontSize = 11.sp) }
            }
        }

        if (active == null) {
            Spacer(Modifier.height(26.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { SectionTitle("What are you studying?", Modifier.weight(1f)); Text("Manage", Modifier.clickable(onClick = onSubjects), color = AppColors.Orange, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(13.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.subjects.forEach { subject ->
                    val picked = subject.id == selectedSubject?.id
                    Row(Modifier.background(if (picked) Color(0xFFFFF1EA) else Color.White, RoundedCornerShape(12.dp)).clickable { selected = subject.id }.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).background(Color(subject.color), RoundedCornerShape(5.dp)))
                        Spacer(Modifier.width(7.dp))
                        Text(subject.name, color = if (picked) AppColors.Ink else AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (state.subjects.isEmpty()) Text("Add a subject to begin.", color = AppColors.Muted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(29.dp)); SectionTitle("Today's progress"); Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.Outlined.HourglassEmpty, "Study time", StudyMath.formatDuration(todayMs), Color(0xFFFDE9E2), Modifier.weight(1f))
            StatTile(Icons.Outlined.Layers, "Sessions", "${sessions.size + if (active != null) 1 else 0}", Color(0xFFE7F3EF), Modifier.weight(1f))
        }
        Spacer(Modifier.height(11.dp))
        WhiteCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Flag, null, tint = AppColors.Orange)
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("Daily goal", color = AppColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${StudyMath.formatDuration(todayMs)} of ${StudyMath.formatDuration(goalMs)}", color = AppColors.Muted, fontSize = 11.sp)
                }
                Text("${(progress * 100).toInt()}%", color = AppColors.Orange, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Spacer(Modifier.height(14.dp)); ThinProgress(progress)
        }
        Spacer(Modifier.height(11.dp))
        Row(Modifier.fillMaxWidth().background(Color(0xFFFFF2E7), RoundedCornerShape(16.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🔥", fontSize = 17.sp); Spacer(Modifier.width(8.dp))
            Text("${StudyMath.streak(state)} day streak  ·  Keep showing up for yourself", color = Color(0xFF7D5D49), fontSize = 12.sp)
        }
        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { SectionTitle("Recent sessions", Modifier.weight(1f)); Text("View calendar", Modifier.clickable(onClick = onCalendar), color = AppColors.Orange, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(14.dp))
        if (state.sessions.isEmpty()) EmptyCard("No sessions yet", "Start your first study session and your progress will appear here.")
        else state.sessions.take(4).forEach { session ->
            SessionItem(session, state.subjects.find { it.id == session.subjectId }, trailing = {
                IconButton(onClick = { deleteSessionId = session.id }, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.DeleteOutline, "Delete session", tint = AppColors.Muted, modifier = Modifier.size(18.dp)) }
            })
            Spacer(Modifier.height(9.dp))
        }
    }

    if (confirmDiscard) ConfirmDialog("Discard this session?", "Your current timer will be cleared without saving.", "Discard", onDismiss = { confirmDiscard = false }, onConfirm = { repository.discard(); confirmDiscard = false })
    if (deleteSessionId != null) ConfirmDialog("Delete session?", "This will remove it from your progress.", "Delete", onDismiss = { deleteSessionId = null }, onConfirm = { deleteSessionId?.let(repository::deleteSession); deleteSessionId = null })
}

@Composable
fun ConfirmDialog(title: String, message: String, confirmText: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(message) }, confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText, color = AppColors.Orange) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
