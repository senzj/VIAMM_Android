package com.example.viamm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
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
import kotlinx.coroutines.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale



class OrderActivity : AppCompatActivity(), OngoingOrderAdapter.RVListEvent, TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityOrderBinding
    // declaring adapter
    private lateinit var ongoingOrderAdapter: OngoingOrderAdapter
    private var orderList: List<OngoingOrder> = emptyList()
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var textToSpeech: TextToSpeech
    private var isSpeaking = false
    private var lastClickTime: Long = 0
    private val clickDelay = 500L // Double-click time threshold in ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        initializeTextToSpeech()
        fetchOngoingBooking()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        loadingDialog = LoadingDialog(this)

        // Set up RecyclerView for ongoing orders
        ongoingOrderAdapter = OngoingOrderAdapter(orderList, this)
        binding.rvOrders.apply {
            // Set up RecyclerView for the ongoing orders adapter
            adapter = ongoingOrderAdapter
            layoutManager = LinearLayoutManager(this@OrderActivity)
        }

        binding.btnBack.setOnClickListener {
            textToSpeech("Back to Dashboard")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this, this)
    }

    // Fetch data from API
    private fun fetchOngoingBooking() {
        loadingDialog.show()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("OrderActivity", "Fetching data...")

                val response = RetrofitClient.instance.getOngoingOrders()

                // Log the raw response (JSON string)
                if (response.isSuccessful) {
                    val rawResponse = response.body().toString()
                    Log.d("OrderActivity", "Raw JSON response: $rawResponse")
                } else {
                    Log.d("OrderActivity", "Error fetching data: ${response.message()}")
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val newOrders = response.body()?.orders.orEmpty()
                        if (newOrders.isEmpty()) {
                            showNoBookingsUI()
                        } else {
                            updateOrderList(newOrders)
                        }
                    } else {
                        showToast("Failed to fetch data: ${response.message()}")
                    }
                }
            } catch (e: IOException) {
                handleError("Network error occurred.", e)
            } catch (e: HttpException) {
                handleError("HTTP error occurred.", e)
            } catch (e: Exception) {
                handleError("An unexpected error occurred.", e)
            } finally {
                withContext(Dispatchers.Main) { loadingDialog.dismiss() }
            }
        }
    }

    private fun handleError(message: String, exception: Exception) {
        lifecycleScope.launch(Dispatchers.Main) {
            showToast(message)
            Log.e("OrderActivity", message, exception)
        }
    }

    private fun showNoBookingsUI() {
        binding.apply {
            tvNoOngoingBookingh1?.visibility = View.VISIBLE
            tvNoOngoingBookingh2?.visibility = View.VISIBLE
            rvOrders.visibility = View.GONE
        }
        textToSpeech("No Bookings Found.")
    }

    private fun updateOrderList(newOrders: List<OngoingOrder>) {
        binding.apply {
            tvNoOngoingBookingh1?.visibility = View.GONE
            tvNoOngoingBookingh2?.visibility = View.GONE
            rvOrders.visibility = View.VISIBLE
        }
        orderList = newOrders

        // Update the adapter with the new data
        ongoingOrderAdapter.updateOrders(newOrders)
    }

    override fun onItemClicked(position: Int) {
        val selectedOrder = orderList[position]
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < clickDelay) {
            onDoubleClick(selectedOrder)
        } else {
            onSingleClick(selectedOrder)
        }
        lastClickTime = currentTime
    }

    private fun onSingleClick(selectedOrder: OngoingOrder) {
        val message = """
            You have tapped on Booking ID ${selectedOrder.orderId}, 
            which is currently ${selectedOrder.orderStatus}. 
            The customer who booked is ${selectedOrder.customer.customerName}, 
            The assigned masseur is ${selectedOrder.masseur.masseurName}, 
            and the masseur gender is ${selectedOrder.masseur.masseurGender}. 
            The total cost for this booking is ${selectedOrder.totalCost} pesos, 
            and the booking is scheduled to end at ${selectedOrder.timeEnd}.
            Double tap to select booking ID ${selectedOrder.orderId}.
        """.trimIndent()
        textToSpeech(message)
        Log.d("OrderActivity", "Single-click: $message")
    }

    private fun onDoubleClick(selectedOrder: OngoingOrder) {
        val message = "You have selected Booking ID ${selectedOrder.orderId}. Redirecting you now to Booking ID ${selectedOrder.orderId}"
        textToSpeech(message)

        val servicesList = arrayListOf(selectedOrder.services)
        Log.d("RecordsActivity", "Service details: $servicesList")

        // Prepare intent for the next activity
        val intent = Intent(this, EditOrderActivity::class.java).apply {
            putExtra("booking_id", selectedOrder.orderId)
            putExtra("booking_status", selectedOrder.orderStatus)
            putExtra("booking_totalcost", selectedOrder.totalCost)
            putExtra("masseur_name", selectedOrder.masseur.masseurName)
            putExtra("customer_name", selectedOrder.customer.customerName)
            putExtra("booking_enddate", selectedOrder.timeEnd)
            putParcelableArrayListExtra("service_details", servicesList)
            Log.d("RecordsActivity", "redirecting to edit records activity with details")
        }

        lifecycleScope.launch(Dispatchers.Main) {
            delay(3000)
            finish()
            startActivity(intent)
        }
    }

    private fun textToSpeech(message: String) {
        if (isSpeaking) textToSpeech.stop()
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        isSpeaking = true
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                showToast("Text-to-Speech is not supported.")
            }
        } else {
            showToast("Failed to initialize Text-to-Speech.")
        }
    }

    override fun onTTSRequested(text: String) {
        textToSpeech(text)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
