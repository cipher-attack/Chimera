package com.chimera.zpqmxr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MinimalistFlagIcon(countryCode: String, size: Dp = 24.dp) {
    val fallbackColor = MaterialTheme.colorScheme.surfaceVariant
    val onFallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Canvas(modifier = Modifier.size(size).clip(CircleShape)) {
        val w = this.size.width
        val h = this.size.height
        
        fun drawV(c1: Color, c2: Color, c3: Color) {
            drawRect(c1, size = Size(w / 3, h))
            drawRect(c2, topLeft = Offset(w / 3, 0f), size = Size(w / 3, h))
            drawRect(c3, topLeft = Offset(2 * w / 3, 0f), size = Size(w / 3, h))
        }
        fun drawH(c1: Color, c2: Color, c3: Color) {
            drawRect(c1, size = Size(w, h / 3))
            drawRect(c2, topLeft = Offset(0f, h / 3), size = Size(w, h / 3))
            drawRect(c3, topLeft = Offset(0f, 2 * h / 3), size = Size(w, h / 3))
        }

        when (countryCode) {
            "US" -> {
                for (i in 0 until 9) {
                    val color = if (i % 2 == 0) Color(0xFFB22234) else Color.White
                    drawRect(color, topLeft = Offset(0f, i * h / 9), size = Size(w, h / 9))
                }
                drawRect(Color(0xFF3C3B6E), topLeft = Offset(0f, 0f), size = Size(w * 0.45f, h * 0.5f))
            }
            "GB" -> {
                drawRect(Color(0xFF012169), size = Size(w, h))
                drawRect(Color.White, topLeft = Offset(w * 0.4f, 0f), size = Size(w * 0.2f, h))
                drawRect(Color.White, topLeft = Offset(0f, h * 0.4f), size = Size(w, h * 0.2f))
                drawRect(Color(0xFFC8102E), topLeft = Offset(w * 0.45f, 0f), size = Size(w * 0.1f, h))
                drawRect(Color(0xFFC8102E), topLeft = Offset(0f, h * 0.45f), size = Size(w, h * 0.1f))
            }
            "FR" -> drawV(Color(0xFF0055A4), Color.White, Color(0xFFEF4135))
            "IT" -> drawV(Color(0xFF009246), Color.White, Color(0xFFCE2B37))
            "DE" -> drawH(Color.Black, Color(0xFFDD0000), Color(0xFFFFCE00))
            "RU" -> drawH(Color.White, Color(0xFF0039A6), Color(0xFFD52B1E))
            "ES" -> {
                drawRect(Color(0xFFAA151B), size = Size(w, h / 4))
                drawRect(Color(0xFFF1BF00), topLeft = Offset(0f, h / 4), size = Size(w, h / 2))
                drawRect(Color(0xFFAA151B), topLeft = Offset(0f, h * 0.75f), size = Size(w, h / 4))
            }
            "BR" -> {
                drawRect(Color(0xFF009C3B), size = Size(w, h))
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w/2, h*0.1f)
                    lineTo(w*0.9f, h/2)
                    lineTo(w/2, h*0.9f)
                    lineTo(w*0.1f, h/2)
                    close()
                }
                drawPath(path, Color(0xFFF8C100))
                drawCircle(Color(0xFF002776), radius = w*0.2f, center = Offset(w/2, h/2))
            }
            "FI", "DK", "SV", "NO" -> {
                val bg = if (countryCode == "FI") Color.White else if (countryCode == "SV") Color(0xFF006AA7) else if (countryCode == "NO") Color(0xFFBA0C2F) else Color(0xFFC60C30)
                val cross = if (countryCode == "FI") Color(0xFF002F6C) else if (countryCode == "SV") Color(0xFFFECC00) else Color.White
                drawRect(bg, size = Size(w, h))
                drawRect(cross, topLeft = Offset(w * 0.3f, 0f), size = Size(w * 0.15f, h))
                drawRect(cross, topLeft = Offset(0f, h * 0.4f), size = Size(w, h * 0.15f))
                if (countryCode == "NO") {
                    drawRect(Color(0xFF00205B), topLeft = Offset(w * 0.35f, 0f), size = Size(w * 0.05f, h))
                    drawRect(Color(0xFF00205B), topLeft = Offset(0f, h * 0.45f), size = Size(w, h * 0.05f))
                }
            }
            "PT" -> {
                drawRect(Color(0xFF006600), size = Size(w*0.4f, h))
                drawRect(Color(0xFFFF0000), topLeft = Offset(w*0.4f, 0f), size = Size(w*0.6f, h))
                drawCircle(Color(0xFFFFCC00), radius = w*0.15f, center = Offset(w*0.4f, h/2))
            }
            "TR" -> {
                drawRect(Color(0xFFE30A17), size = Size(w, h))
                drawCircle(Color.White, radius = w*0.25f, center = Offset(w*0.4f, h/2))
                drawCircle(Color(0xFFE30A17), radius = w*0.2f, center = Offset(w*0.45f, h/2))
            }
            "BE" -> drawV(Color.Black, Color(0xFFFDDA24), Color(0xFFEF3340))
            "HR", "SI", "SK" -> drawH(Color.White, Color(0xFF0039A6), Color(0xFFD52B1E)) 
            "HU" -> drawH(Color(0xFFCD2A3E), Color.White, Color(0xFF436F4D))
            "CA" -> drawV(Color(0xFFFF0000), Color.White, Color(0xFFFF0000))
            else -> {
                drawRect(fallbackColor)
            }
        }
    }
}
