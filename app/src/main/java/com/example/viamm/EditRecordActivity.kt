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
import com.example.viamm.databinding.ActivityEditRecordBinding
import com.example.viamm.models.CancelOrder.CancelOrderResponse
import com.example.viamm.models.UpdateOrder.UpdateOrdersResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecordBinding
    private lateinit var api: Api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        api = RetrofitClient.instance

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

        // Update the UI with the retrieved data
        binding.tvOrderID.text = orderId
        binding.ETOrderService.setText(orderService)
        binding.ETOrderEmpName.setText(orderEmpName)

        // Set click listener for Edit button
        binding.btnRecordEdit.setOnClickListener {
            val updatedService = binding.ETOrderService.text.toString()
            val updatedEmpName = binding.ETOrderEmpName.text.toString()
            val updatedStatus = binding.spOrderStatus.selectedItem.toString()

            orderId?.let { orderId ->
                api.updateOrder(orderId, updatedService, updatedEmpName, updatedStatus)
                    .enqueue(object : Callback<UpdateOrdersResponse> {
                        override fun onResponse(
                            call: Call<UpdateOrdersResponse>,
                            response: Response<UpdateOrdersResponse>
                        ) {
                            if (response.isSuccessful) {
                                // Show success message
                                Toast.makeText(
                                    this@EditRecordActivity,
                                    "Order ID \"$orderId\" Updated Successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d(
                                    "EditOrderActivity",
                                    "Order Updated Successfully! Redirecting to OrderActivity"
                                )

                                // Set result for the previous activity
                                val resultIntent = Intent()
                                resultIntent.putExtra("UPDATED_STATUS", updatedStatus)
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            } else {
                                handleErrorResponse(response)
                            }
                        }

                        override fun onFailure(call: Call<UpdateOrdersResponse>, t: Throwable) {
                            handleFailure(t)
                        }
                    })
            }
        }


        // Set click listener for back button
        binding.btnRecordBack.setOnClickListener {
            finish()
        }

        // Set up the Spinner with the array resource
        ArrayAdapter.createFromResource(
            this,
            R.array.order_status_options,
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

    private fun handleErrorResponse(response: Response<*>) {
        val errorBody = response.errorBody()?.string()
        val errorMessage = errorBody ?: "An Error Occurred. Failed to Update the Requested Order."
        Toast.makeText(this@EditRecordActivity, errorMessage, Toast.LENGTH_LONG).show()
        Log.d("EditOrderActivity", "An Error Occurred $response.")
    }

    private fun handleFailure(t: Throwable) {
        Toast.makeText(this@EditRecordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
        Log.d("EditOrderActivity", "Error: ${t.message}")
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


