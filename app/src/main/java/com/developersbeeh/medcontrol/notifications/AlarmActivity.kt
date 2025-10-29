package com.developersbeeh.medcontrol.notifications

import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.developersbeeh.medcontrol.databinding.ActivityAlarmBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val EXTRA_DEPENDENT_NAME = "dependent_name"
        const val EXTRA_MEDICATION_INFO = "medication_info"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura a tela para aparecer sobre a tela de bloqueio e acender
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }

        // --- Recebe TODOS os dados do Intent ---
        val dependentName = intent.getStringExtra(EXTRA_DEPENDENT_NAME) ?: "Lembrete"
        val medicationInfo = intent.getStringExtra(EXTRA_MEDICATION_INFO) ?: "Verifique seus medicamentos."
        val medId = intent.getStringExtra(NotificationActionReceiver.EXTRA_MEDICAMENTO_ID)
        val depId = intent.getStringExtra(NotificationActionReceiver.EXTRA_DEPENDENT_ID)
        val notificationId = intent.getIntExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, 0)
        val scheduledTime = intent.getStringExtra(NotificationActionReceiver.EXTRA_SCHEDULED_TIME)

        binding.textViewDependentName.text = dependentName
        binding.textViewMedicationInfo.text = medicationInfo

        playSound()

        // --- Configura os novos botões ---

        // Ação: TOMAR AGORA
        binding.buttonMarkAsTaken.setOnClickListener {
            if (medId != null && depId != null) {
                val intent = Intent(this, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_MARK_AS_TAKEN
                    putExtra(NotificationActionReceiver.EXTRA_MEDICAMENTO_ID, medId)
                    putExtra(NotificationActionReceiver.EXTRA_DEPENDENT_ID, depId)
                    putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                    putExtra(NotificationActionReceiver.EXTRA_DEPENDENT_NAME, dependentName)
                    putExtra(NotificationActionReceiver.EXTRA_SCHEDULED_TIME, scheduledTime)
                }
                sendBroadcast(intent)
            }
            stopAndFinish()
        }

        // Ação: ADIAR
        binding.buttonSnooze.setOnClickListener {
            if (medId != null && depId != null) {
                val intent = Intent(this, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_SNOOZE
                    putExtra(NotificationActionReceiver.EXTRA_SNOOZE_MINUTES, 15)
                    putExtra(NotificationActionReceiver.EXTRA_MEDICAMENTO_ID, medId)
                    putExtra(NotificationActionReceiver.EXTRA_DEPENDENT_ID, depId)
                    putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                    putExtra(NotificationActionReceiver.EXTRA_DEPENDENT_NAME, dependentName)
                }
                sendBroadcast(intent)
            }
            stopAndFinish()
        }

        // Ação: PULAR DOSE
        binding.buttonSkip.setOnClickListener {
            if (medId != null && depId != null && scheduledTime != null) {
                val intent = Intent(this, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_SKIP_DOSE
                    putExtra(NotificationActionReceiver.EXTRA_MEDICAMENTO_ID, medId)
                    putExtra(NotificationActionReceiver.EXTRA_DEPENDENT_ID, depId)
                    putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                    putExtra(NotificationActionReceiver.EXTRA_SCHEDULED_TIME, scheduledTime)
                }
                sendBroadcast(intent)
            }
            stopAndFinish()
        }
    }

    private fun playSound() {
        try {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(applicationContext, alarmSound)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun stopAndFinish() {
        // Cancela a notificação na barra de status
        val notificationId = intent.getIntExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, 0)
        if (notificationId != 0) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }
        stopSound()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSound()
    }
}