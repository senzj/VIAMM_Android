@file:Suppress("DEPRECATION")

package com.example.viamm

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityEditOrderBinding
import com.example.viamm.models.CancelOrder.CancelOrderResponse
import com.example.viamm.models.getOngoingOrder.ServiceOrder
import com.example.viamm.models.payment.PaymentResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


class EditOrderActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityEditOrderBinding
    private lateinit var api: Api

    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false

//    private var orderId: String? = null
//    private var orderStatus: String? = null
    private var totalCost: Int = 0

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the binding variable
        binding = ActivityEditOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        // Initialize the Retrofit API client
        api = RetrofitClient.instance

        // Apply window insets listener to the root layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve the data from the Intent
        // Total Cost
        totalCost = intent.getIntExtra("BOOKING_COST", 0)
        val orderId = intent.getIntExtra("booking_id", 0)
        val orderStatus = intent.getStringExtra("booking_status")
        val services: ArrayList<ServiceOrder>? = intent.getParcelableArrayListExtra("service_details")
        val enddate = intent.getStringExtra("booking_enddate")
        val masseurName = intent.getStringExtra("masseur_name")
        val customerName = intent.getStringExtra("customer_name")
        val totalCost = intent.getIntExtra("booking_totalcost", 0)

        // Set booking status with specific color for "ONGOING"
        val statusText = "Status: $orderStatus"
        val spannableString = SpannableString(statusText)

        if (orderStatus?.equals("on-going", ignoreCase = true) == true) {
            val start = statusText.indexOf(orderStatus, ignoreCase = true)
            val end = start + orderStatus.length
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.Status_Ongoing)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Update UI components
        binding.tvOrderID.text = "Booking ID: $orderId"
        binding.tvOrderStatus.text = spannableString
        binding.tvOrderCustomer?.text = "Customer: $customerName"
        binding.tvOrderMasseur?.text = "Masseur: $masseurName"
        binding.tvOrderDate?.text = "Date: $enddate"
        binding.tvTotalCost.text = "Total Amount: ₱$totalCost"

        // Populate the table with services dynamically
        services?.forEach { service ->
            val tableRow = TableRow(this)
            Log.d("EditOrderActivity", "Service: $service")

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.DKGRAY)
            })

            // Service Amount
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
                setBackgroundColor(Color.DKGRAY)
            })

            // Service Name
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
                setBackgroundColor(Color.DKGRAY)
            })

            // Service Price
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
                setBackgroundColor(Color.DKGRAY)
            })

            binding.tblOrder.addView(tableRow)

            // Add horizontal line after each row
            binding.tblOrder.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1.dpToPx())
                setBackgroundColor(Color.DKGRAY)
            })
        }

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)
//        textToSpeech.setSpeechRate(0.9f)

