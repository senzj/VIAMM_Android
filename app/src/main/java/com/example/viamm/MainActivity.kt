package com.example.viamm

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintSet.Motion
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.databinding.ActivityMainBinding
import com.example.viamm.storage.SharedData
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // Initializing variables
    private lateinit var binding: ActivityMainBinding
    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false

    // OnCreate function of MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        getPermission()

        // Initialize the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize toolbar
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        // Initialize buttons
        val orderBtn: Button = binding.btnOrder
        val recordBtn: Button = binding.btnRecord
        val statisticsBtn: Button = binding.btnStatistics

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        // Set hover-like listeners on buttons for text to speech
        setHoverListener(orderBtn, "Booking")
        setHoverListener(recordBtn, "Record")
        setHoverListener(statisticsBtn, "Statistics")

        // Set onClickListeners for button here
        orderBtn.setOnClickListener {
            isClicked = true
            textToSpeech.stop()
            redirectToOrder()
            textToSpeech("Booking Selected")
        }

        recordBtn.setOnClickListener {
            isClicked = true
            textToSpeech.stop()
            redirectToRecord()
            textToSpeech("Record Selected")
        }

        statisticsBtn.setOnClickListener {
            isClicked = true
            textToSpeech.stop()
            redirectToStatistics()
            textToSpeech("Statistics Selected")
        }
    }

//  Override functions=============================================================
    // Function lifecycle to check if user is logged in
    override fun onStart() {
        super.onStart()

        if (!SharedData.getInstance(this).isLoggedIn) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Finish the current activity
        }
    }

    // Action bar/menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    // Action bar item selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            // item menu hover text is impossible cuz there is no hover event
            R.id.btn_logout -> {
                textToSpeech("Logging out")
                logout()
                true
            }

            R.id.btn_scanner -> {
                textToSpeech("Money Scanner")
                redirectToScanner()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Other Functions =================================================================================

    // Redirect to order activity
    private fun redirectToOrder() {
        val intent = Intent(applicationContext, OrderActivity::class.java)
        startActivity(intent)
    }

    // Redirect to records activity
    private fun redirectToRecord() {
        val intent = Intent(applicationContext, RecordActivity::class.java)
        startActivity(intent)
    }

    // Redirect to statistics activity
    private fun redirectToStatistics() {
        val intent = Intent(applicationContext, StatisticsActivity::class.java)
        startActivity(intent)
    }

    // Function to handle logout
    private fun logout() {
        SharedData.getInstance(this).isLoggedIn = false

        // Redirect to login activity
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish the current activity
        Toast.makeText(this, "Logged out Successfully!", Toast.LENGTH_SHORT).show()
    }

    // Function to go Scanner Activity
    private fun redirectToScanner() {
        val intent = Intent(applicationContext, ScannerActivity::class.java)
        startActivity(intent)
    }

    // Setting permissions for the camera
    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Required For Money Scanner!", Toast.LENGTH_SHORT).show()
            getPermission()
        }
    }

    override fun onDestroy() {
        super.onDestroy() // Call the super method first
        textToSpeech.stop()
    }

    // Initialize TextToSpeech
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Text to Speech not supported on this device", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Text to Speech Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Text to Speech function
    private fun textToSpeech(text: String) {
        if (!textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        Handler().postDelayed({
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }, 525) // Adjust delay (300 milliseconds here) as per your preference
    }

    // Hold or Hover Listener event for text to speech
    @SuppressLint("ClickableViewAccessibility")
    private fun setHoverListener(button: Button, text: String) {
        button.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER -> {
                    // check if the button is clicked and speaking
                    if (!isClicked || !isSpeaking){
                        isSpeaking = true
                        Log.d("Main Activity", "Text to Speech Button Pressed")
                        textToSpeech(text)
                        Log.d("Main Activity", "Text to Speech Button Triggered")
                    }
                }

                MotionEvent.ACTION_UP -> {
                    textToSpeech.stop()
                    isClicked = false
                    Log.d("Main Activity", "Text to Speech Button Unpressed")

                }

                MotionEvent.ACTION_HOVER_EXIT -> {
                    isClicked = false
                    Log.d("Main Activity", "Text to Speech Hover Exit")
                }
            }
            false // return false to let other touch events like click still work
        }
    }

    // End of MainActivity ==========================================================================
}
