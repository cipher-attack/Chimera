package com.chimera.zpqmxr.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object RndisGadgetManager {
    private const val TAG = "RndisGadgetManager"
    
    suspend fun enableRndis(customPath: String = ""): String = withContext(Dispatchers.IO) {
        val basePath = if (customPath.isNotEmpty()) customPath else "/config/usb_gadget/g1"
        Log.d(TAG, "Enabling RNDIS at $basePath")
        
        val script = """
            #!/system/bin/sh
            
            BASE="$basePath"
            
            # Find the active UDC
            UDC_NAME=${'$'}(ls /sys/class/udc | head -n 1)
            
            # CRITICAL: Unbind the UDC first
            if [ -n "${'$'}UDC_NAME" ]; then
                echo "" > "${'$'}BASE/UDC" 2>/dev/null || true
            fi
            sleep 0.5
            
            cd "${'$'}BASE"
            
            # Use existing configs/b.1 or fallback
            CONFIG="configs/b.1"
            if [ ! -d "${'$'}CONFIG" ]; then
                CONFIG="configs/c.1"
                mkdir -p "${'$'}CONFIG/strings/0x409"
                echo "RNDIS" > "${'$'}CONFIG/strings/0x409/configuration"
            fi
            
            # Determine correct RNDIS function
            RNDIS_FUNC=""
            if mkdir -p functions/rndis.gs4 2>/dev/null || [ -d functions/rndis.gs4 ]; then
                RNDIS_FUNC="rndis.gs4"
            elif mkdir -p functions/rndis_bam.rndis 2>/dev/null || [ -d functions/rndis_bam.rndis ]; then
                RNDIS_FUNC="rndis_bam.rndis"
            elif mkdir -p functions/rndis.0 2>/dev/null || [ -d functions/rndis.0 ]; then
                RNDIS_FUNC="rndis.0"
            else
                echo "Failed to create any RNDIS function (rndis.gs4, rndis_bam.rndis, rndis.0)"
                exit 1
            fi
            
            echo "Using RNDIS function: ${'$'}RNDIS_FUNC"
            ln -s "functions/${'$'}RNDIS_FUNC" "${'$'}CONFIG/" 2>/dev/null || true
            
            # Rebind the UDC
            echo "${'$'}UDC_NAME" > UDC
            if [ ${'$'}? -eq 0 ]; then
                echo "Success: UDC ${'$'}UDC_NAME bound to ${'$'}BASE"
            else
                echo "Failed to bind UDC ${'$'}UDC_NAME"
                exit 1
            fi
            
            # Try to bring up an interface and set static IP
            sleep 1
            IFACE=${'$'}(ls /sys/class/net | grep -E 'usb0|rndis0' | head -n 1)
            if [ -z "${'$'}IFACE" ]; then
                IFACE="rndis0"
            fi
            ifconfig ${'$'}IFACE 10.0.0.1 netmask 255.255.255.0 up 2>/dev/null || ip addr add 10.0.0.1/24 dev ${'$'}IFACE 2>/dev/null
            ip link set ${'$'}IFACE up 2>/dev/null
            
            echo "RNDIS interface enabled with IP 10.0.0.1. Set your PC to 10.0.0.2 to connect."
        """.trimIndent()
        
        RootUtils.executeScript(script)
    }

    suspend fun disableRndis(customPath: String = ""): String = withContext(Dispatchers.IO) {
        val basePath = if (customPath.isNotEmpty()) customPath else "/config/usb_gadget/g1"
        
        val script = """
            #!/system/bin/sh
            BASE="$basePath"
            if [ -d "${'$'}BASE" ]; then
                # Unbind first
                echo "" > "${'$'}BASE/UDC" 2>/dev/null || true
                
                # Cleanup rndis links in possible configs
                rm -f "${'$'}BASE"/configs/b.1/rndis.* 2>/dev/null || true
                rm -f "${'$'}BASE"/configs/b.1/rndis_bam.* 2>/dev/null || true
                rm -f "${'$'}BASE"/configs/c.1/rndis.* 2>/dev/null || true
                rm -f "${'$'}BASE"/configs/c.1/rndis_bam.* 2>/dev/null || true
                
                echo "RNDIS Gadget unbound and cleaned up."
            else
                echo "Gadget path ${'$'}BASE not found."
            fi
            
            # Request Android to re-configure USB
            setprop sys.usb.config none
            sleep 0.5
            setprop sys.usb.config mtp,adb
        """.trimIndent()
        RootUtils.executeScript(script)
    }
}
