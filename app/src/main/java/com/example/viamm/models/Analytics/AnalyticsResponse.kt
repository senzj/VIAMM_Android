package com.example.viamm.models.Analytics

data class AnalyticsResponse(
    val error: Boolean,
    val message: String,
    val data: Analytics,
)