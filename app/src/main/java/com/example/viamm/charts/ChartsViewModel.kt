package com.example.viamm.charts

import androidx.lifecycle.ViewModel

class ChartsViewModel: ViewModel() {
    // data of the charts

    val chartData = listOf(
        ChartData("Jan", 10),
        ChartData("Feb", 20),
        ChartData("Mar", 15),
        ChartData("Apr", 25),
        ChartData("May", 20),
        ChartData("Jun", 30),
        ChartData("Jul", 25),
        ChartData("Aug", 40),
        ChartData("Sep", 35),
        ChartData("Oct", 30),
        ChartData("Nov", 45),
        ChartData("Dec", 40),
    )
}