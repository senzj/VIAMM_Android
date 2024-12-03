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
    // initiating webview
    private val webView : WebView by lazy { findViewById(R.id.scanner_WV) }

    // permission request code
    private var permissionRequest : PermissionRequest? = null

    // prompt to allow or deny camera access
    private val dialog : AlertDialog.Builder by lazy { AlertDialog.Builder(this).apply {
        setTitle("Camera Permission")
        setMessage("This app has requested camera permission. Would you like to grant it?")
        setPositiveButton(android.R.string.ok) { dialog, _ ->
            permissionRequest?.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
            dialog.dismiss()
        }
        setNegativeButton("Deny"){ dialog, _ ->
            permissionRequest?.deny()
            dialog.dismiss()
        }

    } }

    // launcher
    val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->
        permissionRequest?.apply {
            if (granted){
                dialog.show()
//                grant( arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE) )
            } else {
                deny()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient(){

                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                    // Handle JavaScript alert
                    Toast.makeText(this@ScannerActivity, message, Toast.LENGTH_SHORT).show()
                    result?.confirm() // Confirm the alert
                    return true
                }

                override fun onPermissionRequest(request: PermissionRequest) {

                    // check if devices request for camera
                    if(PermissionRequest.RESOURCE_VIDEO_CAPTURE in request.resources){
                        permissionRequest = request
                        launcher.launch(android.Manifest.permission.CAMERA)
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
