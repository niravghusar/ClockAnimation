package com.example.clockanimation


import android.os.Bundle
import androidx.compose.runtime.Composable

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.clockanimation.ui.theme.ClockAnimationTheme

import kotlinx.coroutines.delay
import java.util.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClockAnimationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                 ) {
                    ClockAnimation()
                }
            }
        }
    }
}

@Composable
fun ClockAnimation() {
    val calendar = Calendar.getInstance()
    var currentSecond by remember { mutableStateOf(calendar.get(Calendar.SECOND)) }
    var currentMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var currentHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }

    val infiniteTransition = rememberInfiniteTransition()
    val animatedColors = listOf(Color.Red, Color.Blue, Color.Green, Color.Black)

    val colorStops = animatedColors.mapIndexed { index, color ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        ).value to color
    }

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )


    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            calendar.timeInMillis = System.currentTimeMillis()
            currentSecond = calendar.get(Calendar.SECOND)
            currentMinute = calendar.get(Calendar.MINUTE)
            currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        }
    }

    val secondsAngle by animateFloatAsState(
        targetValue = (currentSecond * 6).toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    val minutesAngle by animateFloatAsState(
        targetValue = (currentMinute * 6 + currentSecond / 10f).toFloat(),
        animationSpec = tween(durationMillis = 60000, easing = LinearEasing)
    )

    val hoursAngle by animateFloatAsState(
        targetValue = ((currentHour % 12) * 30 + currentMinute / 2f).toFloat(),
        animationSpec = tween(durationMillis = 3600000, easing = LinearEasing)
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width / 2

            val centerX = size.width / 2
            val centerY = size.height / 2
            val offsetx = (centerX - (width/2))
            val offsety = (centerY - (width/2))

            val radius = width / 2f

            val brush = Brush.sweepGradient(
                colorStops = colorStops.toTypedArray(),
                center = Offset(size.width / 2, size.height / 2),
            )

            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                false,
                topLeft = Offset(offsetx,offsety),
                style = Stroke(width = 10f),
                size = Size(width, width)
            )

            drawArc(
                brush = Brush.sweepGradient(colorStops = colorStops.toTypedArray()),
                startAngle = 0f,
                sweepAngle = 360f,
                false,
                topLeft = Offset(offsetx,offsety),
                style = Stroke(width = 10f),
                size = Size(width, width)
            )

            val path = Path().apply {
                val segments = 1000
                for (i in 0..segments) {
                    val angle = i * 2 * Math.PI / segments
                    val waveOffset = (Random.nextFloat() * 20) * kotlin.math.sin(phase + angle).toFloat()
                    val x = (centerX + (radius + waveOffset) * kotlin.math.cos(angle).toFloat()).toFloat()
                    val y = (centerY + (radius + waveOffset) * kotlin.math.sin(angle).toFloat()).toFloat()
                    if (i == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
                close()
            }

            drawPath(
                path = path,
                color = Color(0xFF2A468F),
                style = Stroke(width = 4.dp.toPx())
            )



            // Draw hour hand
            drawLine(
                color = Color.Black,
                start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                end = androidx.compose.ui.geometry.Offset(
                    x = centerX + radius / 2 * kotlin.math.cos((hoursAngle - 90) * (Math.PI / 180)).toFloat(),
                    y = centerY + radius / 2 * kotlin.math.sin((hoursAngle - 90) * (Math.PI / 180)).toFloat()
                ),
                strokeWidth = 8.dp.toPx()
            )

            // Draw minute hand
            drawLine(
                color = Color.Black,
                start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                end = androidx.compose.ui.geometry.Offset(
                    x = centerX + radius * 0.7f * kotlin.math.cos((minutesAngle - 90) * (Math.PI / 180)).toFloat(),
                    y = centerY + radius * 0.7f * kotlin.math.sin((minutesAngle - 90) * (Math.PI / 180)).toFloat()
                ),
                strokeWidth = 6.dp.toPx()
            )

            // Draw second hand
            drawLine(
                color = Color.Red,
                start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                end = androidx.compose.ui.geometry.Offset(
                    x = centerX + radius * kotlin.math.cos((secondsAngle - 90) * (Math.PI / 180)).toFloat(),
                    y = centerY + radius * kotlin.math.sin((secondsAngle - 90) * (Math.PI / 180)).toFloat()
                ),
                strokeWidth = 4.dp.toPx()
            )
        }
    }
}
