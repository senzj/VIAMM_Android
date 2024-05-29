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
import com.example.viamm.adapters.OrderAdapter
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityOrderBinding
import com.example.viamm.models.Order.Orders
import com.example.viamm.storage.SharedData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class OrderActivity : AppCompatActivity(), OrderAdapter.RVListEvent {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var orderAdapter: OrderAdapter
    private var orderList: List<Orders> = emptyList()  // Initialize with an empty list

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate the layout using ViewBinding
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inflate the toolbar
        setSupportActionBar(binding.toolbar)

        // Initialize the adapter with an empty list
        orderAdapter = OrderAdapter(orderList, this)

        // Set the adapter and layout manager
        binding.rvOrders?.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(this@OrderActivity)
        }

        // Apply window insets listener to the root layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fetch all orders
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitClient.instance.getAllOrders()
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
            }

            // Inside the coroutine, after fetching the orders
            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val newOrders = response.body()!!.orders
                    orderList = newOrders // Update the orderList
                    orderAdapter.updateOrders(newOrders) // Call the adapter's updateOrders method
                }
            }
        }
    }

    override fun onItemClicked(position: Int) {
        // Handle item click event
        val selectedOrder = orderList[position]
        // Do something with the selected order
        Toast.makeText(this, "Selected Order ID: ${selectedOrder.orderId}", Toast.LENGTH_SHORT).show()
        Log.d("OrderActivity", "Selected Order ID: ${selectedOrder.orderId} \nEmployee Name: ${selectedOrder.orderEmpName} \nStatus: ${selectedOrder.orderStatus}")

        // Redirect to EditOrderActivity with the selected order ID
        val intent = Intent(this, EditOrderActivity::class.java).apply {
            putExtra("ORDER_ID", selectedOrder.orderId)
            putExtra("ORDER_SERVICE", selectedOrder.orderService)
            putExtra("ORDER_EMP_NAME", selectedOrder.orderEmpName)
            putExtra("ORDER_STATUS", selectedOrder.orderStatus)
        }
        startActivity(intent)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Function to handle logout
    private fun logout() {
        SharedData.getInstance(this).isLoggedIn = false

        // Redirect to login activity
        val intent = Intent(applicationContext, LoginActivity::class.java)  // Assuming LoginActivity is the correct one
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish the current activity
        Toast.makeText(this, "Logged out Successfully!", Toast.LENGTH_SHORT).show()
    }
}
