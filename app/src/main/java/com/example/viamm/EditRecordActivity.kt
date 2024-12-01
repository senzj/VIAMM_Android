@file:Suppress("DEPRECATION")

package com.example.viamm

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityEditRecordBinding
import com.example.viamm.models.getCompletedOrder.ServiceRecord
import retrofit2.Response
import java.util.Locale
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//unused imports
//import android.view.Menu
//import android.view.MenuItem
//import android.os.Parcel
//import android.os.Parcelable
//import android.content.Intent
//import android.os.Handler

class EditRecordActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityEditRecordBinding
    private lateinit var api: Api

    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        val orderId = intent.getStringExtra("booking_id")
        val orderStatus = intent.getStringExtra("booking_status")
        val services: ArrayList<ServiceRecord>? = intent.getParcelableArrayListExtra("service_details")
        val totalCostString = intent.getStringExtra("booking_totalcost") ?: "0" // Default to "0" if not found
        val totalCost = totalCostString.toIntOrNull() ?: 0 // Safely convert to Int, defaulting to 0 if not convertible
        val date = intent.getStringExtra("booking_date")
        val masseurName = intent.getStringExtra("masseur_name")
        val customerName = intent.getStringExtra("customer_name")

        // Set booking status with specific color for "CANCELLED" and "COMPLETE"
        val statusText = "Status: $orderStatus"
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
        } else if (orderStatus?.equals("completed", ignoreCase = true) == true) {
            val start = statusText.indexOf(orderStatus, ignoreCase = true)
            val end = start + orderStatus.length
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.Status_Completelight)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.tvRecordID.text = "Booking ID: $orderId"
        binding.tvRecordStatus.text = spannableString
        binding.tvTotalCost.text = "Total Amount: ₱$totalCost"
        binding.tvRecordMasseur?.text = "Masseur: $masseurName"
        binding.tvRecordCustomer?.text = "Customer: $customerName"
        binding.tvRecordDate?.text = "Date: $date"

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        // binding service details to the tabe
        services?.forEach { service ->
            val tableRow = TableRow(this)
            Log.d("EditRecordActivity", "Service: $service")

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.DKGRAY)
            })

            // Service Count binding
            val amountTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f)
                text = service.amount.toString()
                gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally
                setPadding(4.dpToPx(), 8.dpToPx(), 4.dpToPx(), 4.dpToPx()) // Set padding for text
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) // Set text size to 20sp
                setTextColor(Color.BLACK) // Set text color to black
                setTypeface(null, Typeface.BOLD) // Make the text bold
                setBackgroundColor(Color.WHITE) // Set background color to white
            }
            tableRow.addView(amountTextView)

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.BLACK)
            })

            // Service Name data bind
            val serviceNameTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 2f)
                text = service.name
                gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally
                setPadding(4.dpToPx(), 8.dpToPx(), 4.dpToPx(), 4.dpToPx()) // Set padding for text
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) // Set text size to 20sp
                setTextColor(Color.BLACK) // Set text color to black
                setTypeface(null, Typeface.BOLD) // Make the text bold
                setBackgroundColor(Color.WHITE) // Set background color to white
            }
            tableRow.addView(serviceNameTextView)

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.BLACK)
            })

            // Service Price data bind
            val priceTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f)
                text = service.price.toString()
                gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally
                setPadding(4.dpToPx(), 8.dpToPx(), 4.dpToPx(), 4.dpToPx()) // Set padding for text
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) // Set text size to 20sp
                setTextColor(Color.BLACK) // Set text color to black
                setTypeface(null, Typeface.BOLD) // Make the text bold
                setBackgroundColor(Color.WHITE) // Set background color to white
            }
            tableRow.addView(priceTextView)

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.BLACK)
            })

            // add data to row on table
            binding.tblRecord.addView(tableRow)

            // Add horizontal line after each row
            binding.tblRecord.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1.dpToPx())
                setBackgroundColor(Color.BLACK)
            })
        }

        // Set hover listener for back button
        "Back".setHoverListener(binding.btnRecordBack)

        binding.btnRecordBack.setOnClickListener {
            textToSpeech("Back to Records")
            finish()
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int {
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

        CoroutineScope(Dispatchers.Main).launch {
            // Delay timer
            delay(530)

            // Code to execute after the delay
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("LoginActivity", "Proceeding to next step after TTS delay")
        }
    }

    // Hold or Hover Listener event for text to speech
    @SuppressLint("ClickableViewAccessibility")
    private fun String.setHoverListener(button: Button) {
        button.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER -> {
                    // check if the button is clicked and speaking
                    if (!isClicked || !isSpeaking){
                        isSpeaking = true
                        Log.d("Edit Record Activity", "Text to Speech Button Pressed")
                        textToSpeech(this)
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
