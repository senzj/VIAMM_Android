package com.example.viamm

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ScannerActivity : AppCompatActivity() {
    // WebView instance
    private val webView: WebView by lazy { findViewById(R.id.scanner_WV) }

    // Permission request
    private var permissionRequest: PermissionRequest? = null

    // AlertDialog for permission requests
    private val dialog: AlertDialog.Builder by lazy {
        AlertDialog.Builder(this).apply {
            setTitle("Permission Request")
            setPositiveButton("Allow") { _, _ ->
                Log.d(TAG, "Permission granted via dialog")
                permissionRequest?.grant(permissionRequest?.resources ?: emptyArray())
            }
            setNegativeButton("Deny") { _, _ ->
                Log.d(TAG, "Permission denied via dialog")
                permissionRequest?.deny()
            }
        }
    }

    // Permission launcher
    private val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionRequest?.let {
            if (granted) {
                Log.d(TAG, "Permission granted via launcher")
                it.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
            } else {
                Log.d(TAG, "Permission denied via launcher")
                it.deny()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        Log.d(TAG, "onCreate: Initializing ScannerActivity")

        // Configure WebView
        configureWebView()

        // Load your URL
        val url = "https://viamm.xyz/mobile/camera"
        Log.d(TAG, "Loading URL: $url")
        webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        Log.d(TAG, "Configuring WebView settings")
        webView.apply {
            // Clear cache and history to ensure fresh loading
            clearCache(true)
            clearFormData()
            clearHistory()

            settings.apply {
                // Disable cache to force WebView to load fresh content
                cacheMode = WebSettings.LOAD_NO_CACHE
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
            }

            // WebChromeClient to handle permission requests
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    Log.d(TAG, "Permission request received: ${request.resources.joinToString()}")
                    permissionRequest = request
                    if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in request.resources) {
                        Log.d(TAG, "Requesting camera permission")
                        launcher.launch(Manifest.permission.CAMERA)
                    } else {
                        // Show dialog for other permissions
                        Log.d(TAG, "Showing dialog for additional permissions")
                        dialog.setMessage("The app is requesting additional permissions.")
                        dialog.show()
                    }
                }

                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d(TAG, "Web Console: ${it.message()} [${it.sourceId()}:${it.lineNumber()}]")
                    }
                    return super.onConsoleMessage(consoleMessage)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Cleaning up resources")
        webView.destroy()  // Release WebView resources
        finish()
    }

    companion object {
        private const val TAG = "ScannerActivity"
    }
}
