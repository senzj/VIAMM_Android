package com.example.viamm.models.getOngoingOrder
data class OngoingOrderResponse(
    val error: Boolean,
    val message: String,
    val orders: List<OngoingOrder>
    // The orders is a List of objects, not a single object
)
