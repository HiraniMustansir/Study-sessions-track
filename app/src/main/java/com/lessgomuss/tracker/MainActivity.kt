package com.lessgomuss.tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.lessgomuss.tracker.data.StudyRepository
import com.lessgomuss.tracker.reminders.DailySummaryScheduler
import com.lessgomuss.tracker.ui.LessgoMussApp
import java.io.ByteArrayOutputStream
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private lateinit var repository: StudyRepository
    private var transferMessage by mutableStateOf<String?>(null)
    private val exportDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            transferMessage = runCatching {
                val backup = repository.exportBackup()
                contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                    stream.write(backup.toByteArray(Charsets.UTF_8))
                    stream.flush()
                } ?: error("Could not open the selected file.")
                "Backup saved. Keep a copy somewhere safe."
            }.getOrElse { "Export failed: ${it.message ?: "Could not write the file."}" }
        }
    }
    private val importDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            transferMessage = runCatching {
                val bytes = ByteArrayOutputStream()
                contentResolver.openInputStream(uri)?.use { stream ->
                    val chunk = ByteArray(8192)
                    while (true) {
                        val count = stream.read(chunk)
                        if (count < 0) break
                        require(bytes.size() + count <= 10_000_000) { "Backup file is too large." }
                        bytes.write(chunk, 0, count)
                    }
                } ?: error("Could not open the selected file.")
                repository.importBackup(bytes.toString(Charsets.UTF_8.name()))
                DailySummaryScheduler.sync(this, repository)
                if (repository.state.settings.notificationsEnabled && Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                "Backup imported. Your subjects and session history are ready."
            }.getOrElse { "Import failed: ${it.message ?: "Invalid backup file."}" }
        }
    }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            repository.setNotifications(true)
            DailySummaryScheduler.sync(this, repository)
        } else if (repository.state.settings.notificationsEnabled) {
            repository.setNotifications(false)
            DailySummaryScheduler.sync(this, repository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = StudyRepository(this)
        DailySummaryScheduler.sync(this, repository)
        setContent {
            LessgoMussApp(repository, ::toggleNotifications,
                onExport = { exportDocument.launch("lessgo-muss-backup-${LocalDate.now()}.json") },
                onImport = { importDocument.launch(arrayOf("*/*")) },
                transferMessage = transferMessage,
                onDismissTransferMessage = { transferMessage = null },
            )
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

}
