package com.chimera.zpqmxr.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.chimera.zpqmxr.R
import com.chimera.zpqmxr.data.Payload
import com.chimera.zpqmxr.ui.components.AddPayloadDialog
import com.chimera.zpqmxr.ui.components.BentoHeader
import com.chimera.zpqmxr.ui.components.ExecutionDialog
import com.chimera.zpqmxr.ui.components.GithubSearchDialog
import com.chimera.zpqmxr.ui.components.InteractiveRootShellDialog
import com.chimera.zpqmxr.ui.components.PayloadCard
import com.chimera.zpqmxr.ui.components.TelemetryDashboard
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import com.chimera.zpqmxr.ui.components.InteractiveRootShellDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val payloads by viewModel.uiState.collectAsStateWithLifecycle()
    val isExecuting by viewModel.isExecuting.collectAsStateWithLifecycle()
    val executionOutput by viewModel.currentExecutionOutput.collectAsStateWithLifecycle()
    
    val btRegistered by viewModel.btManager.isRegistered.collectAsStateWithLifecycle()
    val btConnected by viewModel.btManager.isConnected.collectAsStateWithLifecycle()
    val isRootGranted by viewModel.isRootGranted.collectAsStateWithLifecycle()
    val rootVersionString by viewModel.rootVersionString.collectAsStateWithLifecycle()
    val isRndisEnabled by viewModel.isRndisEnabled.collectAsStateWithLifecycle()
    val isIsoMounted by viewModel.isIsoMounted.collectAsStateWithLifecycle()
    val selectedIsoPath by viewModel.selectedIsoPath.collectAsStateWithLifecycle()
    val autoRunPayloadId by viewModel.autoRunPayloadId.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var payloadToEdit by remember { mutableStateOf<Payload?>(null) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showRootShellDialog by remember { mutableStateOf(false) }
    var showAdvancedScreen by remember { mutableStateOf(false) }
    var showLangSheet by remember { mutableStateOf(false) }

    var targetMac by remember { mutableStateOf("") }
    var showGithubSearch by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    val text = viewModel.readTextFromUri(it)
                    if (text != null) {
                        payloadToEdit = Payload(name = "Imported Script", script = text)
                        showAddDialog = true
                    }
                }
            }
        }
    )

    if (showAdvancedScreen) {
        com.chimera.zpqmxr.ui.advanced.AdvancedGadgetsScreen(onBack = { showAdvancedScreen = false })
        return
    }

    if (showSettingsScreen) {
        val customHidPath by viewModel.customHidPath.collectAsStateWithLifecycle()
        val customGadgetPath by viewModel.customGadgetPath.collectAsStateWithLifecycle()
        val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()

        com.chimera.zpqmxr.ui.settings.SettingsScreen(
            customHidPath = customHidPath,
            onCustomHidPathChange = { viewModel.updateCustomHidPath(it) },
            customGadgetPath = customGadgetPath,
            onCustomGadgetPathChange = { viewModel.updateCustomGadgetPath(it) },
            isBiometricEnabled = isBiometricEnabled,
            onBiometricEnabledChange = { viewModel.updateBiometricEnabled(it) },
            onPreloadArsenal = { viewModel.preloadChimeraArsenal() },
            onBack = { showSettingsScreen = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val waveColor = MaterialTheme.colorScheme.onSurface.toArgb()
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { context ->
                                com.chimera.zpqmxr.ui.components.AnimatedWaveIconView(context).apply {
                                    setWaveColor(waveColor)
                                }
                            },
                            update = { view ->
                                view.setWaveColor(waveColor)
                            },
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                actions = {
                    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
                    TextButton(onClick = { showLangSheet = true }) {
                        com.chimera.zpqmxr.ui.components.MinimalistFlagIcon(countryCode = selectedLanguage, size = 18.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(selectedLanguage, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.desc_select_lang), tint = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha=0.3f), RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showRootShellDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(androidx.compose.material.icons.Icons.Default.Terminal, contentDescription = stringResource(R.string.desc_root_terminal), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha=0.3f), RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showAdvancedScreen = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.label_adv), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha=0.3f), RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showSettingsScreen = true },
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.desc_settings), tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = fabExpanded,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { 50 }),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { 50 })
                ) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FloatingActionButton(
                            onClick = { showGithubSearch = true },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.desc_search_github), modifier = Modifier.size(24.dp))
                        }
                        FloatingActionButton(
                            onClick = { filePickerLauncher.launch(arrayOf("text/*", "application/octet-stream")) },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.desc_import_payload), modifier = Modifier.size(24.dp))
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { 
                        if (fabExpanded) {
                            showAddDialog = true 
                            fabExpanded = false
                        } else {
                            fabExpanded = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (fabExpanded) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.desc_create_payload))
                    } else {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.desc_options))
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isExecuting,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Column {
                            TelemetryDashboard(isPayloadRunning = isExecuting)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    BentoHeader(
                        payloadCount = payloads.size,
                        kernelVersion = viewModel.kernelVersion,
                        osName = viewModel.osName,
                        btRegistered = btRegistered,
                        btConnected = btConnected,
                        isRootGranted = isRootGranted,
                        rootVersionString = rootVersionString,
                        isRndisEnabled = isRndisEnabled,
                        isIsoMounted = isIsoMounted,
                        selectedIsoPath = selectedIsoPath,
                        targetMac = targetMac,
                        onTargetMacChange = { targetMac = it },
                        onVerifyHid = { viewModel.verifyHid() },
                        onInitBt = { viewModel.btManager.init() },
                        onSetupGadget = { viewModel.setupKernelGadget() },
                        onDisableGadget = { viewModel.disableKernelGadget() },
                        onStartRndisWeb = { viewModel.startRndisWebRemote() },
                        onStopRndisWeb = { viewModel.stopRndisWebRemote() },
                        onIsoPathChange = { viewModel.selectIsoFile(it) },
                        onMountIso = { viewModel.mountIsoFile() },
                        onUnmountIso = { viewModel.unmountIsoFile() },
                        onAutoPair = { viewModel.autoPairBluetooth(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (payloads.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                stringResource(R.string.msg_no_scripts),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(payloads) { payload ->
                        PayloadCard(
                            payload = payload,
                            isAutoRun = payload.id == autoRunPayloadId,
                            onToggleAutoRun = {
                                if (payload.id == autoRunPayloadId) {
                                    viewModel.setAutoRunPayload(null)
                                } else {
                                    viewModel.setAutoRunPayload(payload.id)
                                }
                            },
                            onRunRoot = { viewModel.executePayload(payload.script) },
                            onRunBt = { viewModel.executePayloadBluetooth(payload.script) },
                            onEdit = { 
                                payloadToEdit = payload
                                showAddDialog = true 
                            },
                            onDelete = { viewModel.deletePayload(payload.id) }
                        )
                    }
                }
            }

            executionOutput?.let { output ->
                ExecutionDialog(
                    output = output,
                    isExecuting = isExecuting,
                    onDismiss = { viewModel.dismissExecutionOutput() }
                )
            }
        }
    }

    if (showAddDialog) {
        AddPayloadDialog(
            initialPayload = payloadToEdit,
            onDismiss = { 
                showAddDialog = false
                payloadToEdit = null 
            },
            onSave = { name, script ->
                if (payloadToEdit != null) {
                    viewModel.updatePayload(payloadToEdit!!.id, name, script)
                } else {
                    viewModel.savePayload(name, script)
                }
                showAddDialog = false
                payloadToEdit = null
            }
        )
    }

    if (showRootShellDialog) {
        InteractiveRootShellDialog(
            onDismiss = { showRootShellDialog = false }
        )
    }

    if (showGithubSearch) {
        val context = androidx.compose.ui.platform.LocalContext.current
        GithubSearchDialog(
            onDismiss = { showGithubSearch = false },
            onImportStart = { android.widget.Toast.makeText(context, context.getString(R.string.msg_downloading), android.widget.Toast.LENGTH_SHORT).show() },
            onImportSuccess = { name, content -> 
                viewModel.savePayload(name, content)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    android.widget.Toast.makeText(context, context.getString(R.string.msg_imported_success, name), android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onImportError = { error ->
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    android.widget.Toast.makeText(context, context.getString(R.string.msg_error, error), android.widget.Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    if (showLangSheet) {
        val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
        androidx.compose.material3.ExperimentalMaterial3Api::class
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showLangSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    stringResource(R.string.title_keyboard_region),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    gridItems(viewModel.languages) { lang ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.selectLanguage(lang)
                                    showLangSheet = false
                                }
                                .background(if (lang == selectedLanguage) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
                                .padding(12.dp)
                        ) {
                            com.chimera.zpqmxr.ui.components.MinimalistFlagIcon(countryCode = lang, size = 32.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                lang,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (lang == selectedLanguage) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                color = if (lang == selectedLanguage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
