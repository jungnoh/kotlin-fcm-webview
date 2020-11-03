package kr.jungnoh.kt_webview.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

fun extractCookie(originalCookie: String?, key: String = Config.sessionKey): String? {
    if (originalCookie == null) {
        return null
    }
    val items = originalCookie.split(';')
    for (item: String in items) {
        val parts = item.split('=')
        if (parts.size < 2) {
            continue
        }
        if (parts[0].trim().compareTo(key) == 0) {
            return parts[1].trim()
        }
    }
    return null
}

/**
 * 푸쉬 알림을 생성, 표시합니다.
 * @param context 생성 Context
 * @param title 알림 제목
 * @param body 알림 내용
 * @param url 알림을 열었을 때 WebView 에서 이동할 페이지 (없을 경우 메인 화면으로 이동함)
 */
fun showNotification(context: Context, title: String?, body: String?, url: String?) {
    // Notification Builder 생성 후 내용물 설정
    val builder = NotificationCompat.Builder(context, "default")
    builder.setSmallIcon(R.mipmap.ic_launcher)
    builder.setContentTitle(title ?: context.getString(R.string.app_name))
    builder.setContentText(body ?: "")
    builder.setAutoCancel(true)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val intent = Intent(context, MainActivity::class.java)
    // Intent extra `page` 설정: MainActivity 에서 수신 후 해당 페이지로 이동함
    intent.putExtra("page", url ?: Config.host)
    // requestCode 중복을 막기 위해 timestamp 사용
    val contentIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
    builder.setContentIntent(contentIntent)
    // Build and show notification
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // @see https://developer.android.com/training/notify-user/channels
        notificationManager.createNotificationChannel(NotificationChannel("default", "알림", NotificationManager.IMPORTANCE_DEFAULT))
    }
    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
}
