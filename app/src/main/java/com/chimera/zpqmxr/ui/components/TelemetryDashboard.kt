package com.chimera.zpqmxr.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.chimera.zpqmxr.R
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.sin
import kotlin.random.Random

data class TelemetryState(
    val isHidg0Active: Boolean = false,
    val payloadInjectionRateHistory: List<Float> = List(40) { 0f },
    val currentRate: Float = 0f
)

@Composable
fun TelemetryDashboard(
    isPayloadRunning: Boolean,
    modifier: Modifier = Modifier
) {
    var telemetryState by remember { mutableStateOf(TelemetryState()) }

    
    LaunchedEffect(isPayloadRunning) {
        while (true) {
            
            val hidg0Exists = File("/dev/hidg0").exists() || File("/config/usb_gadget/g1").exists()
            
            
            val newRate = if (isPayloadRunning) {
                Random.nextFloat() * 150f + 50f 
            } else {
                
                (telemetryState.currentRate * 0.5f).coerceAtLeast(0f).let { if (it < 1f) 0f else it }
            }

            val newHistory = telemetryState.payloadInjectionRateHistory.toMutableList()
            newHistory.removeAt(0)
            newHistory.add(newRate)

            telemetryState = telemetryState.copy(
                isHidg0Active = hidg0Exists,
                currentRate = newRate,
                payloadInjectionRateHistory = newHistory
            )
            delay(100) 
        }
    }

    
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.label_payload_status),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (telemetryState.isHidg0Active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                )
                Text(
                    text = stringResource(R.string.label_hidg0),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        
        Text(
            text = stringResource(R.string.label_rate_ks, telemetryState.currentRate.toInt()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        InjectionRateGraph(
            history = telemetryState.payloadInjectionRateHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        )
    }
}

@Composable
fun InjectionRateGraph(
    history: List<Float>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val boundsColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    
    
    val infiniteTransition = rememberInfiniteTransition()
    val scanlineOffsets by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier.clip(RoundedCornerShape(8.dp))) {
        val width = size.width
        val height = size.height
        
        
        val gridStep = width / 10
        for (i in 0..10) {
            drawLine(
                color = boundsColor,
                start = Offset(x = i * gridStep, y = 0f),
                end = Offset(x = i * gridStep, y = height),
                strokeWidth = 1f
            )
        }
        val gridStepY = height / 4
        for (i in 0..4) {
            drawLine(
                color = boundsColor,
                start = Offset(x = 0f, y = i * gridStepY),
                end = Offset(x = width, y = i * gridStepY),
                strokeWidth = 1f
            )
        }

        
        if (history.isNotEmpty()) {
            val maxRate = (history.maxOrNull() ?: 1f).coerceAtLeast(200f) 
            val stepX = width / (history.size - 1).coerceAtLeast(1)
            
            val path = Path().apply {
                moveTo(0f, height)
                history.forEachIndexed { index, rate ->
                    val x = index * stepX
                    val y = height - ((rate / maxRate) * height)
                    if (index == 0) {
                        lineTo(x, y)
                    } else {
                        val prevX = (index - 1) * stepX
                        val prevY = height - ((history[index - 1] / maxRate) * height)
                        
                        val controlPointX = (prevX + x) / 2
                        cubicTo(controlPointX, prevY, controlPointX, y, x, y)
                    }
                }
                lineTo(width, height)
                close()
            }

            
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.5f),
                        primaryColor.copy(alpha = 0.0f)
                    )
                )
            )

            
            val strokePath = Path().apply {
                history.forEachIndexed { index, rate ->
                    val x = index * stepX
                    val y = height - ((rate / maxRate) * height)
                    if (index == 0) {
                        moveTo(x, y)
                    } else {
                        val prevX = (index - 1) * stepX
                        val prevY = height - ((history[index - 1] / maxRate) * height)
                        val controlPointX = (prevX + x) / 2
                        cubicTo(controlPointX, prevY, controlPointX, y, x, y)
                    }
                }
            }

            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            
            
            if (history.isNotEmpty()) {
                val lastRate = history.last()
                val lastY = height - ((lastRate / maxRate) * height)
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx() + (scanlineOffsets * 2.dp.toPx()),
                    center = Offset(width, lastY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(width, lastY)
                )
            }
        }

        
        val scanY = scanlineOffsets * height
        drawLine(
            color = primaryColor.copy(alpha = 0.4f),
            start = Offset(0f, scanY),
            end = Offset(width, scanY),
            strokeWidth = 2.dp.toPx()
        )
    }
}
