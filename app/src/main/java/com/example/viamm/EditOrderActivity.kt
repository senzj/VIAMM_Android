package com.example.viamm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityEditOrderBinding
import com.example.viamm.models.CancelOrder.CancelOrderResponse
import com.example.viamm.models.UpdateOrder.UpdateOrdersResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditOrderBinding
    private lateinit var api: Api

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the binding variable
        binding = ActivityEditOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // Initialize the Retrofit API client
        api = RetrofitClient.instance

        // Apply window insets listener to the root layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve the data from the Intent
        val orderId = intent.getStringExtra("ORDER_ID")
        val orderService = intent.getStringExtra("ORDER_SERVICE")
        val orderEmpName = intent.getStringExtra("ORDER_EMP_NAME")
        val orderStatus = intent.getStringExtra("ORDER_STATUS")

        // Log or use the data as needed
        Log.d("EditOrderActivity", "Order ID: $orderId")
        Log.d("EditOrderActivity", "Order Service: $orderService")
        Log.d("EditOrderActivity", "Employee Name: $orderEmpName")
        Log.d("EditOrderActivity", "Order Status: $orderStatus")

        // Update the UI or perform actions with the retrieved data
        binding.tvOrderID.text = "$orderId"
        binding.ETOrderService.setText(orderService)
        binding.ETOrderEmpName.setText(orderEmpName)

        // Set click listener for Edit button
        binding.btnOrderPayment.setOnClickListener {
            // Call the updateOrder API endpoint
            val updatedService = binding.ETOrderService.text.toString()
            val updatedEmpName = binding.ETOrderEmpName.text.toString()
            val updatedStatus = binding.spOrderStatus.selectedItem.toString()

            api.updateOrder(orderId!!, updatedService, updatedEmpName, updatedStatus)
                .enqueue(object : Callback<UpdateOrdersResponse> {
                    override fun onResponse(
                        call: Call<UpdateOrdersResponse>,
                        response: Response<UpdateOrdersResponse>
                    ) {
                        if (response.isSuccessful) {
                            // Show success message
                            Toast.makeText(this@EditOrderActivity, "Order ID \"$orderId\" Updated Successfully!", Toast.LENGTH_SHORT).show()

                            Log.d("EditOrderActivity", "Order Updated Successfully! Redirecting to OrderActivity")

                            // Set result for the previous activity
                            val resultIntent = Intent()
                            resultIntent.putExtra("UPDATED_STATUS", updatedStatus)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            // Show error message
                            Toast.makeText(this@EditOrderActivity, "An Error Occurred. Failed to Update the Requested Order. $response", Toast.LENGTH_LONG).show()

                            Log.d("EditOrderActivity", "An Error Occurred $response.")
                        }
                    }

                    override fun onFailure(call: Call<UpdateOrdersResponse>, t: Throwable) {
                        // Show error message
                        Toast.makeText(this@EditOrderActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.d("EditOrderActivity", "Error: ${t.message}")
                    }
                })
        }

        // Set click listener for cancel button
        binding.btnCancelOrder?.setOnClickListener {
            val updatedStatus = "Cancelled"

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
            finish()
        }

        // Set up the Spinner with the array resource
        ArrayAdapter.createFromResource(
            this,
            R.array.order_status_options, // order_status_options is from value>string
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spOrderStatus.adapter = adapter

            // Set the Spinner's selection to match the passed order status
            orderStatus?.let {
                val statusOptions = resources.getStringArray(R.array.order_status_options)
                val index = statusOptions.indexOf(it)
                if (index >= 0) {
                    binding.spOrderStatus.setSelection(index)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        // Remove the logout button if it exists
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

}
