package com.example.viamm

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityEditRecordBinding
import com.example.viamm.models.Order.ServiceRecord
import retrofit2.Response
import java.util.Locale

class EditRecordActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityEditRecordBinding
    private lateinit var api: Api

    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        binding = ActivityEditRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.StatusBarColor)

        setSupportActionBar(binding.toolbar)
        //toolbar back button
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//            setDisplayShowHomeEnabled(true)
//        }

        api = RetrofitClient.instance

        // Retrieve the data from the Intent
        val orderId = intent.getStringExtra("BOOKING_ID")
        val orderStatus = intent.getStringExtra("BOOKING_STATUS")
        val services: ArrayList<ServiceRecord>? = intent.getParcelableArrayListExtra("SERVICES")
        val totalCost = intent.getIntExtra("BOOKING_COST", 0)

//        for future uses
//        val masseurName = intent.getStringExtra("MASSEUR_NAME")
//        val masseurAvailability = intent.getBooleanExtra("MASSEUR_IS_AVAILABLE", false)
//        val locationName = intent.getStringExtra("LOCATION_NAME")
//        val locationAvailability = intent.getBooleanExtra("LOCATION_IS_AVAILABLE", false)

        Log.d("EditRecordActivity", "Booking ID: $orderId")
        Log.d("EditRecordActivity", "Booking Status: $orderStatus")
        Log.d("EditRecordActivity", "Total Cost: $totalCost")

        // Set booking status with specific color for "CANCELLED" and "COMPLETE"
        val statusText = "Booking Status: $orderStatus"
        val spannableString = SpannableString(statusText)

        if (orderStatus?.equals("cancelled", ignoreCase = true) == true) {
            val start = statusText.indexOf(orderStatus, ignoreCase = true)
            val end = start + orderStatus.length
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.Status_Cancelled)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (orderStatus?.equals("complete", ignoreCase = true) == true) {
            val start = statusText.indexOf(orderStatus, ignoreCase = true)
            val end = start + orderStatus.length
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.Status_Complete)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.tvRecordID.text = "Booking ID: $orderId"
        binding.tvRecordStatus.text = spannableString
        binding.tvTotalCost.text = "Total Amount: ₱$totalCost"

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        services?.forEach { service ->
            val tableRow = TableRow(this)
            Log.d("EditRecordActivity", "Service: $service")

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.DKGRAY)
            })

            // Service Amount
            val amountTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                text = service.amount.toString()
                gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx()) // Set padding for text
            }
            tableRow.addView(amountTextView)

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.BLACK)
            })

            // Service Name
            val serviceNameTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                text = service.name
                gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx()) // Set padding for text
            }
            tableRow.addView(serviceNameTextView)

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.BLACK)
            })

            // Service Price
            val priceTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                text = service.price.toString()
                gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx()) // Set padding for text
            }
            tableRow.addView(priceTextView)

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.BLACK)
            })
            binding.tblRecord.addView(tableRow)

            // Add horizontal line after each row
            binding.tblRecord.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1.dpToPx())
                setBackgroundColor(Color.BLACK)
            })
        }
        // Set hover listener for back button
        setHoverListener(binding.btnRecordBack, "Back")

        binding.btnRecordBack.setOnClickListener {
            textToSpeech("Back to Records")
            finish()
        }


    }

    // Extension function to convert dp to pixels
    fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun handleErrorResponse(response: Response<*>) {
        val errorBody = response.errorBody()?.string()
        val errorMessage = errorBody ?: "An Error Occurred. Failed to Update the Requested Order."
        Toast.makeText(this@EditRecordActivity, errorMessage, Toast.LENGTH_LONG).show()
        Log.d("EditRecordActivity", "An Error Occurred $response.")
    }

    private fun handleFailure(t: Throwable) {
        Toast.makeText(this@EditRecordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
        Log.d("EditRecordActivity", "Error: ${t.message}")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        menu?.findItem(R.id.btn_logout)?.isVisible = false
        menu?.findItem(R.id.btn_scanner)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //toolbar back button function
//            android.R.id.home -> {
//                finish()
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //text to speech functions
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
                        Log.d("Edit Record Activity", "Text to Speech Button Pressed")
                        textToSpeech(text)
                        Log.d("Edit Record Activity", "Text to Speech Button Triggered")
                    }
                }

                MotionEvent.ACTION_UP -> {
                    textToSpeech.stop()
                    isClicked = false
                    Log.d("Edit Record Activity", "Text to Speech Button Unpressed")

                }

                MotionEvent.ACTION_HOVER_EXIT -> {
                    isClicked = false
                    Log.d("Edit Record Activity", "Text to Speech Hover Exit")
                }
            }
            false // return false to let other touch events like click still work
        }
    }
}
