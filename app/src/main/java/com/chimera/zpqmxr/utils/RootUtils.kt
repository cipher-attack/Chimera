package com.chimera.zpqmxr.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.async

object RootUtils {

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getRootVersion(): String = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-V"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            process.waitFor()
            line ?: "Unknown Configuration"
        } catch (e: Exception) {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-v"))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val line = reader.readLine()
                process.waitFor()
                line ?: "Unknown"
            } catch (ex: Exception) {
                "Not available"
            }
        }
    }

    suspend fun executeScript(script: String): String = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su")
            
            val outputStream = DataOutputStream(process.outputStream)
            
            
            outputStream.writeBytes(script + "\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = StringBuilder()
            val errorOutput = StringBuilder()
            
            val completed = kotlinx.coroutines.withTimeoutOrNull(25000L) { 
                kotlinx.coroutines.coroutineScope {
                    val outputDeferred = async(Dispatchers.IO) {
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            output.append(line).append("\n")
                        }
                    }
                    
                    val errorDeferred = async(Dispatchers.IO) {
                        var line: String?
                        while (errorReader.readLine().also { line = it } != null) {
                            errorOutput.append(line).append("\n")
                        }
                    }
                    
                    outputDeferred.await()
                    errorDeferred.await()
                    process.waitFor()
                }
            }
            
            if (completed == null) {
                process.destroy()
                return@withContext "Execution Timeout (25s reached)."
            }
            
            val result = output.append(errorOutput).toString()
            if (result.isBlank() && process.exitValue() != 0) {
                return@withContext "Execution failed (Exit Code: ${process.exitValue()}). Root may have been denied."
            } else if (result.isBlank()) {
                return@withContext "Execution completed with no output."
            }
            return@withContext result.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Error executing with su: ${e.message}\nMake sure your device is rooted."
        }
    }
}
