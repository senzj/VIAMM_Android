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
    private val webView: WebView by lazy { findViewById(R.id.scanner_WV) }

    private var permissionRequest: PermissionRequest? = null

    // Launchers to handle permission requests
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission granted, show the WebView content
            webView.reload()
        } else {
            // Permission denied
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog for granting camera permission
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(this).apply {
            setTitle("Camera Permission")
            setMessage("This app needs camera access. Would you like to grant it?")
            setPositiveButton("Grant") { _, _ ->
                permissionRequest?.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
            }
            setNegativeButton("Deny") { _, _ ->
                permissionRequest?.deny()
            }
        }.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        Log.d(TAG, "onCreate: Initializing ScannerActivity")

        webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient(){

                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                    Toast.makeText(this@ScannerActivity, message, Toast.LENGTH_SHORT).show()
                    result?.confirm() // Confirm the alert
                    return true
                }

                override fun onPermissionRequest(request: PermissionRequest) {
                    if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in request.resources) {
                        permissionRequest = request
                        // Request camera permission
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }

            settings.apply {
                javaScriptEnabled = true // Enable JavaScript
                domStorageEnabled = true // Enable DOM storage
                mediaPlaybackRequiresUserGesture = false // Allow media playback without user gestures
            }

            settings.javaScriptEnabled = true
            var URL = "http://192.168.254.105/Capstoneproject_web/mobile/camera"
            var testURL = "https://webcamtests.com/"
            loadUrl(URL)
        }



        // Retrieve intent extras
        val bookingId = intent.getIntExtra("booking_id", 0)
        val masseur = intent.getStringExtra("masseur") ?: ""
        val workstation = intent.getStringExtra("workstation") ?: ""


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
