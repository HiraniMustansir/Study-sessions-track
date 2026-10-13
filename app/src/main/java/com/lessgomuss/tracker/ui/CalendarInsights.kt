package com.lessgomuss.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lessgomuss.tracker.data.StudyMath
import com.lessgomuss.tracker.data.StudySession
import com.lessgomuss.tracker.data.StudyState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(state: StudyState) {
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    var selectedKey by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val month = YearMonth.now().plusMonths(monthOffset.toLong())
    val selected = LocalDate.parse(selectedKey)
    val monthSessions = state.sessions.filter { YearMonth.from(StudyMath.dayOf(it)) == month }
    val daysStudied = monthSessions.map(StudyMath::dayOf).distinct().size
    val selectedSessions = StudyMath.sessionsOn(state, selected)
    val offset = month.atDay(1).dayOfWeek.value % 7
    val days = List(offset) { 0 } + (1..month.lengthOfMonth()).toList()
    val padded = days + List((7 - days.size % 7) % 7) { 0 }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 24.dp)) {
        ScreenTitle("YOUR CONSISTENCY", "Study calendar", "A little progress, every day.")
        Spacer(Modifier.height(21.dp))
        Row(Modifier.fillMaxWidth().background(AppColors.Dark, RoundedCornerShape(24.dp)).padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Column { Text(StudyMath.formatDuration(StudyMath.totalMs(monthSessions)), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black); Text("this month", color = Color(0xFFBAC5CE), fontSize = 11.sp) }
            Spacer(Modifier.width(31.dp))
            Column { Text("$daysStudied", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black); Text("days studied", color = Color(0xFFBAC5CE), fontSize = 11.sp) }
        }
        Spacer(Modifier.height(18.dp))
        WhiteCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { monthOffset--; selectedKey = month.minusMonths(1).atDay(1).toString() }) { Icon(Icons.Outlined.ChevronLeft, "Previous month") }
                Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), Modifier.weight(1f), textAlign = TextAlign.Center, color = AppColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = { monthOffset++; selectedKey = month.plusMonths(1).atDay(1).toString() }) { Icon(Icons.Outlined.ChevronRight, "Next month") }
            }
            Spacer(Modifier.height(11.dp))
            Row(Modifier.fillMaxWidth()) { listOf("S", "M", "T", "W", "T", "F", "S").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, color = AppColors.Muted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold) } }
            Spacer(Modifier.height(10.dp))
            padded.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { number ->
                        Box(Modifier.weight(1f).height(46.dp), contentAlignment = Alignment.Center) {
                            if (number > 0) {
                                val day = month.atDay(number)
                                val ms = StudyMath.totalMs(StudyMath.sessionsOn(state, day))
                                val isSelected = day == selected
                                val heat = when {
                                    isSelected -> AppColors.Dark
                                    ms >= 3 * 3_600_000 -> Color(0xFFF2A580)
                                    ms >= 3_600_000 -> Color(0xFFF8D4BE)
                                    ms > 0 -> Color(0xFFFBE9DD)
                                    else -> Color.Transparent
                                }
                                Box(Modifier.size(35.dp).background(heat, RoundedCornerShape(12.dp)).clickable { selectedKey = day.toString() }, contentAlignment = Alignment.Center) {
                                    Text("$number", color = if (isSelected) Color.White else if (day == LocalDate.now()) AppColors.Orange else AppColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(13.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text("Less", color = AppColors.Muted, fontSize = 10.sp)
                listOf(0xFFFBE9DD, 0xFFF8D4BE, 0xFFF2A580).forEach { value -> Spacer(Modifier.width(5.dp)); Box(Modifier.size(12.dp).background(Color(value), RoundedCornerShape(4.dp))) }
                Spacer(Modifier.width(5.dp)); Text("More", color = AppColors.Muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(27.dp))
        SectionTitle(selected.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")))
        Spacer(Modifier.height(6.dp))
        Text("${StudyMath.formatDuration(StudyMath.totalMs(selectedSessions))} studied  ·  ${selectedSessions.size} ${if (selectedSessions.size == 1) "session" else "sessions"}", color = AppColors.Muted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        if (selectedSessions.isEmpty()) EmptyCard("A fresh page", "No study sessions were recorded on this day.")
        else selectedSessions.forEach { session ->
            SessionItem(session, state.subjects.find { it.id == session.subjectId }); Spacer(Modifier.height(9.dp))
        }
    }
}

@Composable
fun InsightsScreen(state: StudyState) {
    var monthView by rememberSaveable { mutableStateOf(false) }
    val today = LocalDate.now()
    val start = if (monthView) today.withDayOfMonth(1) else today.with(DayOfWeek.MONDAY)
    val sessions = state.sessions.filter { val day = StudyMath.dayOf(it); !day.isBefore(start) && !day.isAfter(today) }
    val total = StudyMath.totalMs(sessions)
    val activeDays = sessions.map(StudyMath::dayOf).distinct().size
    val longest = sessions.maxOfOrNull { it.durationMs } ?: 0
    val bars: List<Pair<String, List<StudySession>>> = if (!monthView) {
        (0..6).map { i ->
            val day = start.plusDays(i.toLong())
            day.format(DateTimeFormatter.ofPattern("EE")) to sessions.filter { StudyMath.dayOf(it) == day }
        }
    } else {
        (0 until (YearMonth.from(today).lengthOfMonth() + 6) / 7).map { i ->
            val from = start.plusDays((i * 7).toLong())
            val to = from.plusDays(6)
            "W${i + 1}" to sessions.filter { val day = StudyMath.dayOf(it); !day.isBefore(from) && !day.isAfter(to) }
        }
    }
    val maxBar = bars.maxOfOrNull { StudyMath.totalMs(it.second) }?.coerceAtLeast(60 * 60_000) ?: 60 * 60_000L
    val subjectTotals = state.subjects.map { subject -> subject to StudyMath.totalMs(sessions.filter { it.subjectId == subject.id }) }.filter { it.second > 0 }.sortedByDescending { it.second }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 24.dp)) {
        ScreenTitle("THE BIG PICTURE", "Your insights", "See how your focus adds up.")
        Spacer(Modifier.height(21.dp))
        Row(Modifier.fillMaxWidth().background(Color(0xFFEDEFEA), RoundedCornerShape(15.dp)).padding(4.dp)) {
            listOf(false to "This week", true to "This month").forEach { (value, label) ->
                Box(Modifier.weight(1f).background(if (monthView == value) Color.White else Color.Transparent, RoundedCornerShape(11.dp)).clickable { monthView = value }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text(label, color = if (monthView == value) AppColors.Ink else AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(17.dp))
        Column(Modifier.fillMaxWidth().background(AppColors.Dark, RoundedCornerShape(24.dp)).padding(22.dp)) {
            Icon(Icons.Outlined.QueryStats, null, tint = Color(0xFFF1AD8C), modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(19.dp))
            Text("TOTAL FOCUS TIME", color = Color(0xFFB2BEC7), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.7.sp)
            Text("%.1fh".format(total / 3_600_000.0), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
            Text("Across ${sessions.size} ${if (sessions.size == 1) "session" else "sessions"} · $activeDays active ${if (activeDays == 1) "day" else "days"}", color = Color(0xFFB2BEC7), fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.Outlined.EmojiEvents, "Longest session", StudyMath.formatDuration(longest), Color(0xFFFFF0E8), Modifier.weight(1f))
            StatTile(Icons.Outlined.QueryStats, "Active-day average", StudyMath.formatDuration(if (activeDays > 0) total / activeDays else 0), Color(0xFFE8F2ED), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        WhiteCard(Modifier.fillMaxWidth()) {
            SectionTitle(if (monthView) "Weekly activity" else "Daily activity")
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth().height(178.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.Bottom) {
                bars.forEach { (label, group) ->
                    val ms = StudyMath.totalMs(group)
                    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (ms > 0) "%.1fh".format(ms / 3_600_000.0) else "", color = AppColors.Muted, fontSize = 10.sp)
                        Spacer(Modifier.height(7.dp))
                        Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFFF6F5F1), RoundedCornerShape(8.dp)), contentAlignment = Alignment.BottomCenter) {
                            if (ms > 0) Box(Modifier.fillMaxWidth().fillMaxHeight((ms.toFloat() / maxBar).coerceIn(0.06f, 1f)).background(if (label == (if (monthView) "W${(today.dayOfMonth - 1) / 7 + 1}" else today.format(DateTimeFormatter.ofPattern("EE")))) AppColors.Orange else Color(0xFFEAB69D), RoundedCornerShape(8.dp)))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(label, color = AppColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(27.dp)); SectionTitle("Time by subject"); Spacer(Modifier.height(14.dp))
        if (subjectTotals.isEmpty()) EmptyCard("Insights are on their way", "Finish a study session to see your patterns here.")
        else subjectTotals.forEach { (subject, ms) ->
            WhiteCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectAvatar(subject, 38.dp)
                    Column(Modifier.weight(1f).padding(start = 11.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(subject.name, Modifier.weight(1f), color = AppColors.Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(StudyMath.formatDuration(ms), color = AppColors.Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(9.dp)); ThinProgress(ms.toFloat() / total, Color(subject.color))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().background(Color(0xFFFFF0E6), RoundedCornerShape(18.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🔥", fontSize = 23.sp); Spacer(Modifier.width(12.dp))
            Column { Text("${StudyMath.streak(state)} day current streak", color = AppColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold); Text("Every session is a step forward.", color = Color(0xFF9B7D68), fontSize = 11.sp) }
        }
    }
}
