/*
 *
 *  *  Copyright (c) 2026 Dhanush Sugganahalli <dhanush41230@gmail.com>
 *  *
 *  *  This program is free software; you can redistribute it and/or modify it under
 *  *  the terms of the GNU General Public License as published by the Free Software
 *  *  Foundation; either version 3 of the License, or (at your option) any later
 *  *  version.
 *  *
 *  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License along with
 *  *  this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.opennotes.notes.presentation.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.opennotes.notes.domain.usecase.NoteUseCases
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val noteUseCases: NoteUseCases,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getInt("NOTE_ID", -1)
        if (noteId == -1) return Result.failure()

        val note = noteUseCases.getNote(noteId) ?: return Result.failure()

        showNotification(noteId, note.title.ifBlank { "Reminder" }, note.content.ifBlank { "Open note to view details" })

        // Handle Rescheduling if repetition is set
        if (note.repeatInterval != null && note.repeatInterval > 0 && note.repeatUnit != null) {
            val nextTime = calculateNextTriggerTime(note.reminderTime ?: System.currentTimeMillis(), note.repeatInterval, note.repeatUnit)
            
            // Update note in DB with next trigger time
            val updatedNote = note.copy(reminderTime = nextTime)
            noteUseCases.addNote(updatedNote)

            // Schedule next work
            scheduleNextReminder(nextTime, noteId, updatedNote.title, updatedNote.content)
        } else {
            // If no repeat, clear the reminderTime in DB so it doesn't show as active in UI
            noteUseCases.addNote(note.copy(reminderTime = null))
        }

        return Result.success()
    }

    private fun calculateNextTriggerTime(currentTime: Long, interval: Long, unit: String): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTime }
        when (unit) {
            "MINUTES" -> calendar.add(Calendar.MINUTE, interval.toInt())
            "HOURS" -> calendar.add(Calendar.HOUR_OF_DAY, interval.toInt())
            "DAYS" -> calendar.add(Calendar.DAY_OF_YEAR, interval.toInt())
            "WEEKS" -> calendar.add(Calendar.WEEK_OF_YEAR, interval.toInt())
            "MONTHS" -> calendar.add(Calendar.MONTH, interval.toInt())
            "YEARS" -> calendar.add(Calendar.YEAR, interval.toInt())
            else -> calendar.add(Calendar.DAY_OF_YEAR, interval.toInt()) // Default to daily
        }
        return calendar.timeInMillis
    }

    private fun scheduleNextReminder(triggerTime: Long, noteId: Int, title: String, content: String) {
        val delay = triggerTime - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putInt("NOTE_ID", noteId)
            .putString("NOTE_TITLE", title.ifBlank { "Reminder" })
            .putString("NOTE_CONTENT", content.ifBlank { "Open note to view details" })
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "reminder_work_$noteId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun showNotification(
        noteId: Int,
        title: String,
        content: String,
    ) {
        val channelId = "note_reminders_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    channelId,
                    "Note Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Channel for note reminder notifications"
                }
            notificationManager.createNotificationChannel(channel)
        }

        val intent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("opennotes://note/$noteId?noteColor=-1"),
            ).apply {
                `package` = applicationContext.packageName
            }

        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                noteId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(applicationContext, channelId)
                .setSmallIcon(com.opennotes.R.drawable.ic_launcher_monochrome)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(noteId, notification)
    }
}
