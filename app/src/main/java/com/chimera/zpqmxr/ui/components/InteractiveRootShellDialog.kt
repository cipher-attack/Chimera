package com.chimera.zpqmxr.ui.components

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Environment
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.chimera.zpqmxr.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.util.Date
import java.util.Locale


enum class ShellRank {
    SYSTEM_CORE, USER_INPUT, STDOUT, STDERR, DIAGNOSTIC, LOOT_ALERT
}

data class TerminalLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rawText: String,
    val rank: ShellRank,
    val timestamp: Long = System.currentTimeMillis()
)

val CHIMERA_THEME_BG = Color(0xFF030508)
val CHIMERA_GLOW_GREEN = Color(0xFF00FF9D)
val CHIMERA_WARN_YELLOW = Color(0xFFFFD400)
val CHIMERA_CRIT_RED = Color(0xFFFF2A2A)
val CHIMERA_USER_CYAN = Color(0xFF00E5FF)
val CHIMERA_SYS_GRAY = Color(0xFF4A5568)


class PersistentRootKernel {
    private var process: Process? = null
    private var outputStream: DataOutputStream? = null
    private var inputReaderState: Job? = null
    private var errorReaderState: Job? = null

    private val _logStream = MutableSharedFlow<TerminalLog>(extraBufferCapacity = 1000)
    val logStream = _logStream.asSharedFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var isAlive by mutableStateOf(false)
        private set

    fun engage() {
        if (isAlive) return
        coroutineScope.launch {
            try {
                emitSystem("Starting root shell...", ShellRank.SYSTEM_CORE)
                process = Runtime.getRuntime().exec("su")
                val p = process ?: return@launch

                outputStream = DataOutputStream(p.outputStream)
                isAlive = true

                emitSystem("Root access granted.", ShellRank.SYSTEM_CORE)

                
                inputReaderState = launch {
                    try {
                        val reader = BufferedReader(InputStreamReader(p.inputStream))
                        var line: String? = null
                        while (isActive && reader.readLine().also { line = it } != null) {
                            line?.let { validateAndEmit(it, ShellRank.STDOUT) }
                        }
                    } catch (e: Exception) {
                        
                    }
                }

                
                errorReaderState = launch {
                    try {
                        val reader = BufferedReader(InputStreamReader(p.errorStream))
                        var line: String? = null
                        while (isActive && reader.readLine().also { line = it } != null) {
                            line?.let { validateAndEmit(it, ShellRank.STDERR) }
                        }
                    } catch (e: Exception) {
                        
                    }
                }

                p.waitFor()
                isAlive = false
                emitSystem("Process exited.", ShellRank.SYSTEM_CORE)
            } catch (e: Exception) {
                isAlive = false
                emitSystem("ERROR: ${e.message}", ShellRank.STDERR)
                emitSystem("Please verify root access.", ShellRank.DIAGNOSTIC)
            }
        }
    }

    fun dispatchCommand(cmd: String) {
        coroutineScope.launch {
            if (!isAlive || outputStream == null) {
                emitSystem("Shell dormant. Waking up...", ShellRank.DIAGNOSTIC)
                engage()
                delay(500)
            }
            try {
                _logStream.emit(TerminalLog(rawText = "$> $cmd", rank = ShellRank.USER_INPUT))
                outputStream?.writeBytes("$cmd\n")
                outputStream?.flush()
            } catch (e: Exception) {
                emitSystem("Dispatch fail: ${e.message}", ShellRank.STDERR)
            }
        }
    }

    fun purge() {
        coroutineScope.launch {
            try {
                outputStream?.writeBytes("exit\n")
                outputStream?.flush()
            } catch (e: Exception) {}
            inputReaderState?.cancel()
            errorReaderState?.cancel()
            
            try {
                outputStream?.close()
            } catch (e: Exception) {}
            
            process?.destroy()
            isAlive = false
            emitSystem("Shell closed.", ShellRank.DIAGNOSTIC)
        }
    }

    private suspend fun emitSystem(msg: String, rank: ShellRank) {
        _logStream.emit(TerminalLog(rawText = msg, rank = rank))
    }

    private suspend fun validateAndEmit(raw: String, rank: ShellRank) {
        var appliedRank = rank
        val lowerText = raw.lowercase()
        if (lowerText.contains("password") || lowerText.contains("token=") || lowerText.contains("ssid") || lowerText.contains("private key")) {
            appliedRank = ShellRank.LOOT_ALERT
        }
        _logStream.emit(TerminalLog(rawText = raw, rank = appliedRank))
    }
}


object DataExfiltrationEngine {
    fun exfiltrateLogsToDownload(logs: List<TerminalLog>, kernel: PersistentRootKernel) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "TerminalExport_${timestamp}.txt"
        val downloadPath = "/sdcard/Download/ChimeraExfil"
        
        val payloadBody = StringBuilder()
        payloadBody.append("===================================================\n")
        payloadBody.append("= Terminal Export Dump\n")
        payloadBody.append("= DATE: $timestamp\n")
        payloadBody.append("===================================================\n\n")

        logs.forEach { log ->
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(log.timestamp))
            val prefix = when(log.rank) {
                ShellRank.USER_INPUT -> "[USER]"
                ShellRank.STDOUT -> "[ OUT]"
                ShellRank.STDERR -> "[ ERR]"
                ShellRank.LOOT_ALERT -> "[!LOOT!]"
                else -> "[ SYS]"
            }
            
