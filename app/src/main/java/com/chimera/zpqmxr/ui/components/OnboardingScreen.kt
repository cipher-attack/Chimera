package com.chimera.zpqmxr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "Hardware Emulation",
            description = "Turns this device into a Swiss Army knife for physical proximity engagements. Strict root required.",
            icon = Icons.Default.Info
        ),
        OnboardingPage(
            title = "Root Access",
            description = "Chimera requires raw access to /config/usb_gadget and char devices. Ensure Magisk/KernelSU is active.",
            icon = Icons.Default.Warning
        ),
        OnboardingPage(
            title = "Engage",
            description = "Get permission before plugging this into a target terminal. Watch your telemetry.",
            icon = Icons.Default.CheckCircle
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage > 0) {
                    TextButton(onClick = { currentPage-- }) {
                        Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pages.forEachIndexed { index, _ ->
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (index == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {}
                    }
                }

                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            onComplete()
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (currentPage == pages.size - 1) "Start" else "Next")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = pages[currentPage].icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = pages[currentPage].title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = pages[currentPage].description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)
