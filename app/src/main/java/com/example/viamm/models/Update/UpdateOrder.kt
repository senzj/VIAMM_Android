package com.example.viamm.models.Update

data class UpdateOrder(
    val orderId: Int,
    val orderService: String,
    val orderStatus: String,
    val orderEmpName: String
)