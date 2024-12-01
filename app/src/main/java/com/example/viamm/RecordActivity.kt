package com.example.viamm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.viamm.adapters.CompletedOrderAdapter
import com.example.viamm.api.RetrofitClient
import com.example.viamm.loadings.LoadingDialog
import kotlinx.coroutines.*
import androidx.lifecycle.*
import com.example.viamm.databinding.ActivityRecordBinding
import com.example.viamm.models.getCompletedOrder.CompletedOrder
import java.util.Locale

class RecordActivity : AppCompatActivity(), CompletedOrderAdapter.RVListEvent, TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityRecordBinding
    private lateinit var completedOrderAdapter: CompletedOrderAdapter
    private var orderList: List<CompletedOrder> = emptyList()
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var textToSpeech: TextToSpeech
    private var isSpeaking = false
    private var lastClickTime: Long = 0
    private val clickDelay = 500L // Double-click time threshold in ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        initializeTextToSpeech()
        fetchOngoingBooking()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        loadingDialog = LoadingDialog(this)

        // Set up RecyclerView for ongoing orders
        completedOrderAdapter = CompletedOrderAdapter(orderList, this)
        binding.rvOrders.apply {
            adapter = completedOrderAdapter
            layoutManager = LinearLayoutManager(this@RecordActivity)
        }

        binding.btnBack.setOnClickListener {
            textToSpeech("Back to Dashboard")
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

    private fun fetchOngoingBooking() {
        loadingDialog.show()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("RecordsActivity", "Fetching data...")

                val response = RetrofitClient.instance.getCompletedOrders()

                if (response.isSuccessful) {
                    val rawResponse = response.body().toString()
                    Log.d("RecordsActivity", "Raw JSON response: $rawResponse")
                } else {
                    Log.d("RecordsActivity", "Error fetching data: ${response.message()}")
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
            } catch (e: Exception) {
                handleError("Error occurred while fetching data.", e)
            } finally {
                withContext(Dispatchers.Main) { loadingDialog.dismiss() }
            }
        }
    }

    private fun handleError(message: String, exception: Exception) {
        lifecycleScope.launch(Dispatchers.Main) {
            showToast(message)
            Log.e("RecordsActivity", message, exception)
        }
    }

    private fun showNoBookingsUI() {
        binding.apply {
            tvNoRecordBookingh1?.visibility = View.VISIBLE
            tvNoRecordBookingh2?.visibility = View.VISIBLE
            rvOrders.visibility = View.GONE
        }
        textToSpeech("No Bookings Found.")
    }

    private fun updateOrderList(newOrders: List<CompletedOrder>) {
        binding.apply {
            tvNoRecordBookingh1?.visibility = View.GONE
            tvNoRecordBookingh2?.visibility = View.GONE
            rvOrders.visibility = View.VISIBLE
        }
        orderList = newOrders
        completedOrderAdapter.updateOrders(newOrders)
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

    private fun onSingleClick(selectedOrder: CompletedOrder) {
        val message = """
            You selected Booking ID: ${selectedOrder.orderId}, 
            which is ${selectedOrder.orderStatus}. 
            The customer who booked is ${selectedOrder.customer.customerName}, 
            The assigned masseur is ${selectedOrder.masseur.masseurName}, 
            and the masseur gender is ${selectedOrder.masseur.masseurGender}. 
            The total cost for this booking is ${selectedOrder.totalCost} pesos, 
            and the booking is scheduled on ${selectedOrder.timeEnd}.
        """.trimIndent()
        textToSpeech(message)
        Log.d("RecordsActivity", "Single-click: $message")
    }

    private fun onDoubleClick(selectedOrder: CompletedOrder) {
        val message = "Redirecting to Booking details for Booking ID: ${selectedOrder.orderId}"
        textToSpeech(message)

        val servicesList = arrayListOf(selectedOrder.services)
        Log.d("RecordsActivity", "Service details: $servicesList")

        val intent = Intent(this, EditRecordActivity::class.java).apply {
            putExtra("booking_id", selectedOrder.orderId)
            putExtra("booking_status", selectedOrder.orderStatus)
            putExtra("booking_totalcost", selectedOrder.totalCost)
            putExtra("masseur_name", selectedOrder.masseur.masseurName)
            putExtra("customer_name", selectedOrder.customer.customerName)
            putExtra("booking_date", selectedOrder.orderDate)
            putParcelableArrayListExtra("service_details", servicesList)
            Log.d("RecordsActivity", "redirecting to edit records activity with details")
        }

        lifecycleScope.launch(Dispatchers.Main) {
            delay(3000)
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
