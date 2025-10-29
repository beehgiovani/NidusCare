// src/main/java/com/developersbeeh/medcontrol/MedControlApplication.kt

package com.developersbeeh.medcontrol

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.developersbeeh.medcontrol.notifications.AdherenceStreakWorker
import com.developersbeeh.medcontrol.notifications.HydrationReminderWorker
import com.developersbeeh.medcontrol.notifications.MissedDoseWorker
import com.developersbeeh.medcontrol.notifications.MotivationalNotificationWorker
import com.developersbeeh.medcontrol.notifications.StockExpiryWorker
import com.developersbeeh.medcontrol.notifications.VaccineAlertWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class MedControlApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        if (com.developersbeeh.medcontrol.BuildConfig.DEBUG) {
            Log.d("AppCheck_Variant", "CONFIRMADO: O build é DEBUG. Instalando o Debug Provider.")
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            Log.w("AppCheck_Variant", "AVISO: O build é RELEASE. Instalando o Play Integrity Provider.")
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        setupDailyStockCheck()
        setupMissedDoseCheck()
        setupAdherenceCheckWorker()
        setupMotivationalNotificationWorker() // NOVO WORKER AGENDADO
        setupVaccineAlertWorker() // NOVO WORKER AGENDADO
        setupHydrationReminderWorker() // NOVO WORKER AGENDADO
    }

    private fun setupDailyStockCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailyCheckRequest =
            PeriodicWorkRequestBuilder<StockExpiryWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyStockExpiryCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyCheckRequest
        )
    }

    private fun setupMissedDoseCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val missedDoseRequest =
            PeriodicWorkRequestBuilder<MissedDoseWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PeriodicMissedDoseCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            missedDoseRequest
        )
    }

    private fun setupAdherenceCheckWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val adherenceRequest =
            PeriodicWorkRequestBuilder<AdherenceStreakWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyAdherenceStreakCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            adherenceRequest
        )
    }

    private fun setupMotivationalNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val motivationalRequest =
            PeriodicWorkRequestBuilder<MotivationalNotificationWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInitialDelay(8, TimeUnit.HOURS) // Adiciona um atraso inicial para não disparar logo ao iniciar
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyMotivationalNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            motivationalRequest
        )
    }
    private fun setupVaccineAlertWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val vaccineAlertRequest =
            PeriodicWorkRequestBuilder<VaccineAlertWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInitialDelay(9, TimeUnit.HOURS) // Executa em um horário diferente dos outros
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyVaccineAlertCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            vaccineAlertRequest
        )
    }

    private fun setupHydrationReminderWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Executa a cada hora
        val hydrationRequest =
            PeriodicWorkRequestBuilder<HydrationReminderWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "HourlyHydrationCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            hydrationRequest
        )
    }

}