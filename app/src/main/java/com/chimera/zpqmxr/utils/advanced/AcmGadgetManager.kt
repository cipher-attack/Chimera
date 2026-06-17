package com.chimera.zpqmxr.utils.advanced

import com.chimera.zpqmxr.utils.RootUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object AcmGadgetManager {

    private val _serialDataFlow = MutableSharedFlow<String>(replay = 100)
    val serialDataFlow: SharedFlow<String> = _serialDataFlow.asSharedFlow()

    private var serialReaderJob: Job? = null
    private var process: Process? = null

    suspend fun setupAcmGadget(): Result<String> {
        val gadgetPath = ConfigFSFinder.findGadgetPath()
        if (gadgetPath.isEmpty()) {
            return Result.failure(Exception("Error: No ConfigFS USB Gadget path found across the host matrix."))
        }
        val udcName = ConfigFSFinder.getUdcName()
        ConfigFSFinder.unbindUdc(gadgetPath)
        
        val script = """
            set -e
            cd "$gadgetPath"
            if [ ! -d functions/acm.usb0 ]; then
                mkdir functions/acm.usb0 || { echo "Error: Failed to create acm.usb0 function! Device may not support ACM or resource busy."; exit 1; }
                echo "Created ACM function"
            else
                echo "ACM function already exists"
            fi
            
            if [ ! -d configs/b.1 ]; then
                echo "Error: configs/b.1 not found. USB configuration incomplete."
                exit 1
            fi
            
            if [ ! -L configs/b.1/acm.usb0 ]; then
                ln -s functions/acm.usb0 configs/b.1/ || { echo "Error: Failed to link acm.usb0."; exit 1; }
                echo "ACM gadget linked."
            else
                echo "ACM gadget already linked."
            fi
        """.trimIndent()
        
        return try {
            val result = RootUtils.executeScript(script)
            if (result.contains("Error:", ignoreCase = true) || result.contains("failed", ignoreCase = true)) {
                Result.failure(Exception(result.trim()))
            } else {
                ConfigFSFinder.bindUdc(gadgetPath, udcName)
                startSerialReader()
                Result.success(result.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun disableAcmGadget(): Result<String> {
        stopSerialReader()
        
        val gadgetPath = ConfigFSFinder.findGadgetPath()
        if (gadgetPath.isEmpty()) return Result.success("Gadget disabled")
        
        ConfigFSFinder.unbindUdc(gadgetPath)
        
        val script = """
            cd "$gadgetPath" || exit 0
            if [ -L configs/b.1/acm.usb0 ]; then
                rm configs/b.1/acm.usb0 || echo "Failed to remove link"
            fi
            if [ -d functions/acm.usb0 ]; then
                rmdir functions/acm.usb0 || echo "Failed to remove function"
            fi
            echo "ACM gadget disabled and unlinked."
        """.trimIndent()
        
        return try {
            val result = RootUtils.executeScript(script)
            Result.success(result.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun startSerialReader() {
        if (serialReaderJob?.isActive == true) return

        serialReaderJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process!!.outputStream)
                os.writeBytes("cat /dev/ttyGS0\n")
                os.flush()

                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String? = null
                while (isActive && reader.readLine().also { line = it } != null) {
                    line?.let {
                        _serialDataFlow.emit("[ACM Serial] \$it")
                    }
                }
            } catch (e: Exception) {
                _serialDataFlow.emit("[ACM Serial] Reader Error: \${e.message}")
            }
        }
    }

    private fun stopSerialReader() {
        serialReaderJob?.cancel()
        serialReaderJob = null
        try {
            process?.destroy()
        } catch (e: Exception) {
            
        }
        process = null
    }
}
