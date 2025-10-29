// src/main/java/com/developersbeeh/medcontrol/notifications/EmergencyAlertActivity.kt

package com.developersbeeh.medcontrol.notifications

import android.app.KeyguardManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.developersbeeh.medcontrol.databinding.ActivityEmergencyAlertBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmergencyAlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyAlertBinding
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val EXTRA_DEPENDENT_NAME = "dependent_name"
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura a tela para aparecer sobre a tela de bloqueio e acender
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        val dependentName = intent.getStringExtra(EXTRA_DEPENDENT_NAME) ?: "Alguém"
        binding.textViewDependentName.text = "$dependentName precisa de ajuda!"

        // Toca o som de alarme
        playSound()

        binding.buttonDismiss.setOnClickListener {
            stopSound()
            finish()
        }
    }

    private fun playSound() {
        try {
            // Usa o som de alarme padrão do sistema para máxima urgência
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

    override fun onDestroy() {
        super.onDestroy()
        stopSound()
    }
}