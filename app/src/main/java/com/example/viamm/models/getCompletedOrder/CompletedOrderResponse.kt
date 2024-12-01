package com.example.viamm.models.getCompletedOrder

data class CompletedOrderResponse(
    val error: Boolean,
    val message: String,
    val orders: List<CompletedOrder>
)
