package com.example.viamm.charts

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.viamm.charts.ChartUtils.DEFAULT_CHART_HEIGHT
import com.example.viamm.charts.ChartUtils.DEFAULT_CHART_PADDING
import kotlin.math.roundToInt


@Suppress("UNREACHABLE_CODE")
class AnalyticsActivity: ComponentActivity() {

    private val viewModel by viewModels<ChartsViewModel>()

    override fun onCreateDescription(): CharSequence? {
        return super.onCreateDescription()
        setContent {
            Surface {
                // passing the data to LineChart
                LineChart(viewModel.chartData)
            }
        }
    }
}

@Composable
fun LineChart(data: List<ChartData>){

    //setting the chart canvas
    val height = DEFAULT_CHART_HEIGHT
    val xAxisPadding = DEFAULT_CHART_PADDING
    val yAxisPadding = DEFAULT_CHART_PADDING

    // data for chart layout
    val xAxisData = data.map { it.x }
    val yAxisData = data.map { it.y }

    // data for charts for plotting
    val offsetList = remember{
        mutableListOf<Offset>()
    }

    // chart canvas
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .padding(DEFAULT_CHART_PADDING)
    ) {

        // drawing the chart axis
        val gridHeight = height.toPx()
        val gridWidth = size.width // size is provided by the drawable scope

        // plotting the data to the chart
        val maxPointSize = yAxisData.distinct().size
        val absMaxYPoint = yAxisData.maxByOrNull {
            it.toFloat().roundToInt()
        } ?: 0

        // calculating the vertical stepper, which is used to plot the points
        val verticalStepper = (absMaxYPoint.toInt() /*the value here is Int*/ / maxPointSize).toFloat() //converting the value to float for ease of use


        val yAxisLabelList = mutableListOf<String>()
        for(i in 0 .. maxPointSize){
            val interval = (i * verticalStepper).roundToInt()
            yAxisLabelList.add(interval.toString())
        }

        // spacing of the lines
        val xAxisSpacing = gridWidth / (maxPointSize - 1)
        val yAxisSpacing = gridHeight / (yAxisLabelList.size - 1)

        // drawing the lines that connect the plots
        offsetList.clear()
        for(i in 0 until maxPointSize){
            val x = (i * xAxisSpacing)
            val y = gridHeight - (yAxisSpacing * (yAxisData[i].toFloat() / verticalStepper))

            // line path
            offsetList.add(
                Offset(x,y)
            )
        }

        // drawing the grid
        for (i in 0 until maxPointSize){
            val xOffset = (xAxisSpacing * i)
            drawLine(
                color = Color.Gray,
                start = Offset(xOffset,0f),
                end = Offset(xOffset,gridHeight),
                strokeWidth = 2f
            )
        }
    }
}