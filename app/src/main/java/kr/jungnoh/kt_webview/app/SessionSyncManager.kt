package kr.jungnoh.kt_webview.app

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SessionSyncManager(private var cookie: String?, private var fcmToken: String?, context: Context) {
    private val requestQueue = Volley.newRequestQueue(context)
    private fun syncTokens() {
        if (cookie == null || fcmToken == null) {
            return
        }
        val payload = HashMap<String, String>()
        payload["token"] = fcmToken!!
        val errorCallback = Response.ErrorListener { error -> Log.d("SESSION", error.toString()) }

        val request = CookieJSONRequest(
            Request.Method.POST,
            "${Config.host}/token",
            JSONObject(payload as Map<*, *>),
            Response.Listener {},
            errorCallback
        )
        request.setCookies(listOf("${Config.sessionKey}=$cookie"))
        requestQueue.add(request)
    }
    fun setSession(cookie: String?) {
        if ((cookie ?: "").compareTo(this.cookie ?: ".") != 0) {
            this.cookie = cookie
            syncTokens()
        } else {
            this.cookie = cookie
        }
    }
    fun setFcmToken(fcmToken: String?) {
        if ((fcmToken ?: "").compareTo(this.fcmToken ?: "") != 0) {
            this.fcmToken = fcmToken
            syncTokens()
        } else {
            this.fcmToken = fcmToken
        }
    }
    init {
        syncTokens()
    }
}
