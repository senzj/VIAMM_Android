package com.example.viamm.models.Update

data class UpdateOrdersResponse(
    val error: String,
    val message: String,
    val order: List<UpdateOrder>
)