package com.example.health

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.AppPrefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepSensorController(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs = AppPrefs(context)

    var currentSensorSteps: Long = 0L
        private set

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun hasSensor(): Boolean = stepSensor != null

    fun startListening(onStepUpdate: (Long) -> Unit) {
        if (stepSensor == null) return
        this.onStepUpdate = onStepUpdate
        sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
    }

    private var onStepUpdate: ((Long) -> Unit)? = null

    fun stopListening() {
        sensorManager?.unregisterListener(this)
        onStepUpdate = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toLong()
            val todayStr = dateFormat.format(Date())

            val savedDate = prefs.stepSensorDate
            var base = prefs.stepSensorBase

            if (savedDate != todayStr || base <= 0 || totalSteps < base) {
                // New day or first run: set today's base
                base = totalSteps
                prefs.stepSensorBase = base
                prefs.stepSensorDate = todayStr
            }

            val todaySteps = (totalSteps - base).coerceAtLeast(0L)
            currentSensorSteps = todaySteps
            onStepUpdate?.invoke(todaySteps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
