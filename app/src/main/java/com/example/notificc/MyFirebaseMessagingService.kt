package com.example.notificc
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        val title = remoteMessage.notification?.title ?: "No Title"
//        val body = remoteMessage.notification?.body ?: "No Message"
        val title = remoteMessage.data["title"] ?: "No Title"
        val body = remoteMessage.data["body"] ?: "No Message"
        val imageUrl = remoteMessage.data["image"]
        val notifId = remoteMessage.data["notif_id"]
        val disasterReportId = remoteMessage.data["disaster_report_id"]

        Log.d("FCM", "notifID: " + notifId)
        Log.e("FCM", "asasassas")

        val entry = NotificationEntry(title, body, System.currentTimeMillis(), imageUrl)
        NotificationStorage.saveNotification(applicationContext, entry)

        // Broadcast ke UI jika app sedang aktif
        NotificationReceiver.onNotificationReceived?.invoke(title, body)

        showNotification(notifId, title, body, imageUrl)
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(notifId: String?, title: String, message: String, imageUrl: String? = null) {
        val channelId = "high_priority_channel"

        // Buat notification channel (untuk Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, "FCM Test Ch", importance).apply {
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description = "General notifications"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notifId", notifId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notifId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Bangun notifikasi
        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher) // tambahkan icon di drawable
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            )
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_MAX)

        if (imageUrl != null) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)

                builder.setLargeIcon(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val notification = builder.build()

        // Tampilkan notifikasi
        val notificationId = System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}