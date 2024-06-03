package com.example.viamm.models.Order

data class OrderResponse(
    val error: Boolean,
    val message: String,
    val orders: List<Orders>
)
