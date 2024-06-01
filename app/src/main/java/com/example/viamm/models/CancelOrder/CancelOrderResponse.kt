package com.example.viamm.models.CancelOrder

data class CancelOrderResponse(
    val error: String,
    val message: String,
    val order: List<CancelOrder>
)