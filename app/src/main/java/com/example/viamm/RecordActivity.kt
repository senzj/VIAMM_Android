package com.example.viamm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.viamm.adapters.CompletedOrderAdapter
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityRecordBinding
import com.example.viamm.loadings.LoadingDialog
import com.example.viamm.models.Order.Orders
import com.example.viamm.models.Order.ServiceRecord
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale

@Suppress("DEPRECATION")
class RecordActivity : AppCompatActivity(), CompletedOrderAdapter.RVListEvent, TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityRecordBinding
    private lateinit var orderAdapter: CompletedOrderAdapter
    private var orderList: List<Orders> = emptyList()
    private val EDIT_ORDER_REQUEST_CODE = 100
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var tvNoRecordBooking: TextView

    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchData() {
        loadingDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitClient.instance.getCompletedOrders()
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "App Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Log.e("RecordActivity", "App error, details: ${e.message}")
                return@launch
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Http Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Log.e("RecordActivity", "Http error, details: ${e.message}")
                return@launch
            }

            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val newOrders = response.body()!!.orders
                    orderList = newOrders
                    orderAdapter.updateOrders(newOrders)
                    if (newOrders.isEmpty()) {
                        tvNoRecordBooking.visibility = View.VISIBLE
                    } else {
                        tvNoRecordBooking.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(applicationContext, "Failed to fetch orders", Toast.LENGTH_SHORT).show()
                }
                loadingDialog.dismiss()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize toolbar
        setSupportActionBar(binding.toolbar)
        ////toolbar back button
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//            setDisplayShowHomeEnabled(true)
//        }

        // Initialize loading dialog
        loadingDialog = LoadingDialog(this)

        // Initialize loading dialog
        textToSpeech = TextToSpeech(this, this)

        // Initialize tvNoOngoingBooking for no ongoing booking
        tvNoRecordBooking = binding.tvNoRecordBooking

        // Initialize order adapter for list views
        orderAdapter = CompletedOrderAdapter(orderList, this)
        binding.rvOrders.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(this@RecordActivity)
        }

        setHoverListener(binding.btnBack,"Back to Dashboard")
        binding.btnBack.setOnClickListener {
            textToSpeech("Back to Dashboard")
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fetchData()
    }

    // Item List View
    // variable for clickings
    private var lastClickTime: Long = 0
    private val clickDelay: Long = 2000 // Time window for detecting double-click in milliseconds

    override fun onItemClicked(position: Int) {
        val selectedOrder = orderList[position]
        val currentTime = System.currentTimeMillis()

        // Single-click or Double-click logic
        if (currentTime - lastClickTime < clickDelay) {
            // Double-click detected
            onDoubleClick(selectedOrder)
        } else {
            // Single-click detected
            onSingleClick(selectedOrder)
        }

        // Update the last click time
        lastClickTime = currentTime
    }

    // Change the function parameter to Orders instead of Order
    private fun onSingleClick(selectedOrder: Orders) {
        // Provide Text-to-Speech feedback for the selected order
        val ttsText = "Booking ID: ${selectedOrder.orderId}, Status: ${selectedOrder.orderStatus}, Total Cost: ${selectedOrder.totalCost} selected."
        textToSpeech(ttsText)  // Assuming `textToSpeech` is a function that handles the TTS functionality

        Log.d("RecordActivity", "Single-click: $ttsText")
    }

    private fun onDoubleClick(selectedOrder: Orders) {
        // Provide Text-to-Speech feedback for redirection
        val ttsText = "Redirecting to Record details for Booking ID: ${selectedOrder.orderId}"
        textToSpeech(ttsText)

        // Redirect to EditRecordActivity with all the relevant data
        val servicesList = ArrayList<ServiceRecord>()
        selectedOrder.services.forEach { (serviceName, serviceDetails) ->
            val service = ServiceRecord(serviceDetails.amount, serviceName, serviceDetails.price, serviceDetails.type)
            servicesList.add(service)
        }

        val intent = Intent(this, EditRecordActivity::class.java).apply {
            putExtra("BOOKING_ID", selectedOrder.orderId)
            putExtra("BOOKING_STATUS", selectedOrder.orderStatus)
            putExtra("BOOKING_COST", selectedOrder.totalCost)
            putParcelableArrayListExtra("SERVICES", servicesList)

            selectedOrder.masseurs.forEach { (masseurName, isAvailable) ->
                putExtra("MASSEUR_NAME", masseurName)
                putExtra("MASSEUR_IS_AVAILABLE", isAvailable)
            }

            selectedOrder.locations.forEach { (locationName, isAvailable) ->
                putExtra("LOCATION_NAME", locationName)
                putExtra("LOCATION_IS_AVAILABLE", isAvailable)
            }
        }

        // add a loading for visual reasons
        loadingDialog.show()

        // Timer using Handler to delay code execution
        Handler().postDelayed({
            // This is the code that will run after the delay
            // Put your code here that you want to execute after a delay
            Log.d("RecordActivity", "Proceeding to next step after TTS delay")

            // starts the next activity
            startActivity(intent)

            loadingDialog.dismiss()

        }, 4000) // 2000 milliseconds = 2 seconds

        Log.d("RecordActivity", "Double-click: Redirecting to Edit Record for Booking ID: ${selectedOrder.orderId}")
    }


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.toolbar_menu, menu)
//        menu?.findItem(R.id.btn_scanner)?.isVisible = false
//        menu?.findItem(R.id.btn_logout)?.isVisible = false
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            //toolbar back button function
//            android.R.id.home -> {
//                finish()
//                true
//            }
//
//            R.id.btn_scanner -> {
//                textToSpeech("Money Scanner")
//                redirectToScanner()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

//    private fun redirectToScanner() {
//        val intent = Intent(applicationContext, ScannerActivity::class.java)
//        startActivity(intent)
//    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    //
    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_ORDER_REQUEST_CODE && resultCode == RESULT_OK) {
            val updatedStatus = data?.getStringExtra("UPDATED_STATUS")
            if (updatedStatus != null) {
                fetchData()
            }
        }
    }

    // text to speech
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

    private fun textToSpeech(text: String) {
        if (!textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        Handler().postDelayed({
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }, 500)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setHoverListener(button: Button, text: String) {
        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER -> {
                    if (!isClicked || !isSpeaking) {
                        isSpeaking = true
                        textToSpeech(text)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    textToSpeech.stop()
                    isClicked = false
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    isClicked = false
                }
            }
            false
        }
    }

    override fun onTTSRequested(text: String) {
        textToSpeech(text)
        Log.d("RecordActivity", "TTS Requested for text: $text")
    }
}
