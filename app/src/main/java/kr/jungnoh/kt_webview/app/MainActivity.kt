package kr.jungnoh.kt_webview.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import android.webkit.*
import java.io.File


class MainActivity : AppCompatActivity() {
    var session: Session? = null
    val fcm = MyFCMService()
    var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    var mUploadMessage: ValueCallback<Uri>? = null
    val FILECHOOSER_RESULTCODE = 1

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        val wv = findViewById<WebView>(R.id.main_webview)
        wv.settings.setSupportMultipleWindows(true)
        wv.settings.javaScriptEnabled = true
        wv.settings.javaScriptCanOpenWindowsAutomatically = true
        wv.settings.loadsImagesAutomatically = true
        wv.settings.setAppCacheEnabled(true)
        wv.settings.setSupportZoom(false)
        wv.settings.useWideViewPort = true
        wv.settings.domStorageEnabled = true
        wv.settings.allowFileAccess = true
        wv.isClickable = true
        wv.settings.userAgentString = Config.userAgent
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val cookies = parseCookie(CookieManager.getInstance().getCookie(Config.host))
                if (cookies != null) {
                    session?.setSession(cookies)
                }
                CookieManager.getInstance().flush()
            }
        }
        wv.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("알림")
                builder.setMessage(message)
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    result?.confirm()
                }
                builder.setCancelable(false)
                builder.create().show()
                return true
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("알림")
                builder.setMessage(message)
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    result?.confirm()
                }
                builder.setNegativeButton(android.R.string.no) { _, _ ->
                    result?.cancel()
                }
                builder.setCancelable(false)
                builder.create().show()
                return true
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (mFilePathCallback != null) {
                    mFilePathCallback?.onReceiveValue(null)
                    mFilePathCallback = null
                }
                mFilePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                intent?.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
                try {
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE)
                } catch (e: ActivityNotFoundException) {
                    mFilePathCallback = null
                    Toast.makeText(this@MainActivity, "Cannot open file chooser", Toast.LENGTH_LONG).show()
                    return false
                }
                return true
            }
        }
        val url = intent.getStringExtra("page") ?: Config.host
//      Log.i("MainActivity", intent.extras.toString())
        wv.loadUrl(url)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("MainActivity", "OnActivityResult")
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mFilePathCallback == null || data == null || resultCode != Activity.RESULT_OK) return
            val myList: MutableList<Uri> = mutableListOf()
            if (data.clipData != null) {
                val count = data.clipData?.itemCount
                var currentItem = 0
                while(currentItem < (count ?: 0)) {
                    val imageUri = data.clipData?.getItemAt(currentItem)?.uri
                    if (imageUri != null) {
                        myList.add(imageUri)
                    }
                    currentItem++
                }
            } else {
                val path = data.data?.path
                if (path != null) {
                    myList.add(Uri.parse(path))
                }
            }
            mFilePathCallback?.onReceiveValue(myList.toTypedArray())
            mFilePathCallback = null
            // single: resultCode -1, mData: Uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // WebView debug
        WebView.setWebContentsDebuggingEnabled(true) // KitKat~
        if(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
        }
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        setContentView(R.layout.activity_main)
        setupWebview()
        session = Session(null, null, this)
        fcm.onChange = {key -> session?.setFcmToken(key)}
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
//                    Log.w("FCM", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                this.session?.setFcmToken(task.result?.token)
            })
    }

    var backKeyPressedTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val wv = findViewById<WebView>(R.id.main_webview)
            if (wv.url != Config.host) {
                wv.goBack()
                return true
            }
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis()
                Toast.makeText(this, getString(R.string.APP_CLOSE_BACK_BUTTON), Toast.LENGTH_SHORT)
                    .show()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun parseCookie(cookie: String?): String? {
        if (cookie == null) {
            return null
        }
        val items = cookie.split(";")
        for (item: String in items) {
            val parts = item.split('=')
            if (parts.size < 2) {
                continue
            }
            if (parts[0].trim().compareTo("connect.sid") == 0) {
                return parts[1].trim()
            }
        }
        return null
    }

    fun onPrevClick(view: View) {
        val wv = findViewById<WebView>(R.id.main_webview)
        wv.goBack()
    }
    fun onNextClick(view: View) {
        val wv = findViewById<WebView>(R.id.main_webview)
        wv.goForward()
    }
    fun onHomeClick(view: View) {
        val wv = findViewById<WebView>(R.id.main_webview)
        wv.loadUrl(Config.host)
    }
    fun onRefreshClick(view: View) {
        val wv = findViewById<WebView>(R.id.main_webview)
        wv.reload()
    }
}
