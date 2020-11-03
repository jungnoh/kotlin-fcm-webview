package kr.jungnoh.kt_webview.app

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData

class SessionLiveData(private val context: Context) : MutableLiveData<String>() {
    companion object {
        private var instance: SessionLiveData? = null

        @MainThread
        fun get(context: Context): SessionLiveData {
            if (instance == null) {
                instance = SessionLiveData(context.applicationContext)
            }
            return instance!!
        }
    }
}