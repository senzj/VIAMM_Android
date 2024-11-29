package com.example.viamm.charts

import androidx.lifecycle.ViewModel

class ChartsViewModel: ViewModel() {
    // data of the charts

    // single data sets
    val chartData = listOf(
        ChartData("Jan", 12),
        ChartData("Feb", 20),
        ChartData("Mar", 13),
        ChartData("Apr", 25),
        ChartData("May", 20),
        ChartData("Jun", 30),
        ChartData("Jul", 25),
        ChartData("Aug", 4),
        ChartData("Sep", 35),
        ChartData("Oct", 70),
        ChartData("Nov", 45),
        ChartData("Dec", 8),
    )

    // multiple data sets
    val chartData2 = listOf(
        listOf(
            ChartData("Jan", 12),
            ChartData("Feb", 20),
            ChartData("Mar", 13),
        ),
        listOf(
            ChartData("Apr", 25),
            ChartData("May", 20),
            ChartData("Jun", 30),
        ),
    )
}