// Set up the listener for TTS progress
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // TTS has started
                Log.d("EditOrderActivity", "TTS Started")
                Log.d("EditOrderActivity", "TTS Message: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                // TTS finished, proceed with next action
                Log.d("EditOrderActivity", "TTS Finished")
                runOnUiThread {
                    proceedWithNextAction() // Call the logic after TTS finishes
                }
            }

            override fun onError(utteranceId: String?) {
                // Handle any TTS errors if necessary
                Log.e("EditOrderActivity", "TTS Error")
            }
        })

        // set hover listener for text to speech
        setHoverListener(binding.btnOrderPayment, "Proceed to Payment")
        setHoverListener(binding.btnCancelOrder, "Cancel Order")
        setHoverListener(binding.btnOrderBack, "Back")

        // Set Payment button click listener to open a dialog box which let them pick manual payment or use money scan
        binding.btnOrderPayment.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> handleTap()
            }
            true
        }

        // Constants for double-click timing
        val doubleClick =400L // 300 milliseconds

        // Track the last click time
        var lastClickTime: Long = 0

        // Set click listener for cancel button
        binding.btnCancelOrder.setOnClickListener {
            val clickTime = System.currentTimeMillis()
            Log.d("EditOrderActivity", "Cancel Order Button Clicked. Time: $clickTime")

            // if the user double clicked the button, cancel the order.
            if (clickTime - lastClickTime < doubleClick) {
                // Double-click detected, proceed with cancellation
                val masseur = intent.getStringExtra("masseur_name").toString()
                val workstation = intent.getStringExtra("workstation").toString()

                if (orderId != null) {
                    cancelOrder(orderId, workstation, masseur)
                }

            } else {
                // Single click detected, provide TTS confirmation
                speakText("Are you sure you want to cancel Booking ID $orderId? Double-click to confirm.")
            }

            // Update the last click time
            lastClickTime = clickTime
        }

        // Set click listener for back button
        binding.btnOrderBack.setOnClickListener {
            speakText("Back to Ongoing Booking")
            Handler().postDelayed({
                val intent = Intent(this, OrderActivity::class.java)
                startActivity(intent)
                finish()
            },1000)
        }
    }

    private fun cancelOrder(orderId: Int, workstation: String, masseur: String) {
        api.updateOrderCancel(orderId, workstation, masseur)
            .enqueue(object : Callback<CancelOrderResponse> {
                override fun onResponse(
                    call: Call<CancelOrderResponse>,
                    response: Response<CancelOrderResponse>
                ) {
                    if (response.isSuccessful) {
                        showToast("Booking ID: $orderId Cancelled.")
                        logMessage("Order cancelled successfully! Redirecting to previous activity.")
                        speakText("Booking Cancelled")

                        val resultIntent = Intent().apply {
                            putExtra("UPDATED_STATUS", "Cancelled")
                        }
                        setResult(RESULT_OK, resultIntent)

                        lifecycleScope.apply {
                            launch {
                                delay(3000)
                                redirectToBooking()
                            }
                        }
                    } else {
                        showToast("An error occurred. Failed to cancel the order.")
                        logMessage("Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<CancelOrderResponse>, t: Throwable) {
                    showToast("Error: ${t.message}")
                    logMessage("API call failed: ${t.message}")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this@EditOrderActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun logMessage(message: String) {
        Log.d("EditOrderActivity", message)
    }


    private var lastClickTime: Long = 0
    private val doubleClickThreshold: Long = 500 // Time threshold for double-click in ms

    private fun handleCancelOrderClick(intent: Intent) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastClickTime < doubleClickThreshold) {
            val orderId = intent.getIntExtra("order_id", 0)
            val masseur = intent.getStringExtra("masseur_name").toString()
            val workstation = intent.getStringExtra("workstation").toString()

            if (orderId != null) {
                cancelOrder(orderId, masseur, workstation)
            } else {
                showToast("Error: Order ID is missing.")
                logMessage("Order ID not found in intent.")
            }
        } else {
            val orderId = intent.getStringExtra("order_id") ?: "unknown"
            speakText("Are you sure you want to cancel Booking ID $orderId? Double-click to confirm.")
        }

        lastClickTime = currentTime
    }


    private var tapCount = 0 // Make tapCount a member variable
    private val handler = Handler() // Use a shared handler to prevent duplication
    private val tapDelay = 400L // Max delay between taps in milliseconds

    private fun handleTap() {
        tapCount++ // Increment tap count on every tap

        Log.d("EditOrderActivity", "Tap Count: $tapCount")

        // Remove previous callback to reset the timer on every new tap
        handler.removeCallbacksAndMessages(null)

        // Post delayed action to handle taps
        handler.postDelayed({
            when (tapCount) {
                1 -> speakText("Proceeding to payment.. Tap twice for scan money.. And thrice for manual payment")
                2 -> speakText("Tap twice for scan money")
                3 -> speakText("Tap thrice for manual payment")
            }

            // Handle action based on tap count
            handlePaymentOption(tapCount)

            // Reset tap count after the delay
            tapCount = 0
        }, tapDelay)
    }

    // Handle Payment Option Logic
    private fun handlePaymentOption(tapCount: Int) {
        when (tapCount) {
            2 -> {
                // Logic for Scan Money
                Toast.makeText(this, "Scan Money Selected", Toast.LENGTH_SHORT).show()
                speakText("Scan Money Option Selected")
                // Example: Start scan activity
                val orderId = intent.getIntExtra("booking_id", 0)
                val workstation = intent.getStringExtra("workstation").toString()
                val masseur = intent.getStringExtra("masseur_name").toString()

                val intent = Intent(this, ScannerActivity::class.java)
                intent.putExtra("booking_id", orderId)
                intent.putExtra("masseur", masseur)
                intent.putExtra("workstation", workstation)

                Log.d("EOActivity -> ScannerActivity", "Booking ID: $orderId\nMasseur: $masseur\nWorkstation: $workstation")
                startActivity(intent)
                finish()
            }
            3 -> {
                // Logic for Manual Payment
                Toast.makeText(this, "Manual Payment Selected", Toast.LENGTH_SHORT).show()
                speakText("Manual Input Option Selected.. Please enter the amount.")

                // Show payment options dialog
                showManualPaymentDialog()
            }
        }
    }

    private fun showManualPaymentDialog() {
        Log.d("EditOrderActivity", "Showing Manual Payment Dialog")

        // Create an AlertDialog.Builder with a custom style if desired
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)

        // Set the title
        builder.setTitle("Enter Payment Amount")

        // Create an EditText for user input
        val input = EditText(this).apply {
            hint = "Enter Amount" // Set placeholder text
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(50, 30, 50, 30) // Add padding for better spacing
        }

        // Add the input box to the dialog
        builder.setView(input)

        // Add a Submit button with a click listener
        builder.setPositiveButton("Submit Payment") { dialog, _ ->
            // Get the total cost as a string, then convert to Int
            val totalCostString = intent.getStringExtra("booking_totalcost") ?: "0"
            val totalCost = totalCostString.toIntOrNull() ?: 0  // Convert to Int, default to 0 if conversion fails

            // Entered amount should also be an integer (or float), so ensure it's a valid number
            val enteredAmountString = input.text.toString().trim()
            val enteredAmount = enteredAmountString.toIntOrNull() ?: 0  // Convert entered amount to Int, default to 0

            Log.d("EditOrderActivity", "Entered Amount: $enteredAmount")
            Log.d("EditOrderActivity", "Total Cost: $totalCost")

            // If the entered amount is less than the total cost
            if (enteredAmount < totalCost) {
                val totalCoststr = totalCost.toString()
                val message = "Insufficient amount! Please enter at least $totalCoststr pesos"
                speakText(message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                // Return early to prevent dialog dismissal
                return@setPositiveButton

            // If the entered amount is more than the total cost
            } else if(enteredAmount > totalCost){
                val change = enteredAmount - totalCost  // Calculate change

                // convert to string
                val changestr = change.toString()
                val enteredAmountstr = enteredAmount.toString()

                val message = "Payment successful! Your input amount is ${enteredAmountstr} pesos and your change is ${changestr} pesos."
                Log.d("EditOrderActivity", "Message: $message")
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                // Use the utility function to handle TTS and redirection
                speakText(message)

                // Call Retrofit to submit the payment
                submitPayment(totalCost)

                // for delay input
//                // Call Retrofit to submit the payment
//                lifecycleScope.apply {
//                    launch {
//                        delay(10000)
//                        submitPayment(totalCost.toString())
//                    }
//                }

            // If the entered amount is exactly equal to the total cost
            } else if (enteredAmount == totalCost) {
                val message = "Payment successful!"
                speakText(message)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                // Call Retrofit to submit the payment
                submitPayment(enteredAmount)
            }

//            // Dismiss the dialog after payment success
//            dialog.dismiss()

            // Use lifecycleScope to delay redirection
            lifecycleScope.apply {
                launch {
                    delay(6000) // delay

                    // Dismiss the dialog after payment success
                    dialog.dismiss()

                    redirectToBooking() // Redirect to the Booking Activity
                }
            }
        }

        // Optionally, add a Cancel button to dismiss the dialog
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()
    }

    // redirect to booking
    private fun redirectToBooking() {
        speakText("Redirecting you now to Booking")
        val intent = Intent(this, OrderActivity::class.java)
        lifecycleScope.apply {
            launch {
                delay(2000)
                startActivity(intent)
                finish()
            }
        }
    }

    // Function to submit payment to the server using Retrofit
    private fun submitPayment(amount: Int) {
        val bookingId = intent.getIntExtra("booking_id", 0)
        val workstation = intent.getStringExtra("workstation").toString()
        val masseur = intent.getStringExtra("masseur_name").toString()

        // Example API call
        RetrofitClient.instance.updateOrderPayment(bookingId, amount, workstation, masseur)
            .enqueue(object : Callback<PaymentResponse> {
                override fun onResponse(call: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                    if (response.isSuccessful) {
//                        Toast.makeText(this@EditOrderActivity, "Payment successful!", Toast.LENGTH_SHORT).show()
                        Log.d("EditOrderActivity", "Payment submitted successfully: $amount")
                    } else {
                        Toast.makeText(this@EditOrderActivity, "Failed to submit payment: ${response.message()}", Toast.LENGTH_SHORT).show()
                        Log.e("EditOrderActivity", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                    Toast.makeText(this@EditOrderActivity, "Error submitting payment: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditOrderActivity", "Error: ${t.message}")
                }
            })
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    //text to speech functions
    // The onInit function to initialize TTS
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // TTS initialized successfully, set language if needed
            val langResult = textToSpeech.setLanguage(Locale.US)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported or missing data")
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    private fun speakText(message: String) {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()  // Stop any ongoing speech
        }
        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "messageId"
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, params)  // Use QUEUE_FLUSH to ensure the message is spoken immediately
    }

    // speaks long text
    private fun speakLongText(message: String) {
        val chunks = message.chunked(100) // Split the message into chunks of 100 characters each
        chunks.forEach { chunk ->
            speakText(chunk)
        }
    }

    // pauses text in between
    private fun speakTextWithPause(message: String) {
        val chunks = message.chunked(100)
        chunks.forEach { chunk ->
            speakText(chunk)
            Thread.sleep(500)  // Pause for 500 milliseconds before speaking the next chunk
        }
    }

    // pauses text if needed
    private fun speakLongTextAsync(message: String) {
        Thread {
            val chunks = message.chunked(100)
            chunks.forEach { chunk ->
                speakText(chunk)
                Thread.sleep(500) // Pause between chunks if needed
            }
        }.start()
    }

    // Proceed with Next Action after TTS finishes
    private fun proceedWithNextAction() {
        // Your logic here to proceed after TTS finishes
        Log.d("EditOrderActivity", "Proceeding with next action after TTS completion")
        // Example: startActivity(Intent(this, AnotherActivity::class.java))
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
                        speakText(text)
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

