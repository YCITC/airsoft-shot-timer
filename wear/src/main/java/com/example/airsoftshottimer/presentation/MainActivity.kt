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
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.airsoftshottimer.R
import com.example.airsoftshottimer.presentation.theme.AirsoftShotTimerTheme
import java.io.IOException
import java.util.Random
import kotlin.concurrent.timer
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

enum class TimerState { IDLE, DELAYING, RUNNING, STOPPED }
enum class DetectionMode { MICROPHONE, ACCELEROMETER }

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var audioRecognizer: AudioRecognizer? = null
    private var accelerometerRecognizer: AccelerometerRecognizer? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "RECORD_AUDIO permission granted")
        } else {
            Log.w("MainActivity", "RECORD_AUDIO permission denied")
            // Inform the user that microphone functionality will not work
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request RECORD_AUDIO permission at runtime
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            WearApp(
                onPlayBeep = { playBeep() },
                onStartAudioRecognition = { onShotDetectedCallback ->
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        audioRecognizer = AudioRecognizer(this, onShotDetectedCallback)
                        audioRecognizer?.startRecognition()
                    } else {
                        Log.w("MainActivity", "Cannot start audio recognition: permission not granted")
                    }
                },
                onStopAudioRecognition = { audioRecognizer?.stopRecognition() },
                onStartAccelerometerRecognition = { onShotDetectedCallback ->
                    accelerometerRecognizer = AccelerometerRecognizer(this, onShotDetectedCallback)
                    accelerometerRecognizer?.startRecognition()
                },
                onStopAccelerometerRecognition = { accelerometerRecognizer?.stopRecognition() },
                onReleaseMediaPlayer = { releaseMediaPlayer() }
            )
        }
    }

    private fun playBeep() {
        // To play a real sound, ensure you have a 'beep.mp3' or 'beep.wav' file in res/raw/
        // then uncomment the line below and replace R.raw.beep with your resource name.
        // If you don't have a sound file yet, this will simply log the beep event.
        Log.d("MainActivity", "Playing BEEP sound (placeholder)")
        // Example if you have R.raw.beep:
        // if (mediaPlayer == null) {
        //     try {
        //         mediaPlayer = MediaPlayer.create(applicationContext, R.raw.beep)
        //         mediaPlayer?.apply {
        //             setOnCompletionListener { mp -> mp.release(); mediaPlayer = null }
        //             start()
        //         }
        //     } catch (e: Exception) {
        //         Log.e("MainActivity", "Error playing beep sound: ${e.message}")
        //     }
        // } else {
        //     mediaPlayer?.start()
        // }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        audioRecognizer?.stopRecognition()
        audioRecognizer = null
        accelerometerRecognizer?.stopRecognition()
        accelerometerRecognizer = null
    }
}

