package com.example.viamm

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityPaymentBinding
import com.example.viamm.loadings.LoadingDialog
import com.example.viamm.loadings.ProcessLoading
import com.example.viamm.models.CancelOrder.CancelOrderResponse
import com.example.viamm.models.getOngoingOrder.ServiceOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

@Suppress("DEPRECATION")
class PaymentActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    // Variables
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var api: Api

    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false
    private var isTTSInitialized = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the Retrofit API client
        api = RetrofitClient.instance

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        // Initializing text to speech
        textToSpeech = TextToSpeech(this, this)

        // Initializing Loading Dialog
        lateinit var processloadingDialog: ProcessLoading

        // Buttons and text view binding initialization
        val etPaymentAmount = binding.etPaymentAmount
        val tvPaymentStatus = binding.tvPaymentStatus

        val btnOrderPayment = binding.btnOrderPayment
        val btnOrderBack = binding.btnOrderBack

        // Initializing text to speech hover
        setHoverListener(btnOrderPayment, "Proceed to Payment")
        setHoverListener(btnOrderBack, "Back")

        // Set up focus change listeners for both EditText
        // This makes that when the user taps outside the EditText, the keyboard disappears
        setupFocusChangeListener(etPaymentAmount)

        // binding data to layout view
        val orderId = intent.getStringExtra("BOOKING_ID")
        val orderStatus = intent.getStringExtra("BOOKING_STATUS")
        val totalCost = intent.getIntExtra("BOOKING_COST", 0)
        val services: ArrayList<ServiceOrder>? = intent.getParcelableArrayListExtra("SERVICES")

        //binding data to text view
        binding.tvOrderID.text = "Booking ID: $orderId"
        binding.tvOrderStatus.text = "Booking Status: $orderStatus"
        binding.tvTotalCost.text = "Total Amount: ₱$totalCost"

        // Logging data for debug
        Log.d("PaymentActivity", "Order ID: $orderId")
        Log.d("PaymentActivity", "Order Status: $orderStatus")
        Log.d("PaymentActivity", "Total Cost: $totalCost")

        //binding service data to table
        services?.forEach { service ->
            val tableRow = TableRow(this)
            Log.d("PaymentActivity", "Service: $service")

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
                setBackgroundColor(Color.DKGRAY)
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
                setBackgroundColor(Color.DKGRAY)
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
                setBackgroundColor(Color.DKGRAY)
            })

            binding.tblOrder.addView(tableRow)

            // Add horizontal line after each row
            binding.tblOrder.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1.dpToPx())
                setBackgroundColor(Color.DKGRAY)
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Functions for payment
        btnOrderPayment.setOnClickListener {
            val paymentAmount = etPaymentAmount.text.toString().toIntOrNull()

            if (paymentAmount != null) {
                when {
                    paymentAmount > totalCost -> {
                        Toast.makeText(this, "Payment Amount is greater than Total Amount", Toast.LENGTH_SHORT).show()
                        textToSpeech("Payment Amount input is Greater than the Total Amount")
                        tvPaymentStatus?.text = "Payment Amount is Greater than the Total Amount"
                        Log.d("PaymentActivity", "Payment Amount is Greater than the Total Amount")
                    }
                    paymentAmount < totalCost -> {
                        Toast.makeText(this, "Payment Amount is less than Total Amount", Toast.LENGTH_SHORT).show()
                        textToSpeech("Payment Amount input is Less than the Total Amount")
                        tvPaymentStatus?.text = "Payment Amount is Less than Total Amount"
                        Log.d("PaymentActivity", "Payment Amount is Less than Total Amount")
                    }
                    else -> {
                        // Loading Process when transacting for aesthetic purposes
                        processloadingDialog = ProcessLoading(this)
                        processloadingDialog.show()

                        // Data pass to the API then to the server
                        val updatedStatus = "COMPLETED"

                        if (orderId!!.isNotEmpty()) {
                            api.updateOrderStatus(orderId, updatedStatus).enqueue(object : Callback<CancelOrderResponse> {
                                override fun onResponse(call: Call<CancelOrderResponse>, response: Response<CancelOrderResponse>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(this@PaymentActivity, "Payment Successful", Toast.LENGTH_SHORT).show()
                                        textToSpeech("Payment Successful")
                                        Log.d("PaymentActivity", "Booking Payment Successfully! Redirecting to previous activity")

                                        // Set result for the previous activity
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("UPDATED_STATUS", updatedStatus)
                                        setResult(RESULT_OK, resultIntent)

                                        // Dismiss loading dialog after 700 milliseconds
                                        Handler().postDelayed({
                                            finish()
                                            processloadingDialog.dismiss()
                                        }, 1500)

                                    } else {
                                        Toast.makeText(this@PaymentActivity, "An Error Occurred. Failed to Update the Order Status.", Toast.LENGTH_LONG).show()
                                        Log.d("PaymentActivity", "An Error Occurred $response.")
                                        processloadingDialog.dismiss()
                                    }
                                }

                                override fun onFailure(call: Call<CancelOrderResponse>, t: Throwable) {
                                    Toast.makeText(this@PaymentActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                    Log.d("PaymentActivity", "Error: ${t.message}")
                                    processloadingDialog.dismiss()
                                }
                            })
                        } else {
                            Toast.makeText(this, "Order ID is missing", Toast.LENGTH_SHORT).show()
                            textToSpeech("Order ID is missing")
                            Log.d("PaymentActivity", "Order ID is missing")
                            processloadingDialog.dismiss()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid payment amount", Toast.LENGTH_SHORT).show()
                textToSpeech("Please enter a valid payment amount")
                tvPaymentStatus?.text = "Please enter a valid payment amount"
            }
        }

        btnOrderBack.setOnClickListener {
            textToSpeech("Back to Ongoing Booking")
            Handler().postDelayed({
                finish()
            },1000)
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

    // Text to speech functions
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
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Toast.makeText(this, "Text to Speech not initialized", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setHoverListener(button: Button, text: String) {
        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER -> {
                    textToSpeech.stop()
                    if (!isClicked || !isSpeaking) {
                        isSpeaking = true
                        Log.d("PaymentActivity", "Text to Speech Button Pressed")
                        textToSpeech(text)
                        Log.d("PaymentActivity", "Text to Speech Button Triggered")
                    }
                }

                MotionEvent.ACTION_UP -> {
                    textToSpeech.stop()
                    isClicked = false
                    Log.d("PaymentActivity", "Text to Speech Button Unpressed")
                }

                MotionEvent.ACTION_HOVER_EXIT -> {
                    isClicked = false
                    Log.d("PaymentActivity", "Text to Speech Hover Exit")
                }
            }
            false // Return false to let other touch events like click still work
        }
    }

    override fun onDestroy() {
        // Shutdown TextToSpeech when activity is destroyed
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }

    fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }
}