package kr.jungnoh.kt_webview.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import android.webkit.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {
    lateinit var sessionSyncManager: SessionSyncManager
    var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    // ResultCode of file chooser
    val mFileChooserResultCode = 801
    // Timestamp of when the back key was last pressed.
    private var backKeyPressedTime: Long = 0

    fun updateSessionToken(session: String) {
        LiveData.session.value = session
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView() {
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
                val cookie = extractCookie(CookieManager.getInstance().getCookie(Config.host))
                cookie?.let {
                    updateSessionToken(cookie)
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
                    startActivityForResult(intent, mFileChooserResultCode)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // WebView debug
        WebView.setWebContentsDebuggingEnabled(true) // KitKat~
        if(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
        }
        actionBar?.hide()
        setContentView(R.layout.activity_main)
        setupWebView()

        sessionSyncManager = SessionSyncManager(null, null, this.applicationContext)
        LiveData.session.observeForever { session ->
            sessionSyncManager.setSession(session)
        }
        LiveData.fcmToken.observeForever { token ->
            sessionSyncManager.setFcmToken(token)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            LiveData.fcmToken.value = token
        })
    }

    // Handler for ActivityResults from file selectors
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != mFileChooserResultCode) {
            return
        }
        if (mFilePathCallback == null || data == null || resultCode != Activity.RESULT_OK) {
            mFilePathCallback = null
            return
        }
        val myList: MutableList<Uri> = mutableListOf()
        if (data.clipData != null) {
            val count = data.clipData?.itemCount ?: 0
            for (index in 0 until count) {
                data.clipData?.getItemAt(index)?.uri?.let { imageUri ->
                    myList.add(imageUri)
                }
            }
        } else {
            data.data?.path?.let { path ->
                myList.add(Uri.parse(path))
            }
        }
        mFilePathCallback?.onReceiveValue(myList.toTypedArray())
        mFilePathCallback = null
    }

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

    @Suppress("UNUSED_PARAMETER")
    fun onPrevClick(view: View) {
        val wv = findViewById<WebView>(R.id.main_webview)
        wv.goBack()
    }
    @Suppress("UNUSED_PARAMETER")
    fun onNextClick(view: View) {
        val wv = findViewById<WebView>(R.id.main_webview)
        wv.goForward()
    }
    @Suppress("UNUSED_PARAMETER")
    fun onHomeClick(view: View) {
        val wv = findViewById<WebView>(R.id.main_webview)
        wv.loadUrl(Config.host)
    }
    @Suppress("UNUSED_PARAMETER")
    fun onRefreshClick(view: View) {
        val wv = findViewById<WebView>(R.id.main_webview)
        wv.reload()
    }
}
