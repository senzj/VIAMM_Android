package com.example.viamm.models.Login

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val user: User
)