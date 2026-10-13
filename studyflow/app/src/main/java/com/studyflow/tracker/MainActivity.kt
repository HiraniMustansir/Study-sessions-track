package com.studyflow.tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.studyflow.tracker.data.StudyRepository
import com.studyflow.tracker.reminders.DailySummaryScheduler
import com.studyflow.tracker.ui.StudyflowApp

class MainActivity : ComponentActivity() {
    private lateinit var repository: StudyRepository
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            repository.setNotifications(true)
            DailySummaryScheduler.sync(this, repository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = StudyRepository(this)
        DailySummaryScheduler.sync(this, repository)
        setContent {
            StudyflowApp(repository, ::toggleNotifications, ::changeReminderHour)
        }
    }

    private fun toggleNotifications(enabled: Boolean) {
        if (!enabled) {
            repository.setNotifications(false)
            DailySummaryScheduler.sync(this, repository)
        } else if (Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            repository.setNotifications(true)
            DailySummaryScheduler.sync(this, repository)
        } else {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun changeReminderHour(hour: Int) {
        repository.setReminderHour(hour)
        DailySummaryScheduler.sync(this, repository)
    }
}
