package com.example.airsoftshottimer.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.android.gms.wearable.Wearable
import com.example.airsoftshottimer.presentation.theme.AirsoftShotTimerTheme
import com.example.airsoftshottimer.presentation.pages.TimerPage
import com.example.airsoftshottimer.presentation.pages.SettingsPage
import com.example.airsoftshottimer.presentation.pages.ResetPage
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    private val viewModel: ShotTimerViewModel by viewModels()
    private var audioRecognizer: AudioRecognizer? = null
    private var accelerometerRecognizer: AccelerometerRecognizer? = null
    private var startTime: Long = 0

    private fun sendShotTime(time: Double) {
        val message = String.format("%.2f", time)
        val messageClient = Wearable.getMessageClient(this)
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/shot_time", message.toByteArray())
                    .addOnSuccessListener { Log.d("MainActivity", "Sent: $message") }
            }
        }
    }

    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun performVibration(effect: VibrationEffect) {
        try {
            getVibrator().vibrate(effect)
        } catch (e: Exception) {
            Log.e("MainActivity", "Vibration failed: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            WearApp(
                viewModel = viewModel,
                onStartDetection = { startDetection() },
                onStopDetection = { stopDetection() },
                onSendTime = { sendShotTime(it) }
            )
        }
    }

    private fun startDetection() {
        if (viewModel.detectionMode == DetectionMode.MICROPHONE) {
            audioRecognizer = AudioRecognizer(this, viewModel.noiseThresholdDb) { stopAndSend() }
            audioRecognizer?.startRecognition()
        } else {
            accelerometerRecognizer = AccelerometerRecognizer(this, viewModel.accelerationThreshold) { stopAndSend() }
            accelerometerRecognizer?.startRecognition()
        }
    }

    private fun stopDetection() {
        audioRecognizer?.stopRecognition()
        accelerometerRecognizer?.stopRecognition()
    }

    private fun stopAndSend() {
        if (viewModel.timerState == TimerState.RUNNING) {
            viewModel.timerState = TimerState.STOPPED
            viewModel.elapsedTime = System.currentTimeMillis() - startTime
            stopDetection()
            
            // 偵測到射擊時：發出雙擊震動
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                performVibration(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
                performVibration(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            
            sendShotTime(viewModel.elapsedTime / 1000.0)
        }
    }

    @Composable
    fun WearApp(
        viewModel: ShotTimerViewModel,
        onStartDetection: () -> Unit,
        onStopDetection: () -> Unit,
        onSendTime: (Double) -> Unit
    ) {
        val pagerState = rememberPagerState(pageCount = { 2 })

        androidx.compose.runtime.LaunchedEffect(viewModel.timerState) {
            if (viewModel.timerState == TimerState.RUNNING) {
                startTime = System.currentTimeMillis()
                while (viewModel.timerState == TimerState.RUNNING) {
                    viewModel.elapsedTime = System.currentTimeMillis() - startTime
                    kotlinx.coroutines.delay(10)
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose { onStopDetection() }
        }

        AirsoftShotTimerTheme {
            HorizontalPager(
                state = pagerState, 
                modifier = Modifier.fillMaxSize().background(Color.Black)
            ) { page ->
                when (page) {
                    0 -> TimerPage(viewModel) {
                        handleButtonClick(viewModel, onStartDetection, onStopDetection)
                    }
                    1 -> SettingsPage(viewModel)
                }
            }
        }
    }

    private fun handleButtonClick(viewModel: ShotTimerViewModel, onStart: () -> Unit, onStop: () -> Unit) {
        when (viewModel.timerState) {
            TimerState.IDLE, TimerState.STOPPED -> {
                viewModel.timerState = TimerState.DELAYING
                viewModel.elapsedTime = 0L
                Handler(Looper.getMainLooper()).postDelayed({
                    if (viewModel.timerState == TimerState.DELAYING) {
                        viewModel.timerState = TimerState.RUNNING
                        
                        // 計時器啟動：發出強烈長震動 (代替 BEEP)
                        performVibration(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                        
                        onStart()
                    }
                }, (1000..3000).random().toLong())
            }
            TimerState.RUNNING -> {
                viewModel.timerState = TimerState.STOPPED
                onStop()
            }
            TimerState.DELAYING -> viewModel.timerState = TimerState.IDLE
        }
    }
}

class AccelerometerRecognizer(
    private val context: Context,
    private val threshold: Float,
    private val onShot: () -> Unit
) : SensorEventListener {
    private var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var lastValue = 0f
    fun startRecognition() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST)
    }
    fun stopRecognition() = sensorManager.unregisterListener(this)
    override fun onSensorChanged(event: SensorEvent?) {
        val current = sqrt(event!!.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
        Log.v("AccelRecognizer", "Current Accel: $current")
        if (lastValue != 0f) {
            val delta = abs(current - lastValue)
            if (delta > threshold) {
                Log.d("AccelRecognizer", "Shot detected! Delta: $delta")
                Handler(Looper.getMainLooper()).post { onShot() }
            }
        }
        lastValue = current
    }
    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
}

class AudioRecognizer(
    private val context: Context,
    private val threshold: Double,
    private val onShot: () -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    fun startRecognition() {
        val size = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, size)
        audioRecord?.startRecording()
        isRecording = true
        Thread {
            val buffer = ShortArray(size)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, size) ?: 0
                if (read > 0) {
                    val rms = sqrt(buffer.take(read).map { (it * it).toDouble() }.average())
                    val db = 20 * log10(rms / 32767.0)
                    Log.v("AudioRecognizer", "Current dB: $db")
                    if (db > threshold) {
                        Log.d("AudioRecognizer", "Shot detected! dB: $db")
                        Handler(Looper.getMainLooper()).post { onShot() }
                    }
                }
            }
        }.start()
    }
    fun stopRecognition() {
        isRecording = false
        audioRecord?.apply { if (state == AudioRecord.RECORDSTATE_RECORDING) stop(); release() }
    }
}
