package com.example.viamm.adapters

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
                            }, 500) // 500ms delay
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
            tvOrderStatus.text = "Booking Status: ${currentOrder.orderStatus}"
            tvOrderTotal.text = "Total Amount: ${currentOrder.totalCost}"
        }
        Log.d("OrderAdapter", "onBindViewHolder: $currentOrder")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateOrders(newOrders: List<OngoingOrder>) {
        orderList = newOrders
        notifyDataSetChanged()
    }

    interface RVListEvent {
        fun onItemClicked(position: Int)
        fun onTTSRequested(text: String)
    }
}