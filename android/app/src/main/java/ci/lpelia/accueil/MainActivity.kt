package ci.lpelia.accueil

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.webkit.CookieManager
import android.widget.Toast
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.webkit.JavascriptInterface
import java.util.Locale
import java.util.UUID

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var textToSpeech: TextToSpeech
    private var ttsReady = false
    private val homeUrl = "https://lpelia-accueil.onrender.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)
        webView.settings.javaScriptEnabled = true
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.FRANCE)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
        webView.addJavascriptInterface(NativeVoice(), "LpeliaNative")
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    if (::webView.isInitialized) {
                        webView.evaluateJavascript(
                            "window.onLpeliaNativeSpeechDone && window.onLpeliaNativeSpeechDone();",
                            null
                        )
                    }
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onDone(utteranceId)
            }
        })
        // Keep the APK aligned with the live website: avoid stale cached HTML/JS.
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.javaScriptCanOpenWindowsAutomatically = false
        webView.settings.setSupportMultipleWindows(false)
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false)
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.settings.mediaPlaybackRequiresUserGesture = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                return uri.scheme != "https" || uri.host != "lpelia-accueil.onrender.com"
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread {
                    val audio = request.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                    val allowed = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (audio && allowed) request.grant(arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) else {
                        request.deny()
                        if (audio) askForMicrophone()
                    }
                }
            }
        }
        if (savedInstanceState == null) webView.loadUrl(homeUrl) else webView.restoreState(savedInstanceState)
    }

    inner class NativeVoice {
        @JavascriptInterface
        fun speak(text: String) {
            if (!ttsReady || text.isBlank()) return
            val utteranceId = UUID.randomUUID().toString()
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
        }

        @JavascriptInterface
        fun stop() {
            if (::textToSpeech.isInitialized) textToSpeech.stop()
        }

        @JavascriptInterface
        fun available(): Boolean = ttsReady
    }

    private fun askForMicrophone() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            webView.reload()
        } else if (requestCode == 200) {
            Toast.makeText(this, "Autorisez le micro pour utiliser les fonctions vocales.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (this::webView.isInitialized) webView.destroy()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}
