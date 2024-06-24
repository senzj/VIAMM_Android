package com.example.viamm

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityEditOrderBinding
import com.example.viamm.models.CancelOrder.CancelOrderResponse
import com.example.viamm.models.getOngoingOrder.ServiceOrder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

@Suppress("DEPRECATION")
class EditOrderActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityEditOrderBinding
    private lateinit var api: Api

    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false
    private var isTTSInitialized = false

    private var orderId: String? = null
    private var orderStatus: String? = null
    private var totalCost: Int = 0


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the binding variable
        binding = ActivityEditOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        //toolbar back button
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//            setDisplayShowHomeEnabled(true)
//        }

        // Initialize the Retrofit API client
        api = RetrofitClient.instance

        // Apply window insets listener to the root layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve the data from the Intent
        orderId = intent.getStringExtra("BOOKING_ID")
        orderStatus = intent.getStringExtra("BOOKING_STATUS")

        // Retrieve list of services (ParcelableArrayList)
        val services: ArrayList<ServiceOrder>? = intent.getParcelableArrayListExtra("SERVICES")

        // Total Cost
        totalCost = intent.getIntExtra("BOOKING_COST", 0)

        // Other data for debugging and future use
        // Masseur
        val masseurName = intent.getStringExtra("MASSEUR_NAME")
        val masseurAvailability = intent.getBooleanExtra("MASSEUR_IS_AVAILABLE", false)

        // Location
        val locationName = intent.getStringExtra("LOCATION_NAME")
        val locationAvailability = intent.getBooleanExtra("LOCATION_IS_AVAILABLE", false)

        // Log or use the data as needed
        Log.d("EditOrderActivity", "Order ID: $orderId")
        Log.d("EditOrderActivity", "Order Status: $orderStatus")
        Log.d("EditOrderActivity", "Total Cost: $totalCost")
        Log.d("EditOrderActivity", "Masseur Name: $masseurName")
        Log.d("EditOrderActivity", "Masseur Availability: $masseurAvailability")
        Log.d("EditOrderActivity", "Location Name: $locationName")
        Log.d("EditOrderActivity", "Location Availability: $locationAvailability")

        // Set booking status with specific color for "ONGOING"
        val statusText = "Booking Status: $orderStatus"
        val spannableString = SpannableString(statusText)

        if (orderStatus?.equals("on-going", ignoreCase = true) == true) {
            val start = statusText.indexOf(orderStatus!!, ignoreCase = true)
            val end = start + orderStatus!!.length
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

        // set hover listener for text to speech
        textToSpeech = TextToSpeech(this, this)
        setHoverListener(binding.btnOrderPayment, "Proceed to Payment")
        setHoverListener(binding.btnCancelOrder, "Cancel Order")
        setHoverListener(binding.btnOrderBack, "Back")

        // Set Payment button click listener to open a dialog box which let them pick manual payment or use money scan
        binding.btnOrderPayment.setOnClickListener {
            textToSpeech("Select Payment Options")
            showPaymentOptionsDialog()
        }

        // Set click listener for cancel button
        binding.btnCancelOrder.setOnClickListener {
            val updatedStatus = "CANCELLED"

            // Call the cancelOrder API endpoint
            api.updateOrderStatus(orderId!!, updatedStatus)
                .enqueue(object : Callback<CancelOrderResponse> {
                    override fun onResponse(
                        call: Call<CancelOrderResponse>,
                        response: Response<CancelOrderResponse>
                    ) {
                        if (response.isSuccessful) {
                            // Show success message
                            Toast.makeText(this@EditOrderActivity, "Order ID \"$orderId\" Cancelled.", Toast.LENGTH_SHORT).show()
                            Log.d("EditOrderActivity", "Order Cancelled Successfully! Redirecting to previous activity")

                            // Set result for the previous activity
                            val resultIntent = Intent()
                            resultIntent.putExtra("UPDATED_STATUS", updatedStatus)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            // Show error message
                            Toast.makeText(this@EditOrderActivity, "An Error Occurred. Failed to Cancel the Requested Order. $response", Toast.LENGTH_LONG).show()
                            Log.d("EditOrderActivity", "An Error Occurred $response.")
                        }
                    }

                    override fun onFailure(call: Call<CancelOrderResponse>, t: Throwable) {
                        // Show error message
                        Toast.makeText(this@EditOrderActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.d("EditOrderActivity", "Error: ${t.message}")
                    }
                })
        }

        // Set click listener for back button
        binding.btnOrderBack.setOnClickListener {
            textToSpeech("Back to Ongoing Booking")
            finish()
        }
    }

    // Function to show the payment options dialog
    private fun showPaymentOptionsDialog() {
        // Create an AlertDialog.Builder
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)

        // Retrieve the list of services (ParcelableArrayList)
        val services: ArrayList<ServiceOrder>? = intent.getParcelableArrayListExtra("SERVICES")

        // Set the title and the items to display in the dialog
        builder.setTitle("Select Payment Options")
            .setItems(arrayOf("Manual Input Payment", "Scan Money Payment")) { _, which ->
                when (which) {
                    0 -> {
                        // Text to speech
                        textToSpeech("Manual Input Option Selected")
                        Log.d("EditOrderActivity", "Manual Input Payment Selected")

                        // Handle Manual Input Payment
                        Toast.makeText(this, "Manual Payment Selected", Toast.LENGTH_SHORT).show()

                        // Navigate to Manual Payment Activity or handle the logic with passing the data
                        val intent = Intent(this, PaymentActivity::class.java).apply {
                            // Booking Details
                            putExtra("BOOKING_ID", orderId)
                            putExtra("BOOKING_STATUS", orderStatus)
                            putExtra("BOOKING_COST", totalCost)

                            // Service Details for table view
                            putParcelableArrayListExtra("SERVICES", services)
                            Log.d("EditOrderActivity", "Booking Details: \nBooking ID: $orderId \nBooking Status: $orderStatus \nTotal Cost: $totalCost \nService Details: $services")
                            Log.d("EditOrderActivity", "Booking Details and data passed to Payment Activity")
                        }
                        startActivity(intent)
                    }

                    1 -> {
                        // Text to speech
                        textToSpeech("Scan Money Option Selected")
                        Log.d("EditOrderActivity", "Scan Money Payment Selected")

                        // Handle Scan Money Input Payment
                        Toast.makeText(this, "Scan Money Selected", Toast.LENGTH_SHORT).show()

                        // Navigate to Scan Money Activity or handle the logic
                        val intent = Intent(this, ScannerActivity::class.java).apply {
                            // Booking Details
                            putExtra("BOOKING_ID", orderId)
                            putExtra("BOOKING_STATUS", orderStatus)
                            putExtra("BOOKING_COST", totalCost)

                            // Service Details for table view
                            putParcelableArrayListExtra("SERVICES", services)
                            Log.d("EditOrderActivity", "Booking Details: \nBooking ID: $orderId \nBooking Status: $orderStatus \nTotal Cost: $totalCost \nService Details: $services")
                            Log.d("EditOrderActivity", "Booking Details and data passed to Scanner Activity")
                        }
                        startActivity(intent)
                    }
                }
            }

        // Create and show the dialog
        builder.create().show()
    }

    fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    // Remove the logout button if it exists
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        menu?.findItem(R.id.btn_logout)?.isVisible = false
        menu?.findItem(R.id.btn_scanner)?.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            // toolbar back button function
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

