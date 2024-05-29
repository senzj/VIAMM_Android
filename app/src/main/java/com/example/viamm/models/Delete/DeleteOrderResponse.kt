package com.example.viamm.models.Delete

data class DeleteOrderResponse(
    val error: String,
    val message: String,
    val orderId: DeleteOrder
)