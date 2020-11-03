package kr.jungnoh.kt_webview.app

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFCMService : FirebaseMessagingService() {
    private lateinit var sessionSyncManager: SessionSyncManager

    override fun onNewToken(token: String) {
        LiveData.fcmToken.postValue(token)
    }

    override fun onMessageReceived(remoteMsg: RemoteMessage) {
        Log.v("FCM", "From: ${remoteMsg.from}")
        remoteMsg.data.isNotEmpty().let {
            Log.v("FCM", "Message data payload: ${remoteMsg.data}")
            showNotification(this, remoteMsg.data["title"], remoteMsg.data["message"], remoteMsg.data["url"])
        }
    }

    override fun onCreate() {
        super.onCreate()
        sessionSyncManager = SessionSyncManager(null, null, this.applicationContext)
        LiveData.session.observeForever { session ->
            sessionSyncManager.setSession(session)
        }
        LiveData.fcmToken.observeForever { token ->
            sessionSyncManager.setFcmToken(token)
        }
    }
}
