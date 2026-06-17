package com.chimera.zpqmxr.ui.components
import com.chimera.zpqmxr.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush

@Composable
fun BentoHeader(
    payloadCount: Int, 
    kernelVersion: String, 
    osName: String, 
    btRegistered: Boolean,
    btConnected: Boolean,
    isRootGranted: Boolean,
    rootVersionString: String,
    isRndisEnabled: Boolean,
    isIsoMounted: Boolean,
    selectedIsoPath: String?,
    targetMac: String,
    onTargetMacChange: (String) -> Unit,
    onVerifyHid: () -> Unit,
    onInitBt: () -> Unit,
    onSetupGadget: () -> Unit,
    onDisableGadget: () -> Unit,
    onStartRndisWeb: () -> Unit,
    onStopRndisWeb: () -> Unit,
    onIsoPathChange: (String) -> Unit,
    onMountIso: () -> Unit,
    onUnmountIso: () -> Unit,
    onAutoPair: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { onVerifyHid() }
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (isRootGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                                    Color.Transparent
                                ),
                                radius = 600f
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = if (isRootGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isRootGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    androidx.compose.foundation.shape.CircleShape
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRootGranted) stringResource(R.string.status_privileged) else stringResource(R.string.status_restricted),
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.2.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = if (isRootGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Icon(
                            imageVector = if (isRootGranted) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = "Status",
                            tint = if (isRootGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = if (isRootGranted) stringResource(R.string.status_system_engaged) else stringResource(R.string.status_system_locked),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRootGranted) stringResource(R.string.desc_hardware_loaded, payloadCount) else stringResource(R.string.desc_root_required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        if (isRootGranted) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onSetupGadget,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.btn_mount_gadget), style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                }
                Button(
                    onClick = onDisableGadget,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_unmount), style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                }
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onInitBt() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier
                    .padding(28.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Bluetooth, contentDescription = "BT", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(stringResource(R.string.label_bt_hid_dev), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (btConnected) stringResource(R.string.status_active) else if (btRegistered) stringResource(R.string.status_standby) else stringResource(R.string.status_down), 
                        style = MaterialTheme.typography.titleMedium, 
                        color = if (btConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.label_click_to_init), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Terminal, contentDescription = "Root System", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(if (rootVersionString.contains("Magisk", true) || rootVersionString.contains("KernelSU", true) || rootVersionString.contains("APatch", true)) stringResource(R.string.label_root_mgr) else stringResource(R.string.label_su_bin), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        rootVersionString.take(12).ifEmpty { stringResource(R.string.label_na) }, 
                        style = MaterialTheme.typography.titleMedium, 
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.label_version_info), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Security, contentDescription = "Security", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(stringResource(R.string.title_auto_pair), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.desc_auto_pair), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = targetMac,
                        onValueChange = onTargetMacChange,
                        placeholder = { Text(stringResource(R.string.hint_mac_address), fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.2f)
                        )
                    )
                    Button(
                        onClick = { onAutoPair(targetMac) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(stringResource(R.string.btn_force_pair), fontFamily = FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Usb, contentDescription = "ISO Mounter", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(stringResource(R.string.title_iso_mount), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.desc_iso_mount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                if (!isRootGranted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(androidx.compose.material.icons.Icons.Default.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.err_root_required), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = selectedIsoPath ?: "",
                    onValueChange = onIsoPathChange,
                    placeholder = { Text(stringResource(R.string.hint_iso_path), fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.2f)
                    ),
                    enabled = isRootGranted
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onMountIso,
                        enabled = isRootGranted && !isIsoMounted && !selectedIsoPath.isNullOrBlank(),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.btn_mount_iso), fontFamily = FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                    Button(
                        onClick = onUnmountIso,
                        enabled = isRootGranted && isIsoMounted,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.btn_unmount), fontFamily = FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Router, contentDescription = "Web Remote", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(stringResource(R.string.title_rndis_web), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.desc_rndis_web), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                if (!isRootGranted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(androidx.compose.material.icons.Icons.Default.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.err_root_required), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onStartRndisWeb,
                        enabled = isRootGranted && !isRndisEnabled,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.btn_start_rndis), fontFamily = FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                    Button(
                        onClick = onStopRndisWeb,
                        enabled = isRootGranted && isRndisEnabled,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.btn_stop), fontFamily = FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
        }
    }
}
