package com.example.viamm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityEditOrderBinding
import com.example.viamm.models.CancelOrder.CancelOrderResponse
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
        val orderId = intent.getStringExtra("BOOKING_ID")
        val orderStatus = intent.getStringExtra("BOOKING_STATUS")

        // Retrieve list of services (ParcelableArrayList)
        val services: ArrayList<Service>? = intent.getParcelableArrayListExtra("SERVICES")

        // Total Cost
        val totalCost = intent.getIntExtra("BOOKING_COST", 0)

        // Other data for debugging and future use
        // Masseur
        val masseurName = intent.getStringExtra("MASSEUR_NAME")
        val masseurAvailability = intent.getBooleanExtra("MASSEUR_IS_AVAILABLE", false)

        // Location
        val locationName = intent.getStringExtra("LOCATION_NAME")
        val locationAvailability = intent.getBooleanExtra("LOCATION_IS_AVAILABLE", false)

        // Log or use the data as needed
        Log.d("EditOrderActivity", "Order ID: $orderId")
        Log.d("EditOrderActivity", "Order Status: $orderStatus")
        Log.d("EditOrderActivity", "Total Cost: $totalCost")
        Log.d("EditOrderActivity", "Masseur Name: $masseurName")
        Log.d("EditOrderActivity", "Masseur Availability: $masseurAvailability")
        Log.d("EditOrderActivity", "Location Name: $locationName")
        Log.d("EditOrderActivity", "Location Availability: $locationAvailability")

        // Update UI components
        binding.tvOrderID?.text = orderId
        binding.tvOrderStatus?.text = orderStatus
        binding.tvTotalCost.text = "₱ $totalCost"

        // Populate the table with services dynamically
        services?.forEach { service ->
            val tableRow = TableRow(this)

            // Amount
            val amountTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                text = service.amount.toString()
            }
            tableRow.addView(amountTextView)

            // Service Name
            val serviceNameTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                text = service.name
            }
            tableRow.addView(serviceNameTextView)

            // Price
            val priceTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                text = service.price.toString()
            }
            tableRow.addView(priceTextView)

            // Add the TableRow to the TableLayout
            binding.tblOrder?.addView(tableRow)
        }

        // Set Payment button click listener to open camera (scanner Activity)
        binding.btnOrderPayment.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set click listener for cancel button
        binding.btnCancelOrder.setOnClickListener {
            val updatedStatus = "CANCELLED"

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
        binding.btnOrderBack?.setOnClickListener {
            finish()
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

data class Service(
    val amount: Int,
    val name: String,
    val price: Int,
    val type: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(amount)
        parcel.writeString(name)
        parcel.writeInt(price)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Service> {
        override fun createFromParcel(parcel: Parcel): Service {
            return Service(parcel)
        }

        override fun newArray(size: Int): Array<Service?> {
            return arrayOfNulls(size)
        }
    }
}
