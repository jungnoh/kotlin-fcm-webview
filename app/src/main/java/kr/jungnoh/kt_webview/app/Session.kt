package kr.jungnoh.kt_webview.app

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Session(private var cookie: String?, private var fcmToken: String?, context: Context) {
    val reqQueue = Volley.newRequestQueue(context)
    private fun sendSession() {
//        Log.v("SESSION", "Sending token update: $fcmToken")
        val request = CookieJSONRequest(
            Request.Method.POST,
            "${Config.host}/api/me/token",
            JSONObject("{\"key\": \"$fcmToken\"}"),
            Response.Listener { response ->
//                Log.d("SESSION", "Token update succeeded")
//                Log.d("SESSION", response.toString())
            },
            Response.ErrorListener { error -> Log.d("SESSION", error.toString()) }
        )
        request.setCookies(listOf("connect.sid=$cookie"))
        reqQueue.add(request)
    }
    private fun ensureSessionSend() {
        if (cookie != null && fcmToken != null) {
            sendSession()
        }
    }
    fun setSession(cookie: String?) {
        if ((cookie ?: "").compareTo(this.cookie ?: ".") != 0) {
            this.cookie = cookie
            ensureSessionSend()
        } else {
            this.cookie = cookie
        }
    }
    fun setFcmToken(fcmToken: String?) {
        this.fcmToken = fcmToken
        ensureSessionSend()
    }
    init {
        ensureSessionSend()
    }
}
