package com.lessgomuss.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lessgomuss.tracker.data.StudyMath
import com.lessgomuss.tracker.data.StudySession
import com.lessgomuss.tracker.data.Subject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AppColors {
    val Background = Color(0xFFF7F7F3)
    val Ink = Color(0xFF202A36)
    val Muted = Color(0xFF818A94)
    val Line = Color(0xFFE6E9E7)
    val Orange = Color(0xFFF07354)
    val Dark = Color(0xFF202B38)
    val Green = Color(0xFF50A888)
}

fun subjectIcon(key: String): ImageVector = when (key) {
    "calculate" -> Icons.Outlined.Calculate
    "science" -> Icons.Outlined.Science
    "code" -> Icons.Outlined.Code
    "language" -> Icons.Outlined.Language
    "palette" -> Icons.Outlined.Palette
    else -> Icons.Outlined.Book
}

@Composable
fun ScreenTitle(eyebrow: String, title: String, subtitle: String) {
    Text(eyebrow, color = AppColors.Orange, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
    Spacer(Modifier.height(6.dp))
    Text(title, color = AppColors.Ink, fontSize = 29.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
    Spacer(Modifier.height(4.dp))
    Text(subtitle, color = AppColors.Muted, fontSize = 13.sp)
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(title, modifier = modifier, color = AppColors.Ink, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp)
}

@Composable
fun WhiteCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(21.dp), shadowElevation = 0.dp) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SubjectAvatar(subject: Subject?, size: Dp = 43.dp) {
    Box(Modifier.size(size).background(subject?.let { Color(it.color) } ?: Color(0xFFD5D9DE), RoundedCornerShape(size / 3)), contentAlignment = Alignment.Center) {
        Icon(subjectIcon(subject?.icon ?: "book"), contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun StatTile(icon: ImageVector, label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    WhiteCard(modifier) {
        Box(Modifier.size(35.dp).background(tint, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(13.dp))
        Text(value, color = AppColors.Ink, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = AppColors.Muted, fontSize = 12.sp)
    }
}

@Composable
fun SessionItem(session: StudySession, subject: Subject?, trailing: @Composable (() -> Unit)? = null) {
    val start = Instant.ofEpochMilli(session.startedAt).atZone(ZoneId.systemDefault())
    WhiteCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubjectAvatar(subject)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(subject?.name ?: "Deleted subject", color = AppColors.Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(start.format(DateTimeFormatter.ofPattern("h:mm a · MMM d")), color = AppColors.Muted, fontSize = 11.sp)
            }
            Text(StudyMath.formatDuration(session.durationMs), color = AppColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            trailing?.invoke()
        }
    }
}

@Composable
fun EmptyCard(title: String, message: String) {
    WhiteCard(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(10.dp))
        Text("✦", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = AppColors.Orange, fontSize = 27.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = AppColors.Ink, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        Text(message, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = AppColors.Muted, fontSize = 12.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun ThinProgress(fraction: Float, color: Color = AppColors.Orange) {
    Box(Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFF1F2EF), RoundedCornerShape(5.dp))) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(8.dp).background(color, RoundedCornerShape(5.dp)))
    }
}
