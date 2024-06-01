package com.example.viamm.models.CancelOrder

import com.example.viamm.models.UpdateOrder.UpdateOrder

data class CancelOrder (
    val orderId: Int,
    val orderStatus: String
)