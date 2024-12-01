package com.example.viamm.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.viamm.R
import com.example.viamm.RecordActivity
import com.example.viamm.databinding.ItemOrderBinding
import com.example.viamm.models.getCompletedOrder.CompletedOrder

class CompletedOrderAdapter(
    private var orderList: List<CompletedOrder>,
    private val listener: RecordActivity
) : RecyclerView.Adapter<CompletedOrderAdapter.OrderViewHolder>() {

    @SuppressLint("ClickableViewAccessibility")
    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private val handler = Handler(Looper.getMainLooper())
        private var isTouched = false

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnTouchListener { _, event ->
                handleTouch(event)
                false
            }
            binding.root.setOnHoverListener { _, event ->
                handleTouch(event)
                false
            }
        }

        private fun handleTouch(event: MotionEvent) {
            val position = adapterPosition
            // Check if the touch event is associated with a valid item in the list
            if (position != RecyclerView.NO_POSITION) {
                val order = orderList[position] // Get the order object based on the current position
                when (event.action) {
                    // Handle ACTION_DOWN or ACTION_HOVER_ENTER (i.e., when the touch begins or the pointer enters the element)
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER -> {
                        isTouched = true // Mark the element as being touched
                        // Post a delayed Runnable to trigger TTS for the order details after a brief delay (75ms)
                        handler.postDelayed({
                            if (isTouched) {
                                // Trigger Text-to-Speech with order details for visually impaired users
                                listener.onTTSRequested("Booking ID: ${order.orderId}, Booking Status: ${order.orderStatus}, Total Amount: ${order.totalCost}")
                                Log.d("RecordAdapter", "Touch detected for Booking ID: ${order.orderId}")
                            }
                        }, 75) // 75ms delay before triggering TTS (for a quick touch/hover)
                    }

                    // Handle ACTION_UP (i.e., when the touch is released)
                    MotionEvent.ACTION_UP -> {
                        isTouched = false // Mark the element as not being touched anymore
                        // Trigger Text-to-Speech to confirm selection of the booking
                        listener.onTTSRequested("Booking ID: ${order.orderId} selected")
                        Log.d("RecordAdapter", "Booking ID: ${order.orderId} selected")
                    }

                    // Handle ACTION_CANCEL or ACTION_MOVE (i.e., touch is moved or cancelled)
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_MOVE -> {
                        isTouched = false // Mark the element as not being touched anymore
                    }

                    // Handle ACTION_HOVER_ENTER (i.e., when the pointer hovers over the item)
                    MotionEvent.ACTION_HOVER_ENTER -> {
                        isTouched = true // Mark as hovered
                        // Post a delayed Runnable to trigger TTS with order details after a longer delay (500ms)
                        handler.postDelayed({
                            if (isTouched) {
                                listener.onTTSRequested("Booking ID: ${order.orderId}, Booking Status: ${order.orderStatus}, Total Amount: ${order.totalCost}")
                                Log.d("RecordAdapter", "Hover detected for Booking ID: ${order.orderId}")
                            }
                        }, 500) // 500ms delay for hover detection
                    }

                    // Handle ACTION_HOVER_EXIT (i.e., when the pointer stops hovering over the item)
                    MotionEvent.ACTION_HOVER_EXIT -> {
                        isTouched = false // Mark as not being hovered anymore
                    }
                }
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
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateOrders(newOrders: List<CompletedOrder>) {
        orderList = newOrders
        notifyDataSetChanged()
    }

    interface RVListEvent {
        fun onItemClicked(position: Int)
        fun onTTSRequested(text: String)
    }
}