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
import com.example.viamm.adapters.OngoingOrderAdapter
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityOrderBinding
import com.example.viamm.loadings.LoadingDialog
import com.example.viamm.models.getOngoingOrder.OngoingOrder
import com.example.viamm.models.getOngoingOrder.ServiceOrder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale

class OrderActivity : AppCompatActivity(), OngoingOrderAdapter.RVListEvent, TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var ongoingOrderAdapter: OngoingOrderAdapter
    private var orderList: List<OngoingOrder> = emptyList()
    private val EDIT_ORDER_REQUEST_CODE = 100
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var tvNoOngoingBooking: TextView

    private lateinit var textToSpeech: TextToSpeech
    private var isClicked = false
    private var isSpeaking = false

    // Fetching data from the API
    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchData() {
        // Show loading dialog
        loadingDialog.show()

        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitClient.instance.getOngoingOrders()
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "App Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Log.e("OrderActivity", "App error, details: ${e.message}")
                return@launch
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Http Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Log.e("OrderActivity", "Http error, details: ${e.message}")
                return@launch
            } finally {
                // Dismiss loading dialog in any case
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                }
            }

            // if there are no ongoing booking then display this
            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val newOrders = response.body()!!.orders
                    orderList = newOrders
                    ongoingOrderAdapter.updateOrders(newOrders)
                    if (newOrders.isEmpty()) {
                        tvNoOngoingBooking.visibility = View.VISIBLE
                    } else {
                        tvNoOngoingBooking.visibility = View.GONE
                    }
                }
            }

        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize loading dialog
        loadingDialog = LoadingDialog(this)

        setSupportActionBar(binding.toolbar)

        // Initialize tvNoOngoingBooking for no ongoing booking
        tvNoOngoingBooking = binding.tvNoOngoingBooking

        // Preping for list data view
        ongoingOrderAdapter = OngoingOrderAdapter(orderList, this)
        binding.rvOrders.apply {
            adapter = ongoingOrderAdapter
            layoutManager = LinearLayoutManager(this@OrderActivity)
        }

        textToSpeech = TextToSpeech(this, this)

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

    override fun onItemClicked(position: Int) {
        val selectedOrder = orderList[position]
        Toast.makeText(this, "Selected Booking ID: ${selectedOrder.orderId}", Toast.LENGTH_SHORT).show()

        // Prepare services list
        val servicesList = ArrayList<ServiceOrder>()
        selectedOrder.services.forEach { (serviceName, serviceDetails) ->
            val service = ServiceOrder(serviceDetails.amount, serviceName, serviceDetails.price, serviceDetails.type)
            servicesList.add(service)
        }

        // Prepare intent to start EditOrderActivity
        val intent = Intent(this, EditOrderActivity::class.java).apply {
            // Passing the basic data to EditOrderActivity
            putExtra("BOOKING_ID", selectedOrder.orderId)
            putExtra("BOOKING_STATUS", selectedOrder.orderStatus)
            putExtra("BOOKING_COST", selectedOrder.totalCost)

            // Pass services list as ParcelableArrayList
            putParcelableArrayListExtra("SERVICES", servicesList)

            // Pass masseurs details individually
            selectedOrder.masseurs.forEach { (masseurName, isAvailable) ->
                putExtra("MASSEUR_NAME", masseurName)
                putExtra("MASSEUR_IS_AVAILABLE", isAvailable)
            }

            // Pass locations details individually
            selectedOrder.locations.forEach { (locationName, isAvailable) ->
                putExtra("LOCATION_NAME", locationName)
                putExtra("LOCATION_IS_AVAILABLE", isAvailable)
            }
        }
        // Start EditOrderActivity
        startActivity(intent)

//        // Destroy the current activity
//        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        menu?.findItem(R.id.btn_logout)?.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // toolbar back button function
            android.R.id.home -> {
                finish()
                true
            }

            R.id.btn_scanner -> {
                redirectToScanner()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //  Function to go Scanner Activity
    private fun redirectToScanner() {
        val intent = Intent(applicationContext, ScannerActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_ORDER_REQUEST_CODE && resultCode == RESULT_OK) {
            val updatedStatus = data?.getStringExtra("UPDATED_STATUS")
            if (updatedStatus != null) {
                fetchData()
            }
        }
    }
    //text to speech
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
        Log.d("OrderActivity", "TTS Requested for text: $text")
    }

}
