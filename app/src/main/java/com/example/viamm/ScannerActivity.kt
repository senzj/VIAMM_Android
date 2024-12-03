package com.example.viamm

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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


    }


}
