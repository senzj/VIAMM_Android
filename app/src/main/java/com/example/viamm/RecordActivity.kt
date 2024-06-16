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
import com.example.viamm.adapters.CompletedOrderAdapter
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityRecordBinding
import com.example.viamm.loadings.LoadingDialog
import com.example.viamm.models.Order.Orders
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class RecordActivity : AppCompatActivity(), CompletedOrderAdapter.RVListEvent {

    private lateinit var binding: ActivityRecordBinding
    private lateinit var orderAdapter: CompletedOrderAdapter
    private var orderList: List<Orders> = emptyList()
    private val EDIT_ORDER_REQUEST_CODE = 100
    private lateinit var loadingDialog: LoadingDialog

    // Fetching data from the API
    private fun fetchData() {
        // Show loading dialog
        loadingDialog.show()

        // Fetch data from the API
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

            // Dismiss loading dialog
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
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        loadingDialog = LoadingDialog(this)

        orderAdapter = CompletedOrderAdapter(orderList, this)

        binding.rvOrders?.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(this@RecordActivity)
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
        Log.d("OrderActivity", "BOOKING_ID: ${selectedOrder.orderId} \nBOOKING_STATUS: ${selectedOrder.orderStatus} \nTotal Amount: ${selectedOrder.totalCost}")

        val intent = Intent(this, EditRecordActivity::class.java).apply {
            putExtra("BOOKING_ID", selectedOrder.orderId)
            putExtra("BOOKING_STATUS", selectedOrder.orderStatus)
            putExtra("BOOKING_AMOUNT", selectedOrder.totalCost)
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
