package com.example.viamm.charts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viamm.charts.ChartUtils.DEFAULT_CHART_HEIGHT
import com.example.viamm.charts.ChartUtils.DEFAULT_CHART_PADDING
import kotlin.math.roundToInt


@Suppress("UNREACHABLE_CODE")
class AnalyticsActivity: ComponentActivity() {

    private val viewModel by viewModels<ChartsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                //static
//                Column {
////                    Text("Hello World")
//                    // passing the data to LineChart
//                    LineChart(viewModel.chartData)
//                }

                //dynamic
                LazyColumn (
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(viewModel.chartData2){
                        index, data ->
                        Text("Chart $index")
                        LineChart(
                            data = data,
                            chartColor = ChartUtils.randomGradient()
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun LineChart(
    data: List<ChartData>,
    chartColor: Color
){

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
        val gridHeight = height.toPx() - yAxisPadding.toPx() * 3
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
        val xAxisSpacing = (gridWidth - xAxisPadding.toPx()) / (maxPointSize - 1)
        val yAxisSpacing = gridHeight / (yAxisLabelList.size - 1)

        // drawing the lines that connect the plots
        offsetList.clear()
        for(i in 0 until maxPointSize){
            val x = (i * xAxisSpacing) + xAxisPadding.toPx()
            val y = gridHeight - (yAxisSpacing * (yAxisData[i].toFloat() / verticalStepper))

            // line path
            offsetList.add(
                Offset(x,y)
            )
        }

        // drawing vertical grid
        for (i in 0 until maxPointSize){
            val xOffset = (xAxisSpacing * i) + xAxisPadding.toPx()
            drawLine(
                color = Color.Gray,
                start = Offset(xOffset,0f),
                end = Offset(xOffset,gridHeight),
                strokeWidth = 2f
            )
        }

        // drawing horizontal grid
        for(i in 0 until yAxisLabelList.size){
            val offsetx = xAxisPadding.toPx()
            val offsety = gridHeight - (yAxisSpacing * i)

            drawLine(
                color = Color.Gray,
                start = Offset(offsetx,offsety),
                end = Offset(gridWidth,offsety),
                strokeWidth = 2f

            )
        }

        // text labels for x axis
        for(i in 0 until maxPointSize){
            val xOffset = (xAxisSpacing * i) + xAxisPadding.toPx()
            drawContext.canvas.nativeCanvas.drawText(
                xAxisData[i],
                xOffset,
                size.height,
                Paint().apply {
                    color = Color.Gray.toArgb()
                    textAlign = Paint.Align.CENTER
                    textSize = 18.sp.toPx()
                }
            )
        }

        // text labels for y axis
        for(i in 0 until yAxisLabelList.size){
            drawContext.canvas.nativeCanvas.drawText(
                yAxisLabelList[i],
                0f,
                gridHeight - (yAxisSpacing * i),
                Paint().apply {
                    color = Color.Gray.toArgb()
                    textAlign = Paint.Align.CENTER
                    textSize = 18.sp.toPx()
                }
            )
        }

        // draw points
        offsetList.forEachIndexed { index, offset ->
            drawCircle(
                color = chartColor,
                radius = 5.dp.toPx(),
                center = offset
            )
        }

        // lines
        drawPoints(
            points = offsetList,
            pointMode = PointMode.Polygon,
            color = chartColor,
            strokeWidth = 4f
        )

        val gradientPath = Path().apply {
            moveTo(xAxisPadding.toPx(), gridHeight)

            for (i in 0 until maxPointSize){
                lineTo(offsetList[i].x, offsetList[i].y)
            }

            lineTo(gridWidth,gridHeight)
            close()
        }

        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(chartColor, Color.Transparent)
            )
        )

    }
}