package com.chimera.zpqmxr.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.chimera.zpqmxr.R
import com.chimera.zpqmxr.ui.theme.AppThemeMode
import com.chimera.zpqmxr.ui.theme.HapticEvent
import com.chimera.zpqmxr.ui.theme.HapticProfile
import com.chimera.zpqmxr.ui.theme.LocalHapticEngine
import com.chimera.zpqmxr.ui.theme.LocalThemeController

import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    customHidPath: String,
    onCustomHidPathChange: (String) -> Unit,
    customGadgetPath: String,
    onCustomGadgetPathChange: (String) -> Unit,
    isBiometricEnabled: Boolean,
    onBiometricEnabledChange: (Boolean) -> Unit,
    onPreloadArsenal: () -> Unit,
    onBack: () -> Unit
) {
    val themeController = LocalThemeController.current
    val hapticEngine = LocalHapticEngine.current
    val currentTheme by themeController.currentTheme.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("chimera_prefs", android.content.Context.MODE_PRIVATE)

    var stealthModeEnabled by remember { mutableStateOf(false) }
    var rootAccessMethod by remember { mutableStateOf("su") }
    var autoPairBt by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            Text(stringResource(R.string.header_appearance), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    
                    Text(stringResource(R.string.title_visual_mode), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentTheme == AppThemeMode.CYBERPUNK_DARK,
                            onClick = { themeController.switchTheme(AppThemeMode.CYBERPUNK_DARK); hapticEngine?.execute(HapticEvent.CLICK) },
                            label = { Text(stringResource(R.string.theme_dark)) }
                        )
                        FilterChip(
                            selected = currentTheme == AppThemeMode.LIGHT_MODE,
                            onClick = { themeController.switchTheme(AppThemeMode.LIGHT_MODE); hapticEngine?.execute(HapticEvent.CLICK) },
                            label = { Text(stringResource(R.string.theme_light)) }
                        )
                        FilterChip(
                            selected = currentTheme == AppThemeMode.FIELD_OPS,
                            onClick = { themeController.switchTheme(AppThemeMode.FIELD_OPS); hapticEngine?.execute(HapticEvent.CLICK) },
                            label = { Text(stringResource(R.string.theme_night)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.error
                            )
                        )
                        FilterChip(
                            selected = currentTheme == AppThemeMode.TERMINAL_MONOCHROME,
                            onClick = { themeController.switchTheme(AppThemeMode.TERMINAL_MONOCHROME); hapticEngine?.execute(HapticEvent.CLICK) },
                            label = { Text(stringResource(R.string.theme_terminal)) }
                        )
                        FilterChip(
                            selected = currentTheme == AppThemeMode.SLATE_MONOCHROME,
                            onClick = { themeController.switchTheme(AppThemeMode.SLATE_MONOCHROME); hapticEngine?.execute(HapticEvent.CLICK) },
                            label = { Text(stringResource(R.string.theme_slate)) }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    
                    var selectedHaptic by remember { mutableStateOf(hapticEngine?.currentProfile ?: HapticProfile.TACTICAL) }
                    Text(stringResource(R.string.title_haptic_feedback), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedHaptic == HapticProfile.STEALTH,
                            onClick = { 
                                selectedHaptic = HapticProfile.STEALTH
                                hapticEngine?.currentProfile = HapticProfile.STEALTH
                            },
                            label = { Text(stringResource(R.string.haptic_disabled)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedHaptic == HapticProfile.TACTICAL,
                            onClick = { 
                                selectedHaptic = HapticProfile.TACTICAL
                                hapticEngine?.currentProfile = HapticProfile.TACTICAL
                                hapticEngine?.execute(HapticEvent.CLICK)
                            },
                            label = { Text(stringResource(R.string.haptic_light)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedHaptic == HapticProfile.ASSAULT,
                            onClick = { 
                                selectedHaptic = HapticProfile.ASSAULT
                                hapticEngine?.currentProfile = HapticProfile.ASSAULT
                                hapticEngine?.execute(HapticEvent.PAYLOAD_INJECT)
                            },
                            label = { Text(stringResource(R.string.haptic_strong)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(stringResource(R.string.header_security), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.title_biometric_lock), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.desc_biometric_lock), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isBiometricEnabled, onCheckedChange = onBiometricEnabledChange)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.header_system_config), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    OutlinedTextField(
                        value = customGadgetPath,
                        onValueChange = onCustomGadgetPathChange,
                        label = { Text(stringResource(R.string.label_configfs_path)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customHidPath,
                        onValueChange = onCustomHidPathChange,
                        label = { Text(stringResource(R.string.label_hid_dev_path)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    var isRootActive by remember { mutableStateOf(prefs.getBoolean("root_active", false)) }
                    var isCheckingRoot by remember { mutableStateOf(false) }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isRootActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isRootActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Native Execution Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isRootActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (isRootActive) "Root Privilege Escalation Successful" else "Requesting Core C++ Access",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isCheckingRoot) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Switch(
                                checked = isRootActive,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        isCheckingRoot = true
                                        
                                        java.util.concurrent.Executors.newSingleThreadExecutor().execute {
                                            val rootGranted = com.chimera.zpqmxr.utils.NativeIOManager.checkRootNative()
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                isCheckingRoot = false
                                                isRootActive = rootGranted
                                                prefs.edit().putBoolean("root_active", rootGranted).apply()
                                            }
                                        }
                                    } else {
                                        isRootActive = false
                                        prefs.edit().putBoolean("root_active", false).apply()
                                    }
                                    Unit
                                },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
                    val isChinese = currentLocales.toLanguageTags().contains("zh")

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.title_app_language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.desc_app_language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            androidx.compose.material3.FilterChip(
                                selected = !isChinese,
                                onClick = {
                                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("en"))
                                },
                                label = { Text("English", fontWeight = FontWeight.Medium) },
                                leadingIcon = { if (!isChinese) Icon(Icons.Default.Check, null) }
                            )
                            androidx.compose.material3.FilterChip(
                                selected = isChinese,
                                onClick = {
                                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("zh"))
                                },
                                label = { Text("中文 (Chinese)", fontWeight = FontWeight.Medium) },
                                leadingIcon = { if (isChinese) Icon(Icons.Default.Check, null) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.header_payload_engine), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.title_stealth_mode), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.desc_stealth_mode), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = stealthModeEnabled, onCheckedChange = { stealthModeEnabled = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.title_auto_pair), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.desc_auto_pair_settings), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = autoPairBt, onCheckedChange = { autoPairBt = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(stringResource(R.string.header_utilities_scripts), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            var payloadsLoaded by remember { mutableStateOf(prefs.getBoolean("payloads_loaded", false)) }
            
            if (payloadsLoaded) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.5f))
                ) {
                    Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Core Payloads Injected", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        onPreloadArsenal()
                        prefs.edit().putBoolean("payloads_loaded", true).apply()
                        payloadsLoaded = true
                        Unit
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(stringResource(R.string.title_preload_payloads), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("Injects essential scripts to DB securely", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
