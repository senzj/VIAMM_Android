package com.example.viamm

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.viamm.api.Api
import com.example.viamm.api.RetrofitClient
import com.example.viamm.databinding.ActivityEditRecordBinding
import retrofit2.Response

class EditRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecordBinding
    private lateinit var api: Api

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        binding = ActivityEditRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.StatusBarColor)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        api = RetrofitClient.instance

        // Retrieve the data from the Intent
        val orderId = intent.getStringExtra("BOOKING_ID")
        val orderStatus = intent.getStringExtra("BOOKING_STATUS")
        val services: ArrayList<ServiceRecord>? = intent.getParcelableArrayListExtra("SERVICES")
        val totalCost = intent.getIntExtra("BOOKING_COST", 0)

//        for future uses
//        val masseurName = intent.getStringExtra("MASSEUR_NAME")
//        val masseurAvailability = intent.getBooleanExtra("MASSEUR_IS_AVAILABLE", false)
//        val locationName = intent.getStringExtra("LOCATION_NAME")
//        val locationAvailability = intent.getBooleanExtra("LOCATION_IS_AVAILABLE", false)

        Log.d("EditRecordActivity", "Booking ID: $orderId")
        Log.d("EditRecordActivity", "Booking Status: $orderStatus")
        Log.d("EditRecordActivity", "Total Cost: $totalCost")

        binding.tvRecordID.text = "Booking ID: $orderId"
        binding.tvRecordStatus.text = "Booking Status: $orderStatus"
        binding.tvTotalCost.text = "₱ $totalCost"

        services?.forEach { service ->
            val tableRow = TableRow(this)
            Log.d("EditRecordActivity", "Service: $service")

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.DKGRAY)
            })

            // Service Amount
            val amountTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                text = service.amount.toString()
                gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx()) // Set padding for text
            }
            tableRow.addView(amountTextView)

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.DKGRAY)
            })

            // Service Name
            val serviceNameTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                text = service.name
                gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx()) // Set padding for text
            }
            tableRow.addView(serviceNameTextView)

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.DKGRAY)
            })

            // Service Price
            val priceTextView = TextView(this).apply {
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                text = service.price.toString()
                gravity = Gravity.CENTER_HORIZONTAL // Center the text horizontally
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx()) // Set padding for text
            }
            tableRow.addView(priceTextView)

            // Add vertical line
            tableRow.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(1.dpToPx(), TableRow.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.DKGRAY)
            })

            binding.tblRecord?.addView(tableRow)

            // Add horizontal line after each row
            binding.tblRecord?.addView(View(this).apply {
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1.dpToPx())
                setBackgroundColor(Color.DKGRAY)
            })
        }

        binding.btnRecordBack.setOnClickListener {
            finish()
        }
    }

    // Extension function to convert dp to pixels
    fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun handleErrorResponse(response: Response<*>) {
        val errorBody = response.errorBody()?.string()
        val errorMessage = errorBody ?: "An Error Occurred. Failed to Update the Requested Order."
        Toast.makeText(this@EditRecordActivity, errorMessage, Toast.LENGTH_LONG).show()
        Log.d("EditRecordActivity", "An Error Occurred $response.")
    }

    private fun handleFailure(t: Throwable) {
        Toast.makeText(this@EditRecordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
        Log.d("EditRecordActivity", "Error: ${t.message}")
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
}

data class ServiceRecord(
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

    companion object CREATOR : Parcelable.Creator<ServiceRecord> {
        override fun createFromParcel(parcel: Parcel): ServiceRecord {
            return ServiceRecord(parcel)
        }

        override fun newArray(size: Int): Array<ServiceRecord?> {
            return arrayOfNulls(size)
        }
    }
}
