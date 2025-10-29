package com.developersbeeh.medcontrol.ui.wellbeing.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.developersbeeh.medcontrol.MainActivity
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Atividade
import com.developersbeeh.medcontrol.data.model.AtividadeFisica
import com.developersbeeh.medcontrol.data.model.TipoAtividade
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ActivityTimerService : Service() {

    @Inject lateinit var firestoreRepository: FirestoreRepository
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var userPreferences: UserPreferences

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var elapsedTime = 0L
    private var activityType: String = "" // Será atualizado no onCreate
    private var dependentId: String? = null

    companion object {
        const val ACTION_START_RESUME = "ACTION_START_RESUME"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_ACTIVITY_TYPE = "EXTRA_ACTIVITY_TYPE"
        const val EXTRA_DEPENDENT_ID = "EXTRA_DEPENDENT_ID"
        private const val NOTIFICATION_ID = 123
        private const val NOTIFICATION_CHANNEL_ID = "activity_timer_channel"
        // ✅ CORREÇÃO: Nome do canal movido para strings.xml
    }

    override fun onCreate() {
        super.onCreate()
        activityType = getString(R.string.timer_default_activity_type) // Define o padrão usando strings
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(EXTRA_ACTIVITY_TYPE)?.let { if (it.isNotBlank()) activityType = it }
        intent?.getStringExtra(EXTRA_DEPENDENT_ID)?.let { if (it.isNotBlank()) dependentId = it }

        when (intent?.action) {
            ACTION_START_RESUME -> startOrResumeTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimerAndService()
        }
        return START_STICKY
    }

    private fun startOrResumeTimer() {
        TimerServiceManager.updateState(TimerState.Running(elapsedTime))
        startForeground(NOTIFICATION_ID, createNotification().build())
        startTimerUpdates()
    }

    private fun pauseTimer() {
        if (runnable != null) {
            handler.removeCallbacks(runnable!!)
            runnable = null
        }
        TimerServiceManager.updateState(TimerState.Paused(elapsedTime))
        updateNotification()
    }

    private fun stopTimerAndService() {
        if (runnable != null) {
            handler.removeCallbacks(runnable!!)
            runnable = null
        }

        val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime).toInt()
        if (durationMinutes > 0 && dependentId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val novoRegistro = AtividadeFisica(tipo = activityType, duracaoMinutos = durationMinutes)
                firestoreRepository.saveAtividadeFisicaRecord(dependentId!!, novoRegistro)
                // ✅ REATORADO: Usa strings.xml
                saveLog(dependentId!!, getString(R.string.log_activity_timer_stopped, durationMinutes, activityType), TipoAtividade.ANOTACAO_CRIADA)
            }
        }

        elapsedTime = 0L
        TimerServiceManager.updateState(TimerState.Idle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private suspend fun saveLog(dependentId: String, descricao: String, tipo: TipoAtividade) {
        val autorNome = userPreferences.getUserName()
        val autorId = if (userPreferences.getIsCaregiver()) {
            userRepository.getCurrentUser()?.uid ?: ""
        } else { "dependent_user" }
        val log = Atividade(
            descricao = descricao,
            tipo = tipo,
            autorId = autorId,
            autorNome = autorNome // O 'getAuthorName' no backend usará este campo
        )
        firestoreRepository.saveActivityLog(dependentId, log)
    }

    private fun startTimerUpdates() {
        if (runnable != null) return
        runnable = object : Runnable {
            override fun run() {
                elapsedTime += 1000L
                TimerServiceManager.updateState(TimerState.Running(elapsedTime))
                updateNotification()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable!!)
    }

    private fun createNotification(): NotificationCompat.Builder {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            // ✅ REATORADO: Usa strings.xml
            .setContentTitle(getString(R.string.notification_activity_in_progress, activityType))
            .setSmallIcon(R.drawable.ic_fitness)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
    }

    private fun updateNotification() {
        val currentState = TimerServiceManager.timerState.value
        val notificationBuilder = createNotification()
        val timeString = formatTime(currentState)
        notificationBuilder.setContentText(timeString)
        notificationBuilder.clearActions()

        when (currentState) {
            is TimerState.Running -> {
                notificationBuilder.addAction(R.drawable.ic_timer_pause, getString(R.string.notification_action_pause), createActionIntent(ACTION_PAUSE))
                notificationBuilder.addAction(R.drawable.ic_timer_stop, getString(R.string.notification_action_stop), createActionIntent(ACTION_STOP))
            }
            is TimerState.Paused -> {
                notificationBuilder.addAction(R.drawable.ic_play_arrow, getString(R.string.notification_action_resume), createActionIntent(ACTION_START_RESUME))
                notificationBuilder.addAction(R.drawable.ic_timer_stop, getString(R.string.notification_action_stop), createActionIntent(ACTION_STOP))
            }
            else -> {}
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun formatTime(state: TimerState): String {
        val timeInMillis = when(state) {
            is TimerState.Running -> state.elapsedTime
            is TimerState.Paused -> state.elapsedTime
            else -> 0L
        }
        val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
        return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else String.format("%02d:%02d", minutes, seconds)
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(this, ActivityTimerService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name), // ✅ REATORADO: Usa strings.xml
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}