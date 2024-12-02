package com.example.viamm.models.getOngoingOrder
data class OngoingOrderResponse(
    val error: Boolean,
    val message: String,
    val orders: List<OngoingOrder>
)
