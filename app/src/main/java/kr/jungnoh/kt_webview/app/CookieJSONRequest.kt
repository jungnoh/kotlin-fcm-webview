package kr.jungnoh.kt_webview.app

import com.android.volley.AuthFailureError
import com.android.volley.Response
import org.json.JSONObject
import com.android.volley.toolbox.JsonObjectRequest


class CookieJSONRequest
    (
    method: Int, url: String, jsonRequest: JSONObject,
    listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener
) : JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {

    private val headers = HashMap<String, String>()

    /**
     * Custom class!
     */
    fun setCookies(cookies: List<String>) {
        val sb = StringBuilder()
        for (cookie in cookies) {
            sb.append(cookie).append("; ")
        }
        headers["Cookie"] = sb.toString()
    }

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return headers
    }

}