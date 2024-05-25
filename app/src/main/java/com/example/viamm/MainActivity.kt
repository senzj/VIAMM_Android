package com.example.viamm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.databinding.ActivityMainBinding
import com.example.viamm.storage.SharedData

class MainActivity : AppCompatActivity() {

//    Initializing variables
    private lateinit var toolbar: Toolbar
    private lateinit var binding: ActivityMainBinding

    private lateinit var logoutBtn: Button
    private lateinit var orderBtn: Button
    private lateinit var recordBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set ups toolbar
//        toolbar = findViewById(R.id.actionbar)
//        setSupportActionBar(toolbar)

        // Initialize buttons
        orderBtn = findViewById(R.id.btn_order)
        logoutBtn = findViewById(R.id.btn_logout)
        recordBtn = findViewById(R.id.btn_record)

        // Set onClickListeners for Order
        orderBtn.setOnClickListener {
            redirectToOrder()
        }

        // Set onClickListeners for Records
        recordBtn.setOnClickListener {
            redirectToRecord()
        }

        // Set onClickListeners for Logout
        logoutBtn.setOnClickListener {
            logout() // Call the logout function
        }

        // Set insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
//end of onCreate

// ===== Other functions go here =====

// Function to handle logout
    private fun logout() {
        SharedData.getInstance(this).isLoggedIn = false

        // Redirect to login activity
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish the current activity
    }


//    Redirect to order activity
    private fun redirectToOrder() {
        val intent = Intent(applicationContext, OrderActivity::class.java)
        startActivity(intent)
    }

//    Redirect to records activity
    private fun redirectToRecord() {
        val intent = Intent(applicationContext, RecordActivity::class.java)
        startActivity(intent)
    }



    // Function to check if user is logged in
    override fun onStart() {
        super.onStart()

        if (!SharedData.getInstance(this).isLoggedIn) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Finish the current activity
        }
    }
}