@Composable
fun WearApp(
    onPlayBeep: () -> Unit,
    onStartAudioRecognition: ((() -> Unit) -> Unit),
    onStopAudioRecognition: () -> Unit,
    onStartAccelerometerRecognition: ((() -> Unit) -> Unit),
    onStopAccelerometerRecognition: () -> Unit,
    onReleaseMediaPlayer: () -> Unit
) {
    val context = LocalContext.current

    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }
    var detectionMode by remember { mutableStateOf(DetectionMode.ACCELEROMETER) } // Set to ACCELEROMETER for testing Task 5

    androidx.compose.runtime.LaunchedEffect(timerState) {
        if (timerState == TimerState.RUNNING) {
            try {
                while (true) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    kotlinx.coroutines.delay(10)
                }
            } catch (e: Exception) {
                Log.e("WearApp", "Timer loop error: ${e.message}", e)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { 
            try {
                onStopAudioRecognition()
                onStopAccelerometerRecognition()
                onReleaseMediaPlayer()
            } catch (e: Exception) {
                Log.e("WearApp", "Cleanup error: ${e.message}", e)
            }
        }
    }

    AirsoftShotTimerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (timerState) {
                    TimerState.IDLE -> "START"
                    TimerState.DELAYING -> "Delaying..."
                    TimerState.RUNNING -> String.format("%.2f", elapsedTime / 1000.0) + "s"
                    TimerState.STOPPED -> String.format("%.2f", elapsedTime / 1000.0) + "s"
                },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Button(onClick = {
                try {
                    when (timerState) {
                        TimerState.IDLE, TimerState.STOPPED -> {
                            timerState = TimerState.DELAYING
                            elapsedTime = 0L
                            val delayMillis = kotlin.random.Random.nextLong(1000L, 3000L)

                            Handler(Looper.getMainLooper()).postDelayed({
                                try {
                                    if (timerState == TimerState.DELAYING) {
                                        onPlayBeep()
                                        startTime = System.currentTimeMillis()
                                        timerState = TimerState.RUNNING

                                        when (detectionMode) {
                                            DetectionMode.MICROPHONE -> {
                                                onStartAudioRecognition { 
                                                    if (timerState == TimerState.RUNNING) {
                                                        timerState = TimerState.STOPPED
                                                        elapsedTime = System.currentTimeMillis() - startTime
                                                        onStopAudioRecognition()
                                                        Log.d("WearApp", "Shot detected (Mic)!")
                                                    }
                                                }
                                            }
                                            DetectionMode.ACCELEROMETER -> {
                                                onStartAccelerometerRecognition { 
                                                    if (timerState == TimerState.RUNNING) {
                                                        timerState = TimerState.STOPPED
                                                        elapsedTime = System.currentTimeMillis() - startTime
                                                        onStopAccelerometerRecognition()
                                                        Log.d("WearApp", "Shot detected (Accel)!")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("WearApp", "Delayed execution crash: ${e.message}", e)
                                    timerState = TimerState.IDLE
                                }
                            }, delayMillis)
                        }
                        TimerState.RUNNING -> {
                            timerState = TimerState.STOPPED
                            onStopAudioRecognition()
                            onStopAccelerometerRecognition()
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e("WearApp", "Button click crash: ${e.message}", e)
                }
            }) {
                Text(
                    text = when (timerState) {
                        TimerState.IDLE -> "START"
                        TimerState.DELAYING -> "CANCEL"
                        TimerState.RUNNING -> "STOP"
                        TimerState.STOPPED -> "RESTART"
                    }
                )
            }
        }
    }
}

class AccelerometerRecognizer(
    private val context: android.content.Context,
    private val onShotDetected: () -> Unit,
    private val accelerationThreshold: Float = 20.0f // Adjust this threshold based on testing
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var isDetecting = false
    private var lastAccelerationValue = 0f

    companion object {
        private const val TAG = "AccelRecognizer"
    }

    fun startRecognition() {
        if (isDetecting) {
            Log.d(TAG, "Accelerometer recognition already running.")
            return
        }

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            Log.e(TAG, "Accelerometer not available on this device.")
            return
        }

        sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        isDetecting = true
        Log.d(TAG, "Accelerometer recognition started.")
    }

    fun stopRecognition() {
        if (!isDetecting) return

        sensorManager?.unregisterListener(this)
        isDetecting = false
        Log.d(TAG, "Accelerometer recognition stopped.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate the magnitude of the acceleration vector
            val currentAcceleration = sqrt(x * x + y * y + z * z)

            if (lastAccelerationValue != 0f) {
                val deltaAcceleration = abs(currentAcceleration - lastAccelerationValue)
                Log.d(TAG, "Current Accel Delta: %.2f".format(deltaAcceleration))
                if (deltaAcceleration > accelerationThreshold) {
                    Log.d(TAG, "Shot detected! Accel Delta: %.2f".format(deltaAcceleration))
                    Handler(Looper.getMainLooper()).post { onShotDetected() }
                    // To prevent multiple detections from a single movement, stop temporarily
                    // stopRecognition() // Will restart when timerState goes to RUNNING again
                }
            }
            lastAccelerationValue = currentAcceleration
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used for this simple detection
    }
}

class AudioRecognizer(
    private val context: android.content.Context,
    private val onShotDetected: () -> Unit,
    private val noiseThresholdDb: Double = -35.0 // Adjusted threshold
) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val audioBufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        CHANNEL_CONFIG,
        AUDIO_FORMAT
    )
    private val audioBuffer = ShortArray(audioBufferSize)

    companion object {
        private const val SAMPLE_RATE = 44100 // 44.1 kHz
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val TAG = "AudioRecognizer"
    }

    fun startRecognition() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "RECORD_AUDIO permission not granted. Cannot start audio recognition.")
            return
        }


        if (isRecording) {
            Log.d(TAG, "Audio recognition already running.")
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            audioBufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed.")
            return
        }

        audioRecord?.startRecording()
        isRecording = true

        Thread { // Run audio processing in a background thread
            while (isRecording) {
                val shortsRead = audioRecord?.read(audioBuffer, 0, audioBufferSize) ?: 0
                if (shortsRead > 0) {
                    val rms = calculateRms(audioBuffer, shortsRead)
                    val db = 20 * log10(rms / REFERENCE_AMPLITUDE)

                    Log.d(TAG, "Current dB: %.2f".format(db))

                    if (db > noiseThresholdDb) {
                        Log.d(TAG, "Shot detected! dB: %.2f".format(db))
                        Handler(Looper.getMainLooper()).post { onShotDetected() }
                        // To prevent multiple detections from a single shot, stop temporarily
                        // stopRecognition() // Will restart when timerState goes to RUNNING again
                    }
                }
            }
        }.start()
        Log.d(TAG, "Audio recognition started.")
    }

    fun stopRecognition() {
        isRecording = false
        audioRecord?.apply {
            if (state == AudioRecord.RECORDSTATE_RECORDING) {
                stop()
            }
            release()
        }
        audioRecord = null
        Log.d(TAG, "Audio recognition stopped.")
    }

    private fun calculateRms(buffer: ShortArray, shortsRead: Int): Double {
        var sum = 0.0
        for (i in 0 until shortsRead) {
            sum += buffer[i] * buffer[i]
        }
        return if (shortsRead > 0) sqrt(sum / shortsRead) else 0.0
    }

    private val REFERENCE_AMPLITUDE = 32767.0 // Max amplitude for PCM 16-bit
}

@WearPreviewDevices
@Composable
fun DefaultPreview() {
    WearApp(onPlayBeep = {}, onStartAudioRecognition = { _ -> }, onStopAudioRecognition = {}, onStartAccelerometerRecognition = { _ -> }, onStopAccelerometerRecognition = {}, onReleaseMediaPlayer = {})
}
