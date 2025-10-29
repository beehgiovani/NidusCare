package com.developersbeeh.medcontrol.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.developersbeeh.medcontrol.MainActivity
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.DoseHistory
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.Reminder
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import java.time.LocalDateTime
import java.time.ZoneId

private const val TAG = "NotificationScheduler"

const val STOCK_CHANNEL_ID = "medcontrol_stock_channel"
const val STOCK_CHANNEL_NAME = "Alertas de Estoque e Validade"
const val MOTIVATIONAL_CHANNEL_ID = "medcontrol_motivational_channel"
const val MOTIVATIONAL_CHANNEL_NAME = "Lembretes Motivacionais"
const val VACCINE_CHANNEL_ID = "medcontrol_vaccine_channel"
const val VACCINE_CHANNEL_NAME = "Alertas de Vacinação"
const val EMERGENCY_CHANNEL_ID = "medcontrol_emergency_channel"
const val EMERGENCY_CHANNEL_NAME = "Alertas de Emergência"
const val HYDRATION_CHANNEL_ID = "medcontrol_hydration_channel"
const val HYDRATION_CHANNEL_NAME = "Lembretes de Hidratação"
const val MISSED_DOSE_CHANNEL_ID = "medcontrol_missed_dose_channel"
const val MISSED_DOSE_CHANNEL_NAME = "Alertas de Doses Atrasadas"

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showStockOrExpiryNotification(title: String, message: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                STOCK_CHANNEL_ID,
                STOCK_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para alertas de estoque baixo e validade próxima."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, STOCK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showMotivationalNotification(title: String, message: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MOTIVATIONAL_CHANNEL_ID,
                MOTIVATIONAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para mensagens de incentivo e dicas de saúde."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, MOTIVATIONAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_motivation)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun schedule(
        medicamento: Medicamento,
        dependentId: String,
        dependentName: String,
        doseHistory: List<DoseHistory>
    ) {
        try {
            require(medicamento.id.isNotBlank()) { "ID do medicamento não pode ser vazio." }
            require(dependentId.isNotBlank()) { "ID do dependente é inválido." }

            if (!medicamento.usaNotificacao || medicamento.horarios.isEmpty() || medicamento.isPaused) {
                Log.d(TAG, "Agendamento ignorado para '${medicamento.nome}'. Motivo: Notificação desativada, sem horários ou pausada.")
                cancelAllNotifications(medicamento, dependentId)
                return
            }

            val nextNotificationTime = DoseTimeCalculator.calculateNextDoseTime(medicamento, doseHistory)

            if (nextNotificationTime == null) {
                Log.d(TAG, "Não há próximo horário de notificação para '${medicamento.nome}'. Cancelando alarmes existentes.")
                cancelAllNotifications(medicamento, dependentId)
                return
            }

            val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
                putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_ID, medicamento.id)
                putExtra(NotificationBroadcastReceiver.EXTRA_DEPENDENT_ID, dependentId)
                putExtra(NotificationBroadcastReceiver.EXTRA_DEPENDENT_NAME, dependentName)
                putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_NOME, medicamento.nome)
                putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_DOSAGEM, medicamento.dosagem)
                // ✅ ADIÇÃO: Passa a hora exata que foi agendada
                putExtra(NotificationBroadcastReceiver.EXTRA_SCHEDULED_TIME, nextNotificationTime.toString())
            }

            val requestCode = (medicamento.id + nextNotificationTime.toLocalTime().toString()).hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerAtMillis = nextNotificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Permissão para agendar alarmes exatos não foi concedida.")
            }
            Log.i(TAG, "Agendando notificação para '${medicamento.nome}' (ID: ${medicamento.id}) em: $nextNotificationTime")
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)

            scheduleMissedDoseCheck(medicamento, dependentId, dependentName, nextNotificationTime)

        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Erro de validação no agendamento: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao agendar notificação: ${e.message}", e)
        }
    }

    private fun scheduleMissedDoseCheck(medicamento: Medicamento, dependentId: String, dependentName: String, scheduledTime: LocalDateTime) {
        val checkTime = scheduledTime.plusMinutes(31) // 31 minutos para ter uma margem de tolerância
        val intent = Intent(context, MissedDoseReceiver::class.java).apply {
            putExtra(MissedDoseReceiver.EXTRA_MED_ID, medicamento.id)
            putExtra(MissedDoseReceiver.EXTRA_DEP_ID, dependentId)
            putExtra(MissedDoseReceiver.EXTRA_DEP_NAME, dependentName)
            putExtra(MissedDoseReceiver.EXTRA_SCHEDULED_TIME, scheduledTime.toString())
        }
        val requestCode = (medicamento.id + scheduledTime.toString() + "_missed_check").hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = checkTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        Log.i(TAG, "Agendando verificação de dose perdida para '${medicamento.nome}' em: $checkTime")
    }

    fun showMissedDoseNotification(medicamento: Medicamento, dependentId: String, dependentName: String, scheduledTime: LocalDateTime) {
        val channelId = MISSED_DOSE_CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, MISSED_DOSE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = (medicamento.id + "_missed").hashCode()
        val title = "🚨 Dose Atrasada: $dependentName"
        val message = "A dose de ${medicamento.nome} das ${scheduledTime.toLocalTime()} não foi registrada."

        // Ação: Tomei Agora
        val takenNowIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_AS_TAKEN_NOW
            putExtra(NotificationActionReceiver.EXTRA_MEDICAMENTO_ID, medicamento.id)
            putExtra(NotificationActionReceiver.EXTRA_DEPENDENT_ID, dependentId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_DEPENDENT_NAME, dependentName)
        }
        val takenNowPendingIntent = PendingIntent.getBroadcast(context, notificationId + 1, takenNowIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Ação: Esqueci de Registrar
        val forgotIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_FORGOT_TO_LOG"
            putExtra("dependentId", dependentId)
            putExtra("medicamentoId", medicamento.id)
            putExtra("scheduledTime", scheduledTime.toString())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val forgotPendingIntent = PendingIntent.getActivity(context, notificationId + 2, forgotIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Ação: Pular Dose
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SKIP_DOSE
            putExtra(NotificationActionReceiver.EXTRA_MEDICAMENTO_ID, medicamento.id)
            putExtra(NotificationActionReceiver.EXTRA_DEPENDENT_ID, dependentId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULED_TIME, scheduledTime.toString())
        }
        val skipPendingIntent = PendingIntent.getBroadcast(context, notificationId + 3, skipIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "Tomei Agora", takenNowPendingIntent)
            .addAction(0, "Esqueci de Registrar", forgotPendingIntent)
            .addAction(0, "Pular Dose", skipPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun scheduleSnooze(medicamento: Medicamento, dependentId: String, dependentName: String, minutes: Long) {
        val now = LocalDateTime.now()
        val snoozeTime = now.plusMinutes(minutes)
        val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_ID, medicamento.id)
            putExtra(NotificationBroadcastReceiver.EXTRA_DEPENDENT_ID, dependentId)
            putExtra(NotificationBroadcastReceiver.EXTRA_DEPENDENT_NAME, dependentName)
            putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_NOME, medicamento.nome)
            putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_DOSAGEM, medicamento.dosagem)
            // ✅ ADIÇÃO: Repassa a hora agendada original durante o "snooze"
            val originalScheduledTime = DoseTimeCalculator.getLastScheduledDoseBeforeNow(medicamento) ?: now
            putExtra(NotificationBroadcastReceiver.EXTRA_SCHEDULED_TIME, originalScheduledTime.toString())
        }
        val requestCode = (medicamento.id + now.toString() + "snooze").hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = snoozeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.i(TAG, "Adiado notificação de '${medicamento.nome}' por $minutes minutos para: $snoozeTime")
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancelSpecificNotification(medicamento: Medicamento, notificationTime: LocalDateTime, dependentId: String, dependentName: String) {
        try {
            val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
                putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_ID, medicamento.id)
                putExtra(NotificationBroadcastReceiver.EXTRA_DEPENDENT_ID, dependentId)
                putExtra(NotificationBroadcastReceiver.EXTRA_DEPENDENT_NAME, dependentName)
                putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_NOME, medicamento.nome)
                putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_DOSAGEM, medicamento.dosagem)
                putExtra(NotificationBroadcastReceiver.EXTRA_SCHEDULED_TIME, notificationTime.toString())
            }
            val requestCode = (medicamento.id + notificationTime.toLocalTime().toString()).hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.i(TAG, "Notificação ESPECÍFICA cancelada para '${medicamento.nome}' no horário $notificationTime.")
            } else {
                Log.w(TAG, "Tentativa de cancelar notificação que não existe para '${medicamento.nome}'.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar notificação específica: ${e.message}", e)
        }
    }

    fun scheduleMissedDoseFollowUp(medicamento: Medicamento, dependentId: String, dependentName: String, followUpCount: Int) {
        val intent = Intent(context, MissedDoseFollowUpReceiver::class.java).apply {
            putExtra("MED_ID", medicamento.id)
            putExtra("DEP_ID", dependentId)
            putExtra("DEP_NAME", dependentName)
            putExtra("FOLLOW_UP_COUNT", followUpCount)
        }
        val requestCode = ("missed_follow_up_${medicamento.id}").hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutos

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        Log.i(TAG, "Agendado acompanhamento #${followUpCount + 1} para '${medicamento.nome}' em 15 minutos.")
    }

    fun cancelMissedDoseFollowUp(medicamentoId: String) {
        val intent = Intent(context, MissedDoseFollowUpReceiver::class.java)
        val requestCode = ("missed_follow_up_$medicamentoId").hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.i(TAG, "Acompanhamentos de dose perdida cancelados para medId: $medicamentoId")
        }
    }

    fun showMissedDoseFollowUpNotification(medicamento: Medicamento, dependentName: String) {
        val notificationId = (medicamento.id + "_missed_follow_up").hashCode()
        val title = "Atenção: Dose Ainda Pendente"
        val message = "O registro da dose de ${medicamento.nome} para ${dependentName} ainda não foi feito. Por favor, verifique."

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, MISSED_DOSE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_missed_dose_follow_up)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun cancelAllNotifications(medicamento: Medicamento, dependentId: String) {
        try {
            Log.d(TAG, "Cancelando TODAS as notificações de '${medicamento.nome}' (ID: ${medicamento.id}).")
            medicamento.horarios.forEach { horario ->
                // Recria a intent exatamente como foi criada para garantir a correspondência
                val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
                    putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_ID, medicamento.id)
                    putExtra(NotificationBroadcastReceiver.EXTRA_DEPENDENT_ID, dependentId)
                    // Adiciona os outros extras que podem ter sido usados para criar o requestCode original
                    putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_NOME, medicamento.nome)
                    putExtra(NotificationBroadcastReceiver.EXTRA_MEDICAMENTO_DOSAGEM, medicamento.dosagem)
                }

                // Recalcula o requestCode de forma consistente
                val nextDose = DoseTimeCalculator.calculateNextDoseTime(medicamento, emptyList())
                if (nextDose != null) {
                    val requestCode = (medicamento.id + nextDose.toLocalTime().toString()).hashCode()
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, requestCode, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                        pendingIntent.cancel()
                        Log.d(TAG, "Alarme para '${medicamento.nome}' em '$horario' cancelado.")
                    }
                }
            }
            Log.i(TAG, "Todas as notificações para '${medicamento.nome}' foram processadas para cancelamento.")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar todas as notificações: ${e.message}", e)
        }
    }

    fun scheduleReminder(reminder: Reminder, dependentId: String) {
        if (!reminder.isActive) {
            cancelReminder(reminder, dependentId)
            return
        }
        val now = LocalDateTime.now()
        var nextTriggerTime = now.withHour(reminder.time.hour).withMinute(reminder.time.minute).withSecond(0)
        if (nextTriggerTime.isBefore(now)) {
            nextTriggerTime = nextTriggerTime.plusDays(1)
        }
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_TYPE, reminder.type)
            putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_ID, (reminder.id + dependentId).hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, (reminder.id + dependentId).hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = nextTriggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP, triggerAtMillis,
            AlarmManager.INTERVAL_DAY, pendingIntent
        )
    }

    fun cancelReminder(reminder: Reminder, dependentId: String) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, (reminder.id + dependentId).hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun showVaccineAlertNotification(title: String, message: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                VACCINE_CHANNEL_ID,
                VACCINE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para alertas importantes sobre vacinas próximas ou atrasadas."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, VACCINE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vaccine_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showEmergencyAlertNotification(dependentName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EMERGENCY_CHANNEL_ID,
                EMERGENCY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificações críticas de emergência."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, EmergencyAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EmergencyAlertActivity.EXTRA_DEPENDENT_NAME, dependentName)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, "emergency".hashCode(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, EMERGENCY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_emergency_alert)
            .setContentTitle("ALERTA DE EMERGÊNCIA")
            .setContentText("$dependentName precisa de ajuda!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify("emergency".hashCode(), notification)
    }

    fun showHydrationReminderNotification(title: String, message: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HYDRATION_CHANNEL_ID,
                HYDRATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Lembretes inteligentes para ajudar a atingir sua meta de hidratação."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, HYDRATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_hydration_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}