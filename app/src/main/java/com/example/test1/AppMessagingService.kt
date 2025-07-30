package com.example.test1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.test1.utils.checkNotificationPermission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.installations.ktx.installations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppMessagingService : FirebaseMessagingService() {


    // Wywoływane, gdy token FCM jest generowany lub odświeżany
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nowy/odświeżony token: $token")

        Firebase.auth.currentUser?.uid?.let { userId ->
            updateTokenInFirestore(userId, token)
        }
    }

    // Wywoływane, gdy aplikacja otrzyma powiadomienie
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Otrzymano wiadomość: ${remoteMessage.from}")

        // Wyświetl powiadomienie tylko jeśli ma ono część "notification"
        remoteMessage.notification?.let { notification ->
            Log.d("FCM", "Tytuł: ${notification.title}, Treść: ${notification.body}")

            // Pobierz dodatkowe dane, np. ID zajęć, do nawigacji
            val classId = remoteMessage.data["classId"]

            sendNotification(notification.title, notification.body, classId)
        }
    }

    private fun sendNotification(title: String?, body: String?, classId: String?) {
        // Utwórz Intent, który otworzy aplikację po kliknięciu
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Przekaż ID zajęć, aby MainActivity wiedziało, co otworzyć
            putExtra("EXTRA_CLASS_ID", classId)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "upcoming_class_channel"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Akcja po kliknięciu
            .setAutoCancel(true) // Automatyczne zamykanie po kliknięciu


        with(NotificationManagerCompat.from(this)) {
            // ✅ Sprawdź uprawnienia przed wywołaniem notify()
            if (checkNotificationPermission(this@AppMessagingService)) {
                notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
            } else {
                Log.w("FCM", "Brak uprawnień do wysyłania powiadomień.")
            }
        }
    }

    // Funkcja do aktualizacji tokena w Firestore
    private fun updateTokenInFirestore(userId: String, token: String) {
        Firebase.installations.id.addOnSuccessListener { deviceId ->
            val tokenPath = "devices.$deviceId.token"
            Firebase.firestore.collection("students").document(userId)
                .update(tokenPath, token)
                .addOnSuccessListener { Log.d("FCM", "Token zaktualizowany w Firestore.") }
                .addOnFailureListener { e -> Log.w("FCM", "Błąd aktualizacji tokena.", e) }
        }
    }

}