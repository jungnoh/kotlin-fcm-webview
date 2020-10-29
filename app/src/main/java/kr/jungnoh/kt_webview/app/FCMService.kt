package kr.jungnoh.kt_webview.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFCMService : FirebaseMessagingService() {
    var onChange: ((String)->Unit)? = null

    override fun onNewToken(p0: String) {
//        Log.v("FCM", "Refreshed token: $p0")
        onChange?.invoke(p0)
    }

    override fun onMessageReceived(remoteMsg: RemoteMessage) {
//        Log.v("FCM", "From: ${remoteMsg.from}")
        remoteMsg.data.isNotEmpty().let {
//            Log.v("FCM", "Message data payload: ${remoteMsg.data}")

            val builder = NotificationCompat.Builder(this, "default")
            builder.setSmallIcon(R.mipmap.ic_launcher)
            builder.setContentTitle(remoteMsg.data["title"])
            builder.setContentText(remoteMsg.data["message"])
            builder.setAutoCancel(true)
            val notiManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("page", remoteMsg.data["url"] ?: Config.host)
//            Log.i("FCM", intent.getStringExtra("page"))
            builder.setContentIntent(PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT))
            // Build and show notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notiManager.createNotificationChannel(NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT))
            }
            notiManager.notify(1, builder.build())
        }
    }
}