package com.example.viamm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

class OrderActivity : AppCompatActivity(), OngoingOrderAdapter.RVListEvent {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var ongoingOrderAdapter: OngoingOrderAdapter
    private var orderList: List<OngoingOrder> = emptyList()
    private val EDIT_ORDER_REQUEST_CODE = 100
    private lateinit var loadingDialog: LoadingDialog

    // Fetching data from the API
    // Fetching data from the API
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

            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val newOrders = response.body()!!.orders
                    orderList = newOrders
                    ongoingOrderAdapter.updateOrders(newOrders)
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
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        ongoingOrderAdapter = OngoingOrderAdapter(orderList, this)

        binding.rvOrders.apply {
            adapter = ongoingOrderAdapter
            layoutManager = LinearLayoutManager(this@OrderActivity)
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
        startActivity(intent)
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        menu?.findItem(R.id.btn_logout)?.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_ORDER_REQUEST_CODE && resultCode == RESULT_OK) {
            val updatedStatus = data?.getStringExtra("UPDATED_STATUS")
            if (updatedStatus != null) {
                fetchData()
            }
        }
    }
}
