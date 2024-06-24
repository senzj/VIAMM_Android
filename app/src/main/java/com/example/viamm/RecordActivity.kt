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
import android.widget.Button
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

            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val newOrders = response.body()!!.orders
                    orderList = newOrders
                    orderAdapter.updateOrders(newOrders)
                }
            }
            loadingDialog.dismiss()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        ////toolbar back button
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//            setDisplayShowHomeEnabled(true)
//        }

        loadingDialog = LoadingDialog(this)
        orderAdapter = CompletedOrderAdapter(orderList, this)
        binding.rvOrders.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(this@RecordActivity)
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
        Toast.makeText(this, "Selected Order ID: ${selectedOrder.orderId}", Toast.LENGTH_SHORT).show()

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

        Log.d("RecordActivity", "Selected Booking ID: ${selectedOrder.orderId}")
        Log.d("RecordActivity", "Selected Booking Status: ${selectedOrder.orderStatus}")
        Log.d("RecordActivity", "Selected Total Cost: ${selectedOrder.totalCost}")
        Log.d("RecordActivity", "Selected Services: $servicesList")

        selectedOrder.masseurs.forEach { (masseurName, isAvailable) ->
            Log.d("RecordActivity", "Masseur Name: $masseurName")
            Log.d("RecordActivity", "Masseur Availability: $isAvailable")
        }

        selectedOrder.locations.forEach { (locationName, isAvailable) ->
            Log.d("RecordActivity", "Location Name: $locationName")
            Log.d("RecordActivity", "Location Availability: $isAvailable")
        }

        startActivityForResult(intent, EDIT_ORDER_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        menu?.findItem(R.id.btn_logout)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //toolbar back button function
//            android.R.id.home -> {
//                finish()
//                true
//            }

            R.id.btn_scanner -> {
                textToSpeech("Money Scanner")
                redirectToScanner()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun redirectToScanner() {
        val intent = Intent(applicationContext, ScannerActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

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
        Log.d("RecordActivity", "TTS Requested for text: $text")
    }
}
