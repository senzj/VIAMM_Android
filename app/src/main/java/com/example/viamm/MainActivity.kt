package com.example.viamm

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.viamm.databinding.ActivityMainBinding
import com.example.viamm.storage.SharedData
import java.util.Locale
import kotlinx.coroutines.*

//unused imports
//import android.os.Handler
//import android.os.Looper
//import androidx.constraintlayout.widget.ConstraintSet.Motion
//import androidx.annotation.RequiresApi


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // Initializing variables
    private lateinit var binding: ActivityMainBinding
    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false



    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get permissions
        getPermission()

        // Make the activity fullscreen and draw behind the status bar
        // Set the content view first, then configure window insets for fullscreen mode
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and above: Use the WindowInsetsController API
            val windowInsetsController = window.insetsController
            windowInsetsController?.hide(WindowInsets.Type.statusBars())
            windowInsetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
        } else {
            // Android 10 (API 29) and below: Use the old method
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }



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



        // Initialize user welcome TextView
        val welcomeTextView: TextView? = binding.tvWelcome

        // Fetch the session_user username from SharedPreferences
        val sharedPref = getSharedPreferences("session", Context.MODE_PRIVATE)  // Ensure you're using "user_session"
        Log.d("MainActivity", "Session user: : $sharedPref")

        // Default to "VIAMM" if no username is found
        val username = sharedPref.getString("session_user", "VIAMM") // Use "session_user" key to fetch
        Log.d("MainActivity", "Session user: : $username")

        // Update the TextView with a personalized welcome message
        welcomeTextView?.text = "Welcome to VIAMM, $username!"


        // Variable declaration for button click timing
        val holdDuration = 350L // Duration in milliseconds to consider as a hold
        val clickDelay = 400L // Delay in milliseconds for double-click detection
        var lastClickTime = 0L
        var isDoubleClick = false
        var isHolding = false

        // Function to handle touch events
        fun handleTouchEvent(
            event: MotionEvent,
            buttonName: String,
            // Callbacks for the following actions
            onSingleClick: () -> Unit,
            onDoubleClick: () -> Unit,
            onHold: () -> Unit
        ): Boolean {
            // Check the type of touch action performed by the user
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Reset hold flag when the button is pressed down
                    isHolding = false

                    // Start a delayed handler to detect a hold action
                    lifecycleScope.launch {
                        delay(holdDuration) // Time to wait before considering it a hold
                        if (!isDoubleClick && !isHolding && event.action != MotionEvent.ACTION_UP) {
                            // If not a double-click and the button remains pressed, mark as holding
                            isHolding = true
                            onHold() // Trigger the hold callback
                        }
                    }
                    return true // Indicate that the ACTION_DOWN event is handled
                }

                MotionEvent.ACTION_UP -> {
                    val currentTime = System.currentTimeMillis()

                    // Check if the button was held
                    if (isHolding) {
                        // If the button was held, ignore click events
                        isHolding = false
                    } else {
                        if (currentTime - lastClickTime < clickDelay) {
                            // If the time difference is within the double-click threshold
                            isDoubleClick = true
                            onDoubleClick() // Trigger the double-click callback
                        } else {
                            // Otherwise, treat it as a single click
                            isDoubleClick = false
                            lastClickTime = currentTime

                            lifecycleScope.launch {
                                delay(clickDelay) // Wait for double-click detection before firing single click
                                if (!isDoubleClick) {
                                    onSingleClick() // Trigger the single-click callback
                                }
                            }
                        }
                    }

                    // Update the last click time to the current time
                    lastClickTime = currentTime
                }
            }
            return false // Return false to allow further event propagation if needed
        }

