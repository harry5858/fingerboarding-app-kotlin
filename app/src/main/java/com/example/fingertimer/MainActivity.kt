package com.example.fingertimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fingertimer.ui.theme.FingerTimerTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        super.onCreate(savedInstanceState)
        setContent {
            FingerTimerTheme {
                // A surface container using the 'background' color from the theme

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val localFocusManager = LocalFocusManager.current

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    localFocusManager.clearFocus()
                                })
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CustomComponent(
                            timerState = viewModel.timerState.value
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextField(
                                modifier = Modifier.size(width = 96.dp, height = 56.dp),
                                value = viewModel.hangTime.value.toString(),
                                onValueChange = { viewModel.onHangTimeChange(it) },
                                label = {
                                    Text(text = "Hang")
                                },
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            TextField(
                                modifier = Modifier.size(width = 96.dp, height = 56.dp),
                                value = viewModel.restTime.value.toString(),
                                onValueChange = { viewModel.onRestTimeChange(it) },
                                label = {
                                    Text(text = "Rest")
                                },
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = {
                                viewModel.setTimerJob(job = viewModel::trigger73)
                            }) {
                                Text(text = "7-3")
                            }

                            Button(onClick = {
                                viewModel.setTimerJob(job = viewModel::trigger1010)
                            }) {
                                Text(text = "10-10")
                            }

                            Button(onClick = {
                                viewModel.setTimerJob(job = viewModel::trigger1050)
                            }
                            ) {
                                Text(text = "10-50")
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(onClick = {
                            viewModel.onResetClick()
                        },
                        enabled = (viewModel.timerJob != null)
                        ) {
                            Text(text = "Reset")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.setTimerJob(job = viewModel::triggerCustom)
                            }
                        ) {
                            Text(text = "Start")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomComponent(
    canvasSize: Dp = 300.dp,
    timerState: TimerState = TimerState(
        rep = 0, timeRemaining = 0, hang = false, oneRepTime = 1
    )
) {

    var animatedIndicatorValue by remember { mutableStateOf(timerState.timeRemaining.toFloat()) }
    LaunchedEffect(key1 = timerState.timeRemaining) {
        animatedIndicatorValue = timerState.timeRemaining.toFloat()
    }

    val animatedIndicatorValueTest = remember { Animatable(timerState.timeRemaining.toFloat()) }
    LaunchedEffect(key1 = timerState.timeRemaining) {
        animatedIndicatorValueTest.animateTo(timerState.timeRemaining.toFloat())
    }

    val sweepAngle by animateFloatAsState(
        targetValue = (animatedIndicatorValue / timerState.oneRepTime) * 360f,
        animationSpec = tween(500)
    )

    Column(
        modifier = Modifier
            .size(canvasSize)
            .drawBehind {
                val componentSize = size / 1.25f
                backgroundIndicator(
                    componentSize = componentSize
                )
                foregroundIndicator(
                    componentSize = componentSize,
                    sweepAngle = sweepAngle,
                    indicatorColor = if (timerState.hang) Color.Green else Color.Red
                )
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimerInfo(
            timeRemaining = timerState.timeRemaining,
            numOfRep = timerState.rep,
            hang = timerState.hang
        )
    }
}

fun DrawScope.backgroundIndicator(
    componentSize: Size
) {
    drawArc(
        size = componentSize,
        color = Color.Gray,
        alpha = 0.3f,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        style = Stroke(
            width = 100f
        ),
        topLeft = Offset(
            x = (size.width - componentSize.width) / 2f,
            y = (size.height - componentSize.height) / 2f
        )
    )
}

fun DrawScope.foregroundIndicator(
    componentSize: Size,
    sweepAngle: Float,
    indicatorColor: Color
) {
    drawArc(
        size = componentSize,
        color = indicatorColor,
        alpha = 0.8f,
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(
            width = 100f,
            cap = StrokeCap.Round
        ),
        topLeft = Offset(
            x = (size.width - componentSize.width) / 2f,
            y = (size.height - componentSize.height) / 2f
        )
    )
}

@Composable
fun TimerInfo(
    timeRemaining: Int,
    numOfRep: Int,
    hang: Boolean
) {
    Text(
        text = "rep: $numOfRep",
        fontSize = 32.sp
    )
    Text(
        text = "$timeRemaining s",
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    )
    Text(
        text = if(hang) "Hang" else "Rest",
        color = if(hang) Color.Green else Color.Red,
        fontSize = 32.sp
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FingerTimerTheme {
        CustomComponent()
    }
}