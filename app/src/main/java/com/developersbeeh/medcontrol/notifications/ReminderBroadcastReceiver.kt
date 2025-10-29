package com.developersbeeh.medcontrol.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.developersbeeh.medcontrol.MainActivity
import com.developersbeeh.medcontrol.R

class ReminderBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_TYPE = "reminder_type"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val NOTIFICATION_CHANNEL_ID = "medcontrol_reminders_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Lembretes de Saúde"
        private const val TAG = "ReminderReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderType = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: "Lembrete"
        val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, 0)

        Log.d(TAG, "Broadcast de lembrete recebido: $reminderType")
        showNotification(context, reminderType, reminderId)
    }

    private fun showNotification(context: Context, type: String, id: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // CRITICAL FIX: Add a PendingIntent to make the notification clickable
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("Lembrete de Saúde")
            .setContentText("Está na hora de: $type")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Associate the PendingIntent

        notificationManager.notify(id, builder.build())
        Log.d(TAG, "Notificação de lembrete exibida com sucesso (ID: $id).")
    }
}