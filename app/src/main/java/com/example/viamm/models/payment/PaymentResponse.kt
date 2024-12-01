package com.example.viamm.models.payment

import com.example.viamm.models.Login.User

data class PaymentResponse(
val error: Boolean,
val message: String,
val paymentDetails: Payment,

)
