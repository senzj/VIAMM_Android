package com.example.viamm.models.Statistics

data class StatisticResponse(
    val error: Boolean,
    val message: String,
    val data: Statistics,
)