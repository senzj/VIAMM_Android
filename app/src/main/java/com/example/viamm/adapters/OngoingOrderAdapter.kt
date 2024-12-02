package com.example.viamm.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.viamm.R
import com.example.viamm.databinding.ItemOrderBinding
import com.example.viamm.models.getOngoingOrder.OngoingOrder

class OngoingOrderAdapter(private var orderList: List<OngoingOrder>, private val listener: RVListEvent) : RecyclerView.Adapter<OngoingOrderAdapter.OrderViewHolder>() {

    @SuppressLint("ClickableViewAccessibility")
    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private val handler = Handler(Looper.getMainLooper())
        private var isTouched = false

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnTouchListener { _, event ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val order = orderList[position]
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isTouched = true
                            handler.postDelayed({
                                if (isTouched) {
                                    listener.onTTSRequested("Booking ID: ${order.orderId}, Booking Status: ${order.orderStatus}, Total Amount: ${order.totalCost}")
                                    Log.d("OrderAdapter", "Touch detected for Booking ID: ${order.orderId}")
                                }
                            }, 70) // 500ms delay
                        }
                        MotionEvent.ACTION_UP -> {
                            isTouched = false
                            listener.onTTSRequested("Booking ID: ${order.orderId} selected")
                            Log.d("OrderAdapter", "Booking ID: ${order.orderId} selected")
                        }
                        MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_MOVE -> {
                            isTouched = false
                        }
                    }
                }
                false
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClicked(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(
            ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val currentOrder = orderList[position]
        holder.binding.apply {
            tvOrderId.text = "Booking ID: ${currentOrder.orderId}"
            tvOrderMasseur.text = "Masseur: ${currentOrder.masseur.masseurName}"
            tvOrderStatus.text = currentOrder.orderStatus
            tvOrderTotal.text = "Total Amount: ₱${currentOrder.totalCost}"


            // Safely access context from parent, ensuring it's not null
            val context = tvOrderStatus.context

            // Using context to set the color
            when (currentOrder.orderStatus) {
                "COMPLETED" -> {
                    tvOrderStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.Status_Complete)
                    ) // Green for completed
                    tvOrderStatus.setTypeface(null, Typeface.BOLD) // Make it bold
                }
                "CANCELLED" -> {
                    tvOrderStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.Status_Cancelled)
                    ) // Red for cancelled
                    tvOrderStatus.setTypeface(null, Typeface.BOLD) // Make it bold
                }
                "ON-GOING" -> {
                    tvOrderStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.Status_Ongoing)
                    ) // Orange for on-going
                    tvOrderStatus.setTypeface(null, Typeface.BOLD) // Make it bold
                }
                else -> {
                    tvOrderStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.black)
                    ) // Default to black if no match
                    tvOrderStatus.setTypeface(null, Typeface.BOLD) // Make it bold
                }
            }
        }
        Log.d("ongoingOrderAdapter", "onBindViewHolder: $currentOrder")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateOrders(newOrders: List<OngoingOrder>) {
        orderList = newOrders
        notifyDataSetChanged()
    }

    // Define the interface for click events
    interface RVListEvent {
        fun onItemClicked(position: Int)
        fun onTTSRequested(text: String)
    }
}