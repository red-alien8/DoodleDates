package com.spycodedoodledates.ui.alarm

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spycodedoodledates.data.repository.DiaryRepository
import com.spycodedoodledates.notifications.NoteAlarmReceiver
import com.spycodedoodledates.ui.theme.DoodleDatesTheme
import com.spycodedoodledates.ui.theme.settings.ThemeSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    @Inject
    lateinit var repository: DiaryRepository

    @Inject
    lateinit var themeSettings: ThemeSettings

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val monthKey = intent.getStringExtra("monthKey") ?: ""
        val noteId = intent.getLongExtra("noteId", -1)

        startAlarm()

        setContent {
            DoodleDatesTheme {
                ImmersiveAlarmScreen(
                    monthKey = monthKey,
                    noteId = noteId,
                    repository = repository,
                    onDismiss = { stopAndFinish() },
                    onSnooze = {
                        val snoozeIntent = Intent(this, NoteAlarmReceiver::class.java).apply {
                            action = "ACTION_SNOOZE"
                            putExtra("noteId", noteId)
                            putExtra("monthKey", monthKey)
                        }
                        sendBroadcast(snoozeIntent)
                        stopAndFinish()
                    }
                )
            }
        }
    }

    private fun startAlarm() {
        lifecycleScope.launch {
            val settings = themeSettings.settingsFlow.first()
            
            // Vibration
            if (settings.vibrationEnabled) {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                val pattern = longArrayOf(0, 500, 500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            }

            // Sound
            val soundUri = if (!settings.ringtoneUri.isNullOrEmpty()) {
                Uri.parse(settings.ringtoneUri)
            } else {
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            }

            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@AlarmActivity, soundUri!!)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopAndFinish() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        vibrator?.cancel()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImmersiveAlarmScreen(
    monthKey: String,
    noteId: Long,
    repository: DiaryRepository,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var note by remember { mutableStateOf<com.spycodedoodledates.data.local.entities.NoteEntity?>(null) }
    val infiniteTransition = rememberInfiniteTransition()
    
    val bgAnim by infiniteTransition.animateColor(
        initialValue = Color(0xFF1A237E),
        targetValue = Color(0xFF311B92),
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse)
    )

    LaunchedEffect(noteId) {
        val notes = repository.getNotesForMonth(monthKey).firstOrNull()
        note = notes?.find { it.id == noteId }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgAnim, Color.Black)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Alarm,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "ALARM",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Beautiful Note Display
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                shape = RoundedCornerShape(32.dp),
                color = note?.paperColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.White,
                tonalElevation = 12.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = note?.contentHtml?.replace(Regex("<[^>]*>"), "") ?: "Loading reminder...",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = note?.textColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Black
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Snooze Button
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier.height(56.dp).fillMaxWidth(0.6f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Snooze, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Snooze 10m")
            }
        }

        // Swipe to Dismiss at the bottom
        SwipeToDismissBar(
            onDismiss = onDismiss,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismissBar(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val width = 280.dp
    val sizePx = with(LocalDensity.current) { width.toPx() }
    val swipeableState = rememberSwipeableState(0)
    val anchors = mapOf(0f to 0, (sizePx - 180f) to 1)

    if (swipeableState.currentValue == 1) {
        LaunchedEffect(Unit) { onDismiss() }
    }

    Box(
        modifier = modifier
            .width(width)
            .height(80.dp)
            .background(Color.White.copy(alpha = 0.15f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .padding(8.dp)
    ) {
        Text(
            "Slide to Stop",
            modifier = Modifier.align(Alignment.Center),
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.titleMedium
        )
        
        Box(
            modifier = Modifier
                .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.5f) },
                    orientation = Orientation.Horizontal
                )
                .size(64.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color.Red)
        }
    }
}
