// src/main/java/com/developersbeeh/medcontrol/notifications/MyFirebaseMessagingService.kt

package com.developersbeeh.medcontrol.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_MESSAGE", "Mensagem recebida: ${remoteMessage.data}")

        // Verifica o tipo de notificação recebida
        when (remoteMessage.data["type"]) {
            "EMERGENCY_ALERT" -> {
                val dependentName = remoteMessage.data["dependentName"] ?: "Alguém"
                val scheduler = NotificationScheduler(applicationContext)
                scheduler.showEmergencyAlertNotification(dependentName)
            }
            // Outros tipos de notificação podem ser tratados aqui no futuro
            else -> {
                // Comportamento padrão
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Novo token gerado: $token")
        saveTokenLocally(token)
        sendTokenToServer(token)
    }

    private fun saveTokenLocally(token: String) {
        val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("pending_fcm_token", token).apply()
    }

    private fun sendTokenToServer(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            val tokenData = mapOf("fcmToken" to token)
            userRef.update(tokenData)
                .addOnSuccessListener {
                    Log.d("FCM_TOKEN", "Token salvo no Firestore com sucesso.")
                    clearLocalToken()
                }
                .addOnFailureListener { e ->
                    Log.e("FCM_TOKEN", "Erro ao salvar token no Firestore.", e)
                }
        } else {
            Log.w("FCM_TOKEN", "Usuário não logado — token salvo localmente para envio posterior.")
        }
    }

    fun syncLocalTokenIfExists() {
        val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        val pendingToken = prefs.getString("pending_fcm_token", null)
        if (!pendingToken.isNullOrEmpty()) {
            Log.d("FCM_TOKEN", "Sincronizando token pendente após login...")
            sendTokenToServer(pendingToken)
        }
    }

    private fun clearLocalToken() {
        val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("pending_fcm_token").apply()
    }
}