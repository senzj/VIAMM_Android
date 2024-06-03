package com.example.viamm.models.UpdateOrder

data class UpdateOrdersResponse(
    val error: String,
    val message: String,
    val order: List<UpdateOrder>
)