package com.example.viamm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
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
//import android.annotation.SuppressLint

class LoginActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var loginBtn: Button
    private lateinit var compName: TextInputEditText
    private lateinit var compPass: TextInputEditText
    private lateinit var loadingDialog: LoginLoading
    private lateinit var textToSpeech: TextToSpeech
    private var isTTSInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // loading dialogs
        loadingDialog = LoginLoading(this)

        loginBtn = findViewById(R.id.btn_login)
        compName = findViewById(R.id.input_username)
        compPass = findViewById(R.id.input_password)

        // Set up focus listeners for EditText
        setupFocusChangeListener(compName) // Hide keyboard when tapping outside this field
        setupFocusChangeListener(compPass) // Hide keyboard when tapping outside this field

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        // login button onclick
        loginBtn.setOnClickListener {
            // trim input text to prevent leading or trailing spaces
            val username = compName.text.toString().trim()
            val password = compPass.text.toString().trim()

            if (username.isNotEmpty()) {
                speakText("Logging in as, $username")
                Log.d("LoginActivity", "Logging in as, $username")
            } else {
                speakText("Username or password cannot be empty.")
                Log.e("LoginActivity", "Username is empty.")
            }

            // error messages if fields are empty
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
            Log.d("LoginActivity", "Attempting login with username: $username")

            // pass the data to retrofit then pass to server
            RetrofitClient.instance.login(username, password)
                .enqueue(object : Callback<LoginResponse> {

                    // Triggered when a network error occurs like no internet, timeout, or server not reachable
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        loadingDialog.dismiss()
                        handleNetworkFailure(t)
                        Log.e("LoginActivity", "Network failure: ${t.message}")
                        Toast.makeText(this@LoginActivity, "Failed to Login.", Toast.LENGTH_SHORT).show()
                    }

                    // Triggered when the server sends a response (successful or error)
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        handleLoginResponse(response, username)
                    }
                })
        }

        // Keeps the keyboard open when the next button(next arrow) in keyboard is clicked
        compName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                compPass.requestFocus()
                true
            } else {
                false
            }
        }

        // Closes keyboard when the "Done" button (check) is pressed in keyboard
        compPass.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginBtn.performClick() // Simulate clicking the login button
                true // Indicate that the event was handled
            } else {
                false // Allow default behavior for other actions
            }
        }

        findViewById<View>(R.id.login).setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                currentFocus?.let { view ->
                    view.clearFocus()
                    hideKeyboard(view)
                }
            }
            v.performClick()
            false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            isTTSInitialized = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            speakText("Welcome to Vaiyam, please log in to continue.")

            if (!isTTSInitialized) {
                Log.e("LoginActivity", "TextToSpeech language not supported")
                Toast.makeText(this, "TTS language not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("LoginActivity", "TextToSpeech initialization failed")
        }
    }

    // check if text to speech is initialized
    private fun speakText(text: String) {
        if (isTTSInitialized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("LoginActivity", "TTS triggered.\ntext: $text")
        } else {
            Log.e("LoginActivity", "TextToSpeech not initialized or supported")
        }
    }

    // System errors or failures
    private fun handleNetworkFailure(t: Throwable) {
        when (t) {
            is UnknownHostException -> {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
                Log.e("LoginActivity", "No internet connection", t)
            }
            is IOException -> {
                Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show()
                Log.e("LoginActivity", "Network error: ${t.message}\n", t)
            }
            else -> {
                Toast.makeText(this, "An error occurred: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("LoginActivity", "Error: ${t.message}", t)
            }
        }
    }

    private fun handleLoginResponse(response: Response<LoginResponse>, username: String) {
        lifecycleScope.launch {
            delay(2000) // Add a delay before processing the response
            loadingDialog.dismiss()

            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    if (!loginResponse.error) {
                        // Save user data to SharedPreferences
                        SharedData.getInstance(this@LoginActivity).saveUser(loginResponse.user)
                        SharedData.getInstance(this@LoginActivity).isLoggedIn = true

                        // saving user data in session
                        saveUserData(applicationContext, "session_user", username)
                        speakText("Login Success! Welcome to Vaiyam, $username")

                        Log.d("LoginActivity", "Login Success!")

                        // next activity
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_LONG).show()
                        Log.e("LoginActivity", loginResponse.message)
                    }
                } ?: run {
                    Toast.makeText(this@LoginActivity, "Unknown error occurred", Toast.LENGTH_LONG).show()
                    Log.e("LoginActivity", "Null response body")
                }
            } else {
                // server response codes
                when (response.code()) {
                    401 -> {
                        val errorMessage = "Invalid username or password"
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                        speakText(errorMessage)
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@LoginActivity, "Error: $errorBody", Toast.LENGTH_LONG).show()
                        Log.e("LoginActivity", "Error: $errorBody")
                    }
                }
            }
        }
    }

    // Set up focus listeners for EditText
    private fun setupFocusChangeListener(view: View) {
        view.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        }
    }

    // Hide the keyboard
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
            Log.d("LoginActivity", "TextToSpeech shutdown")
        }
        super.onDestroy()
        Log.d("LoginActivity", "Activity destroyed")
    }

    // Save user data to SharedPreferences
    private fun saveUserData(context: Context, key: String, value: String) {
        val sharedPref = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }
}

