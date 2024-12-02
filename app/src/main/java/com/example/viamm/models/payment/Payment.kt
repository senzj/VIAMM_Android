package com.example.viamm.models.payment

data class Payment(
    val bookingId: String,
    val amount: Int,
    val workstation: String,
    val masseur: String
)
