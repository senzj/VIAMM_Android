package com.example.viamm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.databinding.ActivityMainBinding
import com.example.viamm.storage.SharedData

class MainActivity : AppCompatActivity() {

    // Initializing variables
    private lateinit var binding: ActivityMainBinding

//    Activity lifecycles ==========================================================================

//    OnCreate function of MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize toolbar
        val toolbar: Toolbar? = binding.toolbar
        setSupportActionBar(toolbar)

        // Initialize buttons
        val orderBtn: Button = binding.btnOrder
        val recordBtn: Button = binding.btnRecord
        val btn_statistics : Button = binding.btnStatistics

        // Set onClickListeners for Order button
        orderBtn.setOnClickListener {
            redirectToOrder()
        }

        // Set onClickListeners for Records button
        recordBtn.setOnClickListener {
            redirectToRecord()
        }

        // Set onClickListeners for Statistics button
        btn_statistics.setOnClickListener {
            redirectToStatistics()
        }

    }

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
            R.id.btn_logout -> {
                logout()
                true
            }

            R.id.btn_scanner -> {
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

    //  Function to go Scanner Activity
    private fun redirectToScanner() {
        val intent = Intent(applicationContext, ScannerActivity::class.java)
        startActivity(intent)
    }

//    End of MainActivity ==========================================================================

}
