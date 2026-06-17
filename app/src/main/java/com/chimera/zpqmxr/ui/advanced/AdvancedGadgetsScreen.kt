package com.chimera.zpqmxr.ui.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.chimera.zpqmxr.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedGadgetsScreen(
    onBack: () -> Unit,
    viewModel: AdvancedGadgetsViewModel = viewModel()
) {
    val rndisEnabled by viewModel.rndisEnabled.collectAsStateWithLifecycle()
    val acmEnabled by viewModel.acmEnabled.collectAsStateWithLifecycle()
    val midiEnabled by viewModel.midiEnabled.collectAsStateWithLifecycle()
    val massStorageEnabled by viewModel.massStorageEnabled.collectAsStateWithLifecycle()
    val hidExfilEnabled by viewModel.hidExfilEnabled.collectAsStateWithLifecycle()
    val lootFiles by viewModel.lootFiles.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    val mainViewModel: com.chimera.zpqmxr.ui.MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val hardwareScanResult by mainViewModel.hardwareScanResult.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.title_advanced_configfs), 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.desc_clear_logs), tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GadgetToggleCard(
                    title = stringResource(R.string.title_gadget_rndis),
                    description = stringResource(R.string.desc_gadget_rndis),
                    enabled = rndisEnabled,
                    onToggle = { viewModel.toggleRndis() }
                )
                
                GadgetToggleCard(
                    title = stringResource(R.string.title_gadget_acm),
                    description = stringResource(R.string.desc_gadget_acm),
                    enabled = acmEnabled,
                    onToggle = { viewModel.toggleAcm() }
                )
                
                GadgetToggleCard(
                    title = stringResource(R.string.title_gadget_midi),
                    description = stringResource(R.string.desc_gadget_midi),
                    enabled = midiEnabled,
                    onToggle = { viewModel.toggleMidi() }
                )
                
                GadgetToggleCard(
                    title = stringResource(R.string.title_gadget_mass_storage),
                    description = stringResource(R.string.desc_gadget_mass_storage),
                    enabled = massStorageEnabled,
                    onToggle = { viewModel.toggleMassStorage() }
                )

            if (massStorageEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.title_loot_files), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            IconButton(onClick = { viewModel.refreshLoot() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", Modifier.size(16.dp))
                            }
                        }
                        if (lootFiles.isEmpty()) {
                            Text(stringResource(R.string.msg_no_loot_files), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha=0.7f))
                        } else {
                            lootFiles.forEach { file ->
                                Text("- \$file", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            }

            GadgetToggleCard(
                title = stringResource(R.string.title_hid_covert),
                description = stringResource(R.string.desc_hid_covert),
                enabled = hidExfilEnabled,
                onToggle = { viewModel.toggleHidExfil() }
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.title_deep_hardware_scan), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        androidx.compose.material3.Button(
                            onClick = { mainViewModel.performDeepHardwareScan() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.btn_scan_now))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (hardwareScanResult == null) {
                        Text(stringResource(R.string.msg_no_scan_data), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f))
                    } else {
                        Text(
                            hardwareScanResult!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 10,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
            }
        }

        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                    .padding(16.dp)
                    .fillMaxWidth(0.95f)
                    .height(300.dp)
                    .background(Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DragHandle, contentDescription = "Drag", tint = Color.Gray)
                        Text(
                            stringResource(R.string.title_telemetry_logger),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Row {
                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            val context = androidx.compose.ui.platform.LocalContext.current
                            IconButton(onClick = { viewModel.clearLogs() }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(logs.joinToString("\n")))
                                android.widget.Toast.makeText(context, context.getString(R.string.msg_logs_copied), android.widget.Toast.LENGTH_SHORT).show()
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Share, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                
                Spacer(modifier = Modifier.height(8.dp))

                if (logs.isEmpty()) {
                    Text(
                        stringResource(R.string.msg_no_events_logged),
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { log ->
                            Text(
                                log,
                                color = Color.Green,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun GadgetToggleCard(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
