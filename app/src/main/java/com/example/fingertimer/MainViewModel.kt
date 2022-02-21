package com.example.fingertimer

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class TimerState(
    val rep: Int,
    val timeRemaining: Int,
    val hang: Boolean,
    val oneRepTime: Int
)

class MainViewModel: ViewModel() {

    var hangTime = mutableStateOf("0")
    private set

    var restTime = mutableStateOf("0")
    private set

    private val initialTimerState = TimerState(
        rep = 0, timeRemaining = 0, hang = false, oneRepTime = 1
    )

    var timerState = mutableStateOf(initialTimerState)
    private set

    var timerJob: Job? = null

    fun onHangTimeChange(input: String) {
        hangTime.value = input
    }

    fun onRestTimeChange(input: String) {
        restTime.value = input
    }

    fun triggerCustom() = flow<TimerState> {
        try {
            var hangTimeInInt: Int = 0
            var restTimeInInt: Int = 0

            if (hangTime.value == "") {
                hangTime.value = "0"
            } else if (restTime.value == "") {
                restTime.value = "0"
            } else {
                hangTime.value = hangTime.value.toFloat().roundToInt().toString()
                restTime.value = restTime.value.toFloat().roundToInt().toString()
                hangTimeInInt = hangTime.value.toFloat().roundToInt()
                restTimeInInt = restTime.value.toFloat().roundToInt()
            }

            val totalTime = hangTimeInInt + restTimeInInt

            if (restTimeInInt < 0 || hangTimeInInt < 0) {
                hangTimeInInt = abs(hangTimeInInt)
                restTimeInInt = abs(restTimeInInt)
            } else if (restTimeInInt == 0 && hangTimeInInt == 0) {
                Unit
            } else if (restTimeInInt == 0 && hangTimeInInt != 0) {
                for (i in totalTime downTo 0) {
                    delay(1000L)
                    emit(TimerState(rep = 1, timeRemaining = i, hang = true, oneRepTime = totalTime))
                }
            } else if (restTimeInInt != 0 && hangTimeInInt == 0) {
                for (i in totalTime downTo 0) {
                    delay(1000L)
                    emit(TimerState(rep = 1, timeRemaining = i, hang = false, oneRepTime = totalTime))
                }
            } else {
                for (i in totalTime downTo restTimeInInt) {
                    delay(1000L)
                    emit(TimerState(rep = 1, timeRemaining = i, hang = true, oneRepTime = totalTime))
                }
                for (i in (restTimeInInt - 1) downTo 0) {
                    delay(1000L)
                    emit(TimerState(rep = 1, timeRemaining = i, hang = false, oneRepTime = totalTime))
                }
            }
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }

    fun trigger73(): Flow<TimerState> = flow {
        repeat(6) {
            for (i in 10 downTo 3) {
                delay(1000L)
                emit(TimerState(rep = it+1, timeRemaining = i, hang = true, oneRepTime = 10))
            }
            for (i in 2 downTo 0) {
                delay(1000L)
                emit(TimerState(rep = it+1, timeRemaining = i, hang = false, oneRepTime = 10))
            }
        }
    }

    fun trigger1010(): Flow<TimerState> = flow {
        repeat(3) {
            for (i in 20 downTo 10) {
                delay(1000L)
                emit(TimerState(rep = it+1, timeRemaining = i, hang = true, oneRepTime = 20))
            }
            for (i in 9 downTo 0) {
                delay(1000L)
                emit(TimerState(rep = it+1, timeRemaining = i, hang = false, oneRepTime = 20))
            }
        }
    }

    fun trigger1050() = flow<TimerState> {
        for (i in 60 downTo 50) {
            delay(1000L)
            emit(TimerState(rep = 1, timeRemaining = i, hang = true, oneRepTime = 60))
        }
        for (i in 49 downTo 0) {
            delay(1000L)
            emit(TimerState(rep = 1, timeRemaining = i, hang = false, oneRepTime = 60))
        }
    }

    fun onResetClick() {
        timerJob?.cancel()
        timerState.value = initialTimerState
    }

    @OptIn(InternalCoroutinesApi::class)
    fun setTimerJob(job:() -> Flow<TimerState>) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            job().collect {
                timerState.value = it
            }
        }
    }

}