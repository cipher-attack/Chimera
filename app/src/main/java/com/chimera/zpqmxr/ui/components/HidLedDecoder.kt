package com.chimera.zpqmxr.utils.advanced

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.DataOutputStream
import com.chimera.zpqmxr.utils.RootUtils

object HidLedDecoder {
    private val _decodedDataFlow = MutableSharedFlow<String>(replay = 100)
    val decodedDataFlow: SharedFlow<String> = _decodedDataFlow.asSharedFlow()

    private var decoderJob: Job? = null
    private var process: Process? = null

    fun startDecoding() {
        if (decoderJob?.isActive == true) return
        
        decoderJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process!!.outputStream)
                os.writeBytes("cat /dev/hidg0\n")
                os.flush()

                val inputStream = process!!.inputStream
                val buffer = ByteArray(16)
                var bytesRead: Int
                
                var bitBuffer = ""
                
                _decodedDataFlow.emit("[HID Exfil] Started listening to /dev/hidg0 for LED interrupts (Caps/Num lock flashes)...")
                
                while (isActive) {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        for (i in 0 until bytesRead) {
                            val byte = buffer[i].toInt()
                            
                            val numLock = (byte and 0x01) != 0
                            val capsLock = (byte and 0x02) != 0
                            
                            
                            
                            if (capsLock) bitBuffer += "1"
                            if (numLock) bitBuffer += "0"
                            
                            if (bitBuffer.length >= 8) {
                                val charCode = bitBuffer.substring(0, 8).toIntOrNull(2)
                                if (charCode != null) {
                                    val decodedChar = charCode.toChar()
                                    _decodedDataFlow.emit("[HID Exfil] Decoded char: \$decodedChar (Binary: \$bitBuffer)")
                                }
                                bitBuffer = ""
                            }
                        }
                    } else {
                        delay(20)
                    }
                }
            } catch (e: Exception) {
                _decodedDataFlow.emit("[HID Exfil] Reader Error: \${e.message}")
            }
        }
    }

    fun stopDecoding() {
        decoderJob?.cancel()
        decoderJob = null
        try {
            process?.destroy()
        } catch (e: Exception) { }
        process = null
        CoroutineScope(Dispatchers.IO).launch {
            _decodedDataFlow.emit("[HID Exfil] Stopped HID decoding.")
        }
    }
}
