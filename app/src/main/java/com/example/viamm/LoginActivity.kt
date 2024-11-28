package com.example.viamm

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.*
import androidx.lifecycle.*
import com.example.viamm.api.RetrofitClient
import com.example.viamm.models.Login.LoginResponse
import com.example.viamm.storage.SharedData
import com.example.viamm.loadings.LoginLoading
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException
import java.util.Locale


//unused imports
//import android.os.Handler

class LoginActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var loginBtn: Button
    private lateinit var compName: TextInputEditText
    private lateinit var compPass: TextInputEditText
    private lateinit var loadingDialog: LoginLoading
    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadingDialog = LoginLoading(this)

        loginBtn = findViewById(R.id.btn_login)
        compName = findViewById(R.id.input_username)
        compPass = findViewById(R.id.input_password)

        // Set up focus change listeners for both EditText
        // This makes that when the user taps outside the EditText, the keyboard disappears
        setupFocusChangeListener(compName)
        setupFocusChangeListener(compPass)

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        "Login".setHoverListener(loginBtn)

        loginBtn.setOnClickListener {
            val username = compName.text.toString().trim()
            val password = compPass.text.toString().trim()

            if (username.isNotEmpty()) {
                textToSpeech("Logging in as, $username")
            } else {
                textToSpeech("Username or password cannot be empty.")
            }


            // Check if username or password is empty
            if (username.isEmpty()) {
                compName.error = "Please enter your username"
                compName.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                compPass.error = "Please enter your password"
                compPass.requestFocus()
                return@setOnClickListener
            }

            // Show loading dialog
            loadingDialog.show()

            // Log the request details
            Log.d("LoginActivity", "Attempting login with username: $username")

            RetrofitClient.instance.login(username, password)
                .enqueue(object : Callback<LoginResponse> {

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        // Dismiss loading dialog on failure
                        loadingDialog.dismiss()

                        // Handle network or other errors
                        when (t) {
                            is UnknownHostException -> {
                                // No internet connection
                                Toast.makeText(applicationContext, "No internet connection, please connect to the internet", Toast.LENGTH_LONG).show()
                                Log.e("LoginActivity", "onFailure: No internet connection", t)
                            }
                            is IOException -> {
                                // General network failure
                                Toast.makeText(applicationContext, "Network error occurred: ${t.message}", Toast.LENGTH_LONG).show()
                                Log.e("LoginActivity", "onFailure: Network error", t)
                            }
                            else -> {
                                // Handle other exceptions
                                Toast.makeText(applicationContext, "An error occurred, ${t.message}", Toast.LENGTH_LONG).show()
                                Log.e("LoginActivity", "onFailure: ${t.message}", t)
                            }
                        }
                    }

                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        // Use lifecycleScope to launch a coroutine for handling delays
                        lifecycleScope.launch {
                            // Introduce a delay of 2 seconds before proceeding
                            delay(2000)  // 2 seconds delay

                            // Dismiss loading dialog on response
                            loadingDialog.dismiss()

                            if (response.isSuccessful) {
                                // Handle successful response
                                response.body()?.let { loginResponse ->
                                    if (!loginResponse.error) {
                                        // Save user info and set logged in flag
                                        SharedData.getInstance(applicationContext).saveUser(loginResponse.user)
                                        SharedData.getInstance(applicationContext).isLoggedIn = true

                                        // After saving user data in LoginActivity
                                        saveUserData(applicationContext, "session_user", username)

                                        // Debugging to check if the session_username is saved correctly
                                        val sharedPref = getSharedPreferences("session_user", Context.MODE_PRIVATE)
                                        val savedUsername = sharedPref.getString("session_user", "VIAMM")
                                        Log.d("LoginActivity", "Saved username: $savedUsername")  // Check if the value is being saved

                                        // Add a delay of 1 second for TextToSpeech
                                        delay(1000)  // Delay for 1 second

                                        // Perform TextToSpeech
                                        Log.d("LoginActivity", "Welcome to VIAMM, $username")
                                        textToSpeech("Login Success! Welcome to Vaiyam, $username")

                                        // Navigate to MainActivity
                                        val intent = Intent(applicationContext, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)

                                        // Show success message
                                        Toast.makeText(applicationContext, loginResponse.message, Toast.LENGTH_LONG).show()
                                        Log.i("LoginActivity", "Login Success!")
                                    } else {
                                        // Show server error message
                                        Toast.makeText(applicationContext, loginResponse.message, Toast.LENGTH_LONG).show()
                                        Log.e("LoginActivity", "An Error Occurred: ${loginResponse.message}")
                                    }
                                } ?: run {
                                    // If body is null
                                    Toast.makeText(applicationContext, "Unknown error occurred", Toast.LENGTH_LONG).show()
                                    Log.e("LoginActivity", "Fatal Error: Unknown error occurred")
                                }
                            } else {
                                // Handle HTTP response errors like 401
                                when (response.code()) {
                                    401 -> {
                                        val errorMessage = "Invalid username or password, please try again."
                                        // Unauthorized - Incorrect credentials
                                        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                                        Log.e("LoginActivity", "Failed: $errorMessage")

                                        // Add TextToSpeech for 401 response
                                        textToSpeech(errorMessage)
                                    }
                                    else -> {
                                        // Other errors
                                        val errorBody = response.errorBody()?.string()
                                        Toast.makeText(applicationContext, "Unknown error occurred: $errorBody", Toast.LENGTH_LONG).show()
                                        Log.e("LoginActivity", "Failed: $errorBody")
                                    }
                                }
                            }
                        }
                    }
                })
        }

        // Handle the Done action on the password input
        compPass.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                loginBtn.performClick() // Simulate clicking the login button
                true
            } else {
                false
            }
        }

        // Set up the touch listener for the entire layout, to listen for clicks outside the EditText
        findViewById<View>(R.id.login).setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                currentFocus?.let { view ->
                    view.clearFocus()
                    hideKeyboard(view)
                }
            }
            // Ensure that performClick() is called to handle click events
            v.performClick()
            false
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already logged in
        if (SharedData.getInstance(this).isLoggedIn) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Log.i("LoginActivity", "Already Logged in")
        }
    }

    // Initialize TextToSpeech on Init
    override fun onInit(status: Int) {
        // setting up for tts
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("LoginActivity", "TextToSpeech language is not supported on this device.")
            }
        } else {
            Log.e("LoginActivity", "TextToSpeech initialization failed")
        }
    }

    //  When the focus changes or clicked anywhere, hide the keyboard
    private fun setupFocusChangeListener(view: View) {
        view.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                currentFocus?.let { view ->
                    view.clearFocus()
                    hideKeyboard(view)
                }
            } else {
                hideKeyboard(v)
            }
        }
    }

    //  Hide the keyboard function
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Hold or Hover Listener event
    @SuppressLint("ClickableViewAccessibility")
    private fun String.setHoverListener(button: Button) {
        button.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER -> {
                    // check if the button is clicked and speaking
                    if (!isClicked || !isSpeaking){
                        isSpeaking = true
                        Log.d("LoginActivity", "Text to Speech Button Pressed")
                        textToSpeech(this)
                        Log.d("LoginActivity", "Text to Speech Button Triggered")
                    }
                }

                MotionEvent.ACTION_UP -> {
                    textToSpeech.stop()
                    isClicked = false
                    Log.d("LoginActivity", "Text to Speech Button Unpressed")

                }

                MotionEvent.ACTION_HOVER_EXIT -> {
                    isClicked = false
                    Log.d("LoginActivity", "Text to Speech Hover Exit")
                }
            }
            false // return false to let other touch events like click still work
        }
    }

    // Text to Speech function
    private fun textToSpeech(text: String) {
        if (!textToSpeech.isSpeaking) {
            textToSpeech.stop()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            // Delay timer
            delay(530)

            // Code to execute after the delay
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("LoginActivity", "Proceeding to next step after TTS delay")
        }
    }

    // Function to save user data (e.g., session token, username)
    fun saveUserData(context: Context, key: String, value: String) {
        val sharedPref = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

}
