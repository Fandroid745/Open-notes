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

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.opennotes.R
import com.opennotes.notes.domain.usecase.NoteUseCases
import com.opennotes.notes.domain.util.ReminderUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted workerParams: WorkerParameters,
        private val noteUseCases: NoteUseCases,
    ) : CoroutineWorker(context, workerParams) {
        companion object {
            private const val TAG = "ReminderWorker"
            private const val CHANNEL_ID = "note_reminders_channel_v3"
        }

        override suspend fun doWork(): Result {
            val noteId = inputData.getInt("NOTE_ID", -1)
            if (noteId == -1) return Result.failure()

            return try {
                val note = noteUseCases.getNote(noteId) ?: return Result.failure()

                showNotification(noteId, note.title.ifBlank { applicationContext.getString(R.string.reminder_notification_title) }, note.content.ifBlank { applicationContext.getString(R.string.reminder_notification_content) })

                // Handle Rescheduling if repetition is set
                if (note.repeatInterval != null && note.repeatInterval > 0 && note.repeatUnit != null) {
                    val nextTime = ReminderUtils.calculateNextTriggerTime(note.reminderTime ?: System.currentTimeMillis(), note.repeatInterval, note.repeatUnit)

                    // Update note in DB with next trigger time
                    val updatedNote = note.copy(reminderTime = nextTime)
                    noteUseCases.addNote(updatedNote)

                    // Schedule next work
                    scheduleNextReminder(nextTime, noteId, updatedNote.title, updatedNote.content)
                } else {
                    // If no repeat, clear the reminderTime in DB
                    noteUseCases.addNote(note.copy(reminderTime = null))
                }

                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Error in ReminderWorker", e)
                Result.retry()
            }
        }

        private fun scheduleNextReminder(
            triggerTime: Long,
            noteId: Int,
            title: String,
            content: String,
        ) {
            val now = System.currentTimeMillis()
            val delay = (triggerTime - now).coerceAtLeast(1000L)

            val data =
                Data
                    .Builder()
                    .putInt("NOTE_ID", noteId)
                    .putString("NOTE_TITLE", title)
                    .putString("NOTE_CONTENT", content)
                    .build()

            val workRequest =
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "reminder_work_$noteId",
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )
        }

        private fun showNotification(
            noteId: Int,
            title: String,
            content: String,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(CHANNEL_ID, "Note Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Urgent note reminders"
                        enableVibration(true)
                    }
                notificationManager.createNotificationChannel(channel)
            }

            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("opennotes://note/$noteId?noteColor=-1")).apply {
                    `package` = applicationContext.packageName
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

            val pendingIntent = PendingIntent.getActivity(applicationContext, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val notification =
                NotificationCompat
                    .Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(com.opennotes.R.drawable.ic_launcher_monochrome)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()

            notificationManager.notify(noteId, notification)
        }
    }
