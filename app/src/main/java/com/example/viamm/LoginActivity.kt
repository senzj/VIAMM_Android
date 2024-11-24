package com.example.viamm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.example.viamm.api.RetrofitClient
import com.example.viamm.models.Login.LoginResponse
import com.example.viamm.storage.SharedData
import com.example.viamm.loadings.LoginLoading
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.UnknownHostException
import java.util.Locale

class LoginActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var loginBtn: Button
    private lateinit var CompName: TextInputEditText
    private lateinit var CompPass: TextInputEditText
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
        CompName = findViewById(R.id.input_username)
        CompPass = findViewById(R.id.input_password)

        // Set up focus change listeners for both EditText
        // This makes that when the user taps outside the EditText, the keyboard disappears
        setupFocusChangeListener(CompName)
        setupFocusChangeListener(CompPass)

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        setHoverListener(loginBtn,"Login")

        loginBtn.setOnClickListener {
            textToSpeech("Logging in")

            val username = CompName.text.toString().trim()
            val password = CompPass.text.toString().trim()

            if (username.isEmpty()) {
                CompName.error = "Please enter your username"
                CompName.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                CompPass.error = "Please enter your password"
                CompPass.requestFocus()
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

                        try {
                            Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                            Log.e("LoginActivity > Retrofit", "onFailure: ${t.message}", t)
                        } catch (e: UnknownHostException) {
                            // This block specifically handles UnknownHostException
                            Toast.makeText(applicationContext, "No internet connection, please connect to the internet", Toast.LENGTH_LONG).show()
                            Log.e("LoginActivity to Retrofit", "onFailure: ${e.message}", e)
                        } catch (e: Exception) {
                            // This block handles other exceptions
                            Toast.makeText(applicationContext, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("LoginActivity to Retrofit", "onFailure: ${e.message}", e)
                        }

                    }

                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        // Dismiss loading dialog on response
                        loadingDialog.dismiss()

                        if (response.isSuccessful) {
                            response.body()?.let { loginResponse ->
                                if (!loginResponse.error) {
                                    SharedData.getInstance(applicationContext).saveUser(loginResponse.user)
                                    SharedData.getInstance(applicationContext).isLoggedIn = true

                                    val intent = Intent(applicationContext, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)

                                    Toast.makeText(applicationContext, loginResponse.message, Toast.LENGTH_LONG).show()
                                    Log.i("LoginActivity", "Login Success!")

                                } else {
                                    // Show the server error message
                                    Toast.makeText(applicationContext, loginResponse.message, Toast.LENGTH_LONG).show()
                                    Log.e("LoginActivity", "An Error Occurred: ${loginResponse.message}")
                                }

                            } ?: run {
                                Toast.makeText(applicationContext, "Unknown error occurred", Toast.LENGTH_LONG).show()
                                Log.e("LoginActivity", "Fatal Error: Unknown error occurred")
                            }

                        } else {
                            // Handle different HTTP response codes
                            when (response.code()) {
                                401 -> {
                                    val errorMessage = "Incorrect Username or Password"
                                    // Unauthorized - Incorrect credentials
                                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                                    Log.e("LoginActivity", "Failed: $errorMessage")
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
                })
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

    //  When the focus changes or clicked anywhere, hide the keyboard
    private fun setupFocusChangeListener(view: View) {
        view.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
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
    private fun setHoverListener(button: Button, text: String) {
        button.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER -> {
                    // check if the button is clicked and speaking
                    if (!isClicked || !isSpeaking){
                        isSpeaking = true
                        Log.d("LoginActivity", "Text to Speech Button Pressed")
                        textToSpeech(text)
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
        }
        Handler().postDelayed({
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }, 525) // Adjust delay (300 milliseconds here) as per your preference
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

}
