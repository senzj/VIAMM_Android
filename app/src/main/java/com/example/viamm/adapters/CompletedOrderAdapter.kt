package com.example.viamm.adapters

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.viamm.RecordActivity
import com.example.viamm.databinding.ItemOrderBinding
import com.example.viamm.models.Order.Orders

class CompletedOrderAdapter(
    private var orderList: List<Orders>,
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
            if (position != RecyclerView.NO_POSITION) {
                val order = orderList[position]
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER -> {
                        isTouched = true
                        handler.postDelayed({
                            if (isTouched) {
                                listener.onTTSRequested("Booking ID: ${order.orderId}, Booking Status: ${order.orderStatus}, Total Amount: ${order.totalCost}")
                                Log.d("RecordAdapter", "Touch detected for Booking ID: ${order.orderId}")
                            }
                        }, 70) //
                    }
                    MotionEvent.ACTION_UP -> {
                        isTouched = false
                        listener.onTTSRequested("Booking ID: ${order.orderId} selected")
                        Log.d("RecordAdapter", "Booking ID: ${order.orderId} selected")
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_MOVE -> {
                        isTouched = false
                    }
                    MotionEvent.ACTION_HOVER_ENTER -> {
                        isTouched = true
                        handler.postDelayed({
                            if (isTouched) {
                                listener.onTTSRequested("Booking ID: ${order.orderId}, Booking Status: ${order.orderStatus}, Total Amount: ${order.totalCost}")
                                Log.d("RecordAdapter", "Hover detected for Booking ID: ${order.orderId}")
                            }
                        }, 500) // 500ms delay
                    }
                    MotionEvent.ACTION_HOVER_EXIT -> {
                        isTouched = false
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
            tvOrderStatus.text = "Booking Status: ${currentOrder.orderStatus}"
            tvOrderTotal.text = "Total Amount: ${currentOrder.totalCost}"
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateOrders(newOrders: List<Orders>) {
        orderList = newOrders
        notifyDataSetChanged()
    }

    interface RVListEvent {
        fun onItemClicked(position: Int)
        fun onTTSRequested(text: String)
    }
}