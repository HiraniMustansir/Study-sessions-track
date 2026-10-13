package com.studyflow.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.studyflow.tracker.data.StudyRepository
import com.studyflow.tracker.reminders.DailySummaryScheduler

class MainActivity : ComponentActivity() {
    private lateinit var repository: StudyRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = StudyRepository(this)
        DailySummaryScheduler.sync(this, repository)
        setContent { StudyflowApp() }
    }
}

@Composable
fun StudyflowApp() {
    MaterialTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Studyflow")
        }
    }
}
