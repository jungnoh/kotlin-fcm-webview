package kr.jungnoh.kt_webview.app

import androidx.lifecycle.MutableLiveData


class LiveData {
    companion object {
        val session: MutableLiveData<String> = MutableLiveData()
        val fcmToken: MutableLiveData<String> = MutableLiveData()
    }
}