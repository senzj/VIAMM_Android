package com.example.viamm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.api.RetrofitClient
import com.example.viamm.models.Login.LoginResponse
import com.example.viamm.storage.SharedData
import com.example.viamm.loadings.LoadingDialog
import com.example.viamm.loadings.LoginLoading
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var loginBtn: Button
    private lateinit var CompName: TextInputEditText
    private lateinit var CompPass: TextInputEditText
    private lateinit var loadingDialog: LoginLoading

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

        loginBtn.setOnClickListener {
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

                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                        Log.e("Login", "onFailure: ${t.message}", t)
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
                                    Log.i("Login", "Login Success!")

                                } else {
                                    // Show the server error message
                                    Toast.makeText(applicationContext, loginResponse.message, Toast.LENGTH_LONG).show()
                                    Log.e("Login", "An Error Occurred: ${loginResponse.message}")
                                }

                            } ?: run {
                                Toast.makeText(applicationContext, "Unknown error occurred", Toast.LENGTH_LONG).show()
                                Log.e("Login", "Fatal Error: Unknown error occurred")
                            }

                        } else {
                            // Handle different HTTP response codes
                            when (response.code()) {
                                401 -> {
                                    val errorMessage = "Incorrect Username or Password"
                                    // Unauthorized - Incorrect credentials
                                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                                    Log.e("Login", "Failed: $errorMessage")
                                }
                                else -> {
                                    // Other errors
                                    val errorBody = response.errorBody()?.string()
                                    Toast.makeText(applicationContext, "Unknown error occurred: $errorBody", Toast.LENGTH_LONG).show()
                                    Log.e("Login", "Failed: $errorBody")
                                }
                            }
                        }
                    }
                })
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already logged in
        if (SharedData.getInstance(this).isLoggedIn) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Log.i("Login", "Already Logged in")
        }
    }
}
