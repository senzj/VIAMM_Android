package com.example.viamm.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.viamm.databinding.ItemOrderBinding
import com.example.viamm.models.Order.Orders

class OrderAdapter(private var orderList: List<Orders>, private val listener: RVListEvent) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
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
            tvOrderId.text = "Order ID: ${currentOrder.orderId}"
            tvOrderService.text = "Service: ${currentOrder.orderService}"
            tvOrderEmpName.text = "Employee Name: ${currentOrder.orderEmpName}"
            tvOrderStatus.text = "Status: ${currentOrder.orderStatus}"
        }
        Log.d("OrderAdapter", "onBindViewHolder: $currentOrder")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateOrders(newOrders: List<Orders>) {
        orderList = newOrders
        notifyDataSetChanged()
    }

    interface RVListEvent {
        fun onItemClicked(position: Int)
    }
}
