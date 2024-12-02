package com.example.viamm.models.CancelOrder

import com.example.viamm.models.UpdateOrder.UpdateOrder

data class CancelOrder (
    val orderId: Int,
    val workstation: String,
    val masseur: String,
)