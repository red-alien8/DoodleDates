package com.spycodedoodledates.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.spycodedoodledates.MainActivity
import com.spycodedoodledates.ui.alarm.AlarmActivity
import com.spycodedoodledates.ui.theme.settings.ThemeSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class NoteAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var themeSettings: ThemeSettings

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val noteId = intent.getLongExtra("noteId", -1)
        val monthKey = intent.getStringExtra("monthKey") ?: ""
        val alarmLabel = intent.getStringExtra("alarmLabel") ?: "Reminder"

        if (action == "ACTION_SNOOZE") {
            snoozeAlarm(context, noteId, monthKey, alarmLabel)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(noteId.toInt())
            return
        }

        if (action == "ACTION_DISMISS") {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(noteId.toInt())
            return
        }

        showAlarmNotification(context, noteId, monthKey, alarmLabel)
    }

    private fun showAlarmNotification(context: Context, noteId: Long, monthKey: String, alarmLabel: String) {
        val settings = runBlocking { themeSettings.settingsFlow.first() }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "note_reminders_v5"
        
        val soundUri = if (!settings.ringtoneUri.isNullOrEmpty()) {
            Uri.parse(settings.ringtoneUri)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Sticky Note Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Critical alarms for your sticky notes"
                enableLights(true)
                enableVibration(settings.vibrationEnabled)
                if (!settings.vibrationEnabled) {
                    vibrationPattern = longArrayOf(0)
                }
                setSound(soundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Full Screen Intent
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("monthKey", monthKey)
            putExtra("noteId", noteId)
        }

        // Only auto-start activity if screen is locked or high priority
        context.startActivity(fullScreenIntent)

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action
        val snoozeIntent = Intent(context, NoteAlarmReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("noteId", noteId)
            putExtra("monthKey", monthKey)
            putExtra("alarmLabel", alarmLabel)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(context, noteId.toInt() + 1000, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Dismiss Action
        val dismissIntent = Intent(context, NoteAlarmReceiver::class.java).apply {
            action = "ACTION_DISMISS"
            putExtra("noteId", noteId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(context, noteId.toInt() + 2000, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("DoodleDates Reminder")
            .setContentText(alarmLabel)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze (10m)", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .build()

        try {
            notificationManager.notify(noteId.toInt(), notification)
        } catch (e: SecurityException) {
        }
    }

    private fun snoozeAlarm(context: Context, noteId: Long, monthKey: String, alarmLabel: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NoteAlarmReceiver::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("monthKey", monthKey)
            putExtra("alarmLabel", alarmLabel)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}
