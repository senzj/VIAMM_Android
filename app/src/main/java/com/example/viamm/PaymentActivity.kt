package com.example.viamm

import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityPaymentBinding
import java.util.Locale

class PaymentActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    //Variables
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var api: Api

    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false
    private var isTTSInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the Retrofit API client
        api = RetrofitClient.instance

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        // Initializing text to speech
        textToSpeech = TextToSpeech(this, this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }

    //text to speech functions
    // Initialize TextToSpeech
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Text to Speech not supported on this device", Toast.LENGTH_SHORT).show()
            } else {
                isTTSInitialized = true
            }
        } else {
            Toast.makeText(this, "Text to Speech Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Text to Speech function
    private fun textToSpeech(text: String) {
        if (isTTSInitialized) {
            if (!textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Toast.makeText(this, "Text to Speech not initialized", Toast.LENGTH_SHORT).show()
        }
    }
    // Hold or Hover Listener event for text to speech
    @SuppressLint("ClickableViewAccessibility")
    private fun setHoverListener(button: Button, text: String) {
        button.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER -> {
                    textToSpeech.stop()
                    // check if the button is clicked and speaking
                    if (!isClicked || !isSpeaking){
                        isSpeaking = true
                        Log.d("EditOrderActivity", "Text to Speech Button Pressed")
                        textToSpeech(text)
                        Log.d("EditOrderActivity", "Text to Speech Button Triggered")
                    }
                }

                MotionEvent.ACTION_UP -> {
                    textToSpeech.stop()
                    isClicked = false
                    Log.d("EditOrderActivity", "Text to Speech Button Unpressed")

                }

                MotionEvent.ACTION_HOVER_EXIT -> {
                    isClicked = false
                    Log.d("EditOrderActivity", "Text to Speech Hover Exit")
                }
            }
            false // return false to let other touch events like click still work
        }
    }

    override fun onDestroy() {
        // Shutdown TextToSpeech when activity is destroyed
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }
}