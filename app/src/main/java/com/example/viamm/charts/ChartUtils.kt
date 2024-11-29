package com.example.viamm.charts

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

object ChartUtils {
    // custom size of the chart
    val DEFAULT_CHART_HEIGHT = 340.dp

    val DEFAULT_CHART_PADDING = 18.dp

    //random color function for gradient
    fun randomGradient(): Color {
        return Color(
            red = Random.nextInt(150),
            green = Random.nextInt(150),
            blue = Random.nextInt(150)
        )
    }
}