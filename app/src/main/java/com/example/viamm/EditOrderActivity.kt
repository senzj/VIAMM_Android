package com.example.viamm

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityEditOrderBinding
import com.example.viamm.models.Delete.DeleteOrderResponse
import com.example.viamm.models.Update.UpdateOrdersResponse
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
        binding.btnOrderEdit.setOnClickListener {
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

                            // Redirect to OrderActivity
                            startActivity(Intent(this@EditOrderActivity, OrderActivity::class.java))
                            finish()
                            Log.d("EditOrderActivity", "Redirected to OrderActivity \nResponse: $response")

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

        // Set click listener for Delete button
        binding.btnOrderDelete.setOnClickListener {
            showConfirmationDialog(orderId)
        }

//      Retrieve specific data======================================================================

        // Set up the Spinner with the array resource
        ArrayAdapter.createFromResource(
            this,
            R.array.order_status_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spOrderStatus.adapter = adapter
        }

        // Set the Spinner's selection to match the passed order status
        orderStatus?.let {
            val statusOptions = resources.getStringArray(R.array.order_status_options)
            val index = statusOptions.indexOf(it)
            if (index >= 0) {
                binding.spOrderStatus.setSelection(index)
            }
        }
    }
//    Performing Delete Operation

    private fun showConfirmationDialog(orderId: String?) {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to delete Order ID $orderId?")
            .setPositiveButton("Delete") { dialogInterface: DialogInterface, i: Int ->
                // Perform delete operation here
                performDeleteOperation(orderId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to perform delete operation
    private fun performDeleteOperation(orderId: String?) {
        // Call the deleteOrder API endpoint
        api.deleteOrder(orderId!!)
            .enqueue(object : Callback<DeleteOrderResponse> {
                override fun onResponse(
                    call: Call<DeleteOrderResponse>,
                    response: Response<DeleteOrderResponse>
                ) {
                    if (response.isSuccessful) {
                        // Show success message
                        Toast.makeText(this@EditOrderActivity, "Order ID $orderId Successfully Deleted!", Toast.LENGTH_SHORT).show()

                        // Redirect to OrderActivity
                        startActivity(Intent(this@EditOrderActivity, OrderActivity::class.java))
                        finish() // Finish current activity to prevent navigating back to it with back button
                    } else {
                        // Show error message
                        Toast.makeText(this@EditOrderActivity, "Failed to delete order. ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DeleteOrderResponse>, t: Throwable) {
                    // Show error message
                    Toast.makeText(this@EditOrderActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