            val safeText = log.rawText.replace("'", "'\\''")
            payloadBody.append("$timeStr $prefix $safeText\n")
        }

        val injectionCmd = """
            mkdir -p "$downloadPath"
            cat << 'EOF_CHIMERA_EXFIL' > "$downloadPath/$fileName"
${payloadBody}
EOF_CHIMERA_EXFIL
            chmod 777 "$downloadPath/$fileName"
            echo "SUCCESS: Logs exported to $downloadPath/$fileName"
        """.trimIndent()

        kernel.dispatchCommand(injectionCmd)
    }
}


fun buildTerminalSyntax(text: String, rank: ShellRank): AnnotatedString {
    return buildAnnotatedString {
        val baseColor = when (rank) {
            ShellRank.SYSTEM_CORE -> CHIMERA_SYS_GRAY
            ShellRank.USER_INPUT -> CHIMERA_USER_CYAN
            ShellRank.STDOUT -> Color(0xFFA0AEC0)
            ShellRank.STDERR -> CHIMERA_CRIT_RED
            ShellRank.DIAGNOSTIC -> CHIMERA_WARN_YELLOW
            ShellRank.LOOT_ALERT -> CHIMERA_GLOW_GREEN
        }
        
        withStyle(SpanStyle(color = baseColor)) {
            append(text)
        }
    }
}


@Composable
fun InteractiveRootShellDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val kernel = remember { PersistentRootKernel() }
    val logs = remember { mutableStateListOf<TerminalLog>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    
    val commandHistory = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableIntStateOf(-1) }
    
    var commandInput by remember { mutableStateOf("") }
    var showHeadsUp by remember { mutableStateOf(false) }

    
    LaunchedEffect(Unit) {
        kernel.engage()
        kernel.logStream.collectLatest { log ->
            logs.add(log)
            
            if (logs.size > 0) {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { kernel.purge() }
    }

    Dialog(
        onDismissRequest = onDismiss, 
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = CHIMERA_THEME_BG,
            border = androidx.compose.foundation.BorderStroke(1.dp, CHIMERA_SYS_GRAY.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A)) 
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                tint = CHIMERA_GLOW_GREEN,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.title_root_terminal),
                                color = CHIMERA_GLOW_GREEN,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (kernel.isAlive) CHIMERA_GLOW_GREEN else CHIMERA_CRIT_RED)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (kernel.isAlive) stringResource(R.string.status_connected) else stringResource(R.string.status_disconnected),
                                color = CHIMERA_SYS_GRAY,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.desc_close), tint = CHIMERA_SYS_GRAY)
                        }
                }
                
                
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF050B14))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickCommands = listOf("ls -la", "netstat -tulpn", "id", "ifconfig", "cat /data/system/packages.xml")
                    items(quickCommands) { cmd ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B), 
                            modifier = Modifier.clickable { kernel.dispatchCommand(cmd) }
                        ) {
                            Text(
                                text = cmd, 
                                color = CHIMERA_USER_CYAN, 
                                fontFamily = FontFamily.Monospace, 
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF020408))
                        .padding(8.dp)
                ) {
                    SelectionContainer {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(logs, key = { it.id }) { logItem ->
                                val timeStr = remember { SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(logItem.timestamp)) }
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "[$timeStr] ",
                                        color = CHIMERA_SYS_GRAY.copy(alpha = 0.5f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = buildTerminalSyntax(logItem.rawText, logItem.rank),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                     
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { 
                                DataExfiltrationEngine.exfiltrateLogsToDownload(logs, kernel)
                                scope.launch {
                                    showHeadsUp = true
                                    delay(3000)
                                    showHeadsUp = false
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = stringResource(R.string.desc_export_sdcard))
                        }
                        FloatingActionButton(
                            onClick = { logs.clear() },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.desc_clear_cache))
                        }
                    }

                    
                    Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showHeadsUp,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { -50 }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { -50 })
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CHIMERA_GLOW_GREEN.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CHIMERA_GLOW_GREEN)
                            ) {
                                Text(
                                    stringResource(R.string.msg_logs_saved), 
                                    color = CHIMERA_GLOW_GREEN,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(14.dp))
                            .border(1.dp, CHIMERA_USER_CYAN.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.prompt_root_short),
                            color = CHIMERA_GLOW_GREEN,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = CHIMERA_USER_CYAN
                            ),
                            placeholder = { Text(stringResource(R.string.hint_enter_command), color = CHIMERA_SYS_GRAY, fontFamily = FontFamily.Monospace) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (commandInput.isNotBlank()) {
                                        commandHistory.add(commandInput)
                                        historyIndex = commandHistory.size
                                        kernel.dispatchCommand(commandInput)
                                        commandInput = ""
                                    }
                                }
                            )
                        )
                        
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            IconButton(onClick = {
                                if (commandHistory.isNotEmpty() && historyIndex > 0) {
                                    historyIndex--
                                    commandInput = commandHistory[historyIndex]
                                }
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.desc_history_up), tint = CHIMERA_SYS_GRAY)
                            }
                            IconButton(onClick = {
                                if (commandHistory.isNotEmpty() && historyIndex < commandHistory.size - 1) {
                                    historyIndex++
                                    commandInput = commandHistory[historyIndex]
                                } else {
                                    historyIndex = commandHistory.size
                                    commandInput = ""
                                }
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.desc_history_down), tint = CHIMERA_SYS_GRAY)
                            }
                        }
                    }
                }
            }
        }
    }
}