// Set onTouchListeners for buttons
        orderBtn.setOnTouchListener { _, event ->
            val button = "Booking"

            handleTouchEvent(
                event,
                "Booking",
                onSingleClick = {
                    textToSpeech.stop()
                    textToSpeech("You have tapped on $button")
                    Log.d("MainActivity", "Tapped $button")
                },
                onDoubleClick = {
                    textToSpeech.stop()
                    textToSpeech("You've selected $button. Redirecting now to $button.")

                    // add a delay to redirecting to finish the tts
                    CoroutineScope(Dispatchers.Main).launch {
                        // Delay for 3 seconds
                        delay(2000)

                        // Code to execute after the delay
                        Log.d("MainActivity", "Proceeding to next step after TTS delay")

                        // Start the next activity
                        redirectToOrder()
                    }
                    Log.d("MainActivity", "Double Tapped $button")
                },
                onHold = {
                    textToSpeech.stop()
                    textToSpeech("Holding $button button.")
                    Log.d("MainActivity", "Holding $button")
                }
            )
        }

        recordBtn.setOnTouchListener { _, event ->
            val button = "Records"

            handleTouchEvent(
                event,
                "Record",
                onSingleClick = {
                    textToSpeech.stop()
                    textToSpeech("You have tapped on $button")
                    Log.d("MainActivity", "Tapped $button")
                },
                onDoubleClick = {
                    textToSpeech.stop()
                    textToSpeech("You've selected $button. Redirecting now to $button.")

                    // add a delay to redirecting to finish the tts
                    CoroutineScope(Dispatchers.Main).launch {
                        // Delay for 3 seconds
                        delay(2000)

                        // Code to execute after the delay
                        Log.d("MainActivity", "Proceeding to next step after TTS delay")

                        // Start the next activity
                        redirectToRecord()
                    }
                    Log.d("MainActivity", "Double Tapped $button")
                },
                onHold = {
                    textToSpeech.stop()
                    textToSpeech("Holding $button button.")
                    Log.d("MainActivity", "Holding $button")
                }
            )
        }

        statisticsBtn.setOnTouchListener { _, event ->
            val button = "Analytics"

            handleTouchEvent(
                event,
                "Analytics",
                onSingleClick = {
                    textToSpeech.stop()
                    textToSpeech("You have tapped on $button")
                    Log.d("MainActivity", "Tapped $button")
                },
                onDoubleClick = {
                    textToSpeech.stop()
                    textToSpeech("You've selected $button. Redirecting now to $button.")

                    // add a delay to redirecting to finish the tts
                    // add a delay to redirecting to finish the tts
                    CoroutineScope(Dispatchers.Main).launch {
                        // Delay for 3 seconds
                        delay(2000)

                        // Code to execute after the delay
                        Log.d("MainActivity", "Proceeding to next step after TTS delay")

                        // Start the next activity
                         redirectToAnalytics()
                    }
                    Log.d("MainActivity", "Double Tapped $button")
                },
                onHold = {
                    textToSpeech.stop()
                    textToSpeech("Holding $button button.")
                    Log.d("MainActivity", "Holding $button")
                }
            )
        }

    }

//  Override functions
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
        menu?.findItem(R.id.btn_scanner)?.isVisible = false
        return true
    }

    // Action bar item selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // item menu hover text is impossible cuz there is no hover event
            R.id.btn_logout -> {
                textToSpeech("Logging out")

                // add a delay to redirecting to finish the tts
                lifecycleScope.launch {
                    delay(1000) // Adjust delay time in milliseconds
                    logout()
                }
                true
            }
//            R.id.btn_scanner -> {
//                textToSpeech("Money Scanner")
//                redirectToScanner()
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }


// Redirecting to other activity
    // Order activity
    private fun redirectToOrder() {
        val intent = Intent(applicationContext, OrderActivity::class.java)
        startActivity(intent)
    }

    // Records activity
    private fun redirectToRecord() {
        val intent = Intent(applicationContext, RecordActivity::class.java)
        startActivity(intent)
    }

    // Analytics activity
    private fun redirectToAnalytics() {
//        val intent = Intent(applicationContext, StatisticsActivity::class.java)
//        startActivity(intent)
    }

    // Function to go Scanner Activity
//    private fun redirectToScanner() {
//        val intent = Intent(applicationContext, ScannerActivity::class.java)
//        startActivity(intent)
//    }


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
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        Log.d("MainActivity", "Text to Speech Button Triggered")
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
                        Log.d("MainActivity", "Text to Speech Button Pressed")
                        textToSpeech(text)
                        Log.d("MainActivity", "Text to Speech Button Triggered")
                    }
                }

                MotionEvent.ACTION_UP -> {
                    textToSpeech.stop()
                    isClicked = false
                    Log.d("MainActivity", "Text to Speech Button Unpressed")

                }

                MotionEvent.ACTION_HOVER_EXIT -> {
                    isClicked = false
                    Log.d("MainActivity", "Text to Speech Hover Exit")
                }
            }
            false // return false to let other touch events like click still work
        }
    }

//  Exit app functions
    // Logout
    private fun logout() {
        // Set the user as logged out
        SharedData.getInstance(this).isLoggedIn = false

        // Clear session data from SharedPreferences
        sessionDestroy()

        lifecycleScope.launch {
            // Delay for 1 second
            delay(1000)

            // Redirect to LoginActivity
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Start the LoginActivity
            startActivity(intent)
            finish()  // Finish the current activity

            // Stop any ongoing TTS (Text-to-Speech) if needed
            textToSpeech.stop()

            // Show logout success message
            Toast.makeText(this@MainActivity, "Logged out successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        sessionDestroy() // Destroying or clearing saved user info
        super.onDestroy() // Call the super method first
        textToSpeech.stop()
    }

    private fun sessionDestroy(){
        // Get the SharedPreferences instance where user data is saved
        val sharedPref = getSharedPreferences("session", Context.MODE_PRIVATE)

        // Use the editor to remove the specific keys (like the session or user data)
        with(sharedPref.edit()) {
            remove("session_user")  // Remove the username key
            remove("isLoggedIn")    // Remove the login status key if saved
            clear()  // This clears all keys and values
            apply()  // Don't forget to apply changes
            Log.d("MainActivity", "User Session cleared.")
        }
    }

// End of MainActivity
}
