package com.example.viamm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.databinding.ActivityOrderBinding
import com.example.viamm.storage.SharedData

class OrderActivity : AppCompatActivity() {

    // Initializing variables
    private lateinit var binding: ActivityOrderBinding

//    Activity lifecycles ==========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate the layout using ViewBinding
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inflate the toolbar
        setSupportActionBar(binding.toolbar)

        // Apply window insets listener to the root layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
            else -> super.onOptionsItemSelected(item)
        }
    }


//    Other functions ==============================================================================

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



//    End of Order Activity ======================================================================
}
