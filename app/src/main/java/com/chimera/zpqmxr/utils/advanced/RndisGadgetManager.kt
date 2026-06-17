package com.chimera.zpqmxr.utils.advanced

import com.chimera.zpqmxr.utils.RootUtils

import com.chimera.zpqmxr.utils.NativeBridge
import com.chimera.zpqmxr.utils.NativeIOManager

object RndisGadgetManager {
    suspend fun setupRndisGadget(): Result<String> {
        val gadgetPath = ConfigFSFinder.findGadgetPath()
        if (gadgetPath.isEmpty()) {
            return Result.failure(Exception("Error: No ConfigFS USB Gadget path found across the host matrix."))
        }
        val udcName = ConfigFSFinder.getUdcName()
        ConfigFSFinder.unbindUdc(gadgetPath)
        
        val setupScript = """
            set -e
            
            cd "$gadgetPath"
            
            # 2. Advanced Error Handling for RNDIS Function Creation
            if [ ! -d functions/rndis.usb0 ]; then
                echo "Creating RNDIS function..."
                mkdir functions/rndis.usb0 || {
                    # Try to load module
                    modprobe usb_f_rndis || echo "Warning: module usb_f_rndis not found or failed to load."
                    mkdir functions/rndis.usb0 || {
                        echo "Error: Failed to create rndis.usb0 function! Device might not support f_rndis or resources are busy."
                        exit 1
                    }
                }
            else
                echo "RNDIS function already exists."
            fi
            
            # 3. Configure MAC Address (improves stability on Host PC)
            if [ -f functions/rndis.usb0/host_addr ]; then
                echo "00:11:22:33:44:55" > functions/rndis.usb0/host_addr || echo "Warning: Could not set host MAC address"
                echo "00:11:22:33:44:56" > functions/rndis.usb0/dev_addr || echo "Warning: Could not set dev MAC address"
            fi
            
            # 4. OS Descriptors for Windows (Required for Native Windows driver)
            if [ -d os_desc ]; then
                echo "1" > os_desc/use || echo "Warning: Cannot enable os_desc"
                echo "0xcd" > os_desc/b_vendor_code || echo "Warning: Cannot set vendor_code"
                echo "MSFT100" > os_desc/qw_sign || echo "Warning: Cannot set qw_sign"
            fi
            
            # 5. Link RNDIS function
            if [ ! -d configs/b.1 ]; then
                echo "Error: configs/b.1 not found. USB configuration incomplete."
                exit 1
            fi
            
            if [ ! -L configs/b.1/rndis.usb0 ]; then
                echo "Linking RNDIS to configs/b.1..."
                ln -s functions/rndis.usb0 configs/b.1/ || { 
                    echo "Error: Failed to link rndis.usb0. Check if UDC is disabled first."
                    exit 1 
                }
                echo "RNDIS gadget linked successfully."
            else
                echo "RNDIS gadget already linked."
            fi
        """.trimIndent()
        
        val setupRes = executeAndVerify(setupScript)
        if (setupRes.isFailure) return setupRes

        
        ConfigFSFinder.bindUdc(gadgetPath, udcName)
        
        val networkScript = """
            set -e
            
            # 6. Apply Network Configuration (Headless Server Management)
            NET_IF=${'$'}(ls /sys/class/net | grep -E 'usb0|rndis0' | head -n 1)
            if [ -z "${'$'}NET_IF" ]; then
                NET_IF="rndis0" # Fallback if neither found yet
            fi
            
            echo "Waiting for ${'$'}NET_IF interface to appear..."
            sleep 1
            ip link set ${'$'}NET_IF up 2>/dev/null || echo "Warning: Could not bring up ${'$'}NET_IF link immediately"
            
            echo "Assigning static IP 192.168.2.1 to ${'$'}NET_IF..."
            ifconfig ${'$'}NET_IF 192.168.2.1 netmask 255.255.255.0 up || ip addr add 192.168.2.1/24 dev ${'$'}NET_IF || echo "Warning: Failed to set IP on ${'$'}NET_IF"
            
            echo "Starting dnsmasq DHCP server on ${'$'}NET_IF..."
            # Run in background to prevent blocking su script
            nohup dnsmasq --interface=${'$'}NET_IF --bind-interfaces --dhcp-range=192.168.2.10,192.168.2.100,12h --pid-file=/data/local/tmp/dnsmasq_rndis.pid </dev/null >/dev/null 2>&1 &
            
            echo "SUCCESS: RNDIS setup complete with IP 192.168.2.1 on ${'$'}NET_IF"
        """.trimIndent()
        
        return executeAndVerify(networkScript)
    }

    suspend fun disableRndisGadget(): Result<String> {
        val gadgetPath = ConfigFSFinder.findGadgetPath()
        if (gadgetPath.isEmpty()) {
            return Result.success("SUCCESS: RNDIS gadget already disabled (ConfigFS missing).")
        }
        
        val script = """
            set -e
            
            cd "$gadgetPath"
            
            if [ -f /data/local/tmp/dnsmasq_rndis.pid ]; then
                kill ${'$'}(cat /data/local/tmp/dnsmasq_rndis.pid) 2>/dev/null || true
                rm /data/local/tmp/dnsmasq_rndis.pid 2>/dev/null || true
                echo "Stopped dnsmasq DHCP server."
            fi
            
            NET_IF=${'$'}(ls /sys/class/net | grep -E 'usb0|rndis0' | head -n 1)
            if [ -n "${'$'}NET_IF" ]; then
                ip link set ${'$'}NET_IF down 2>/dev/null || true
            fi
            
            if [ -L configs/b.1/rndis.usb0 ]; then
                rm configs/b.1/rndis.usb0 || {
                    echo "Error: Failed to remove RNDIS link"
                    exit 1
                }
            fi
            
            if [ -d functions/rndis.usb0 ]; then
                rmdir functions/rndis.usb0 || {
                    echo "Error: Failed to remove RNDIS function"
                    exit 1
                }
            fi
            echo "SUCCESS: RNDIS gadget disabled and unlinked."
        """.trimIndent()
        
        ConfigFSFinder.unbindUdc(gadgetPath)
        
        return executeAndVerify(script)
    }

    private suspend fun executeAndVerify(script: String): Result<String> {
        return try {
            val result = RootUtils.executeScript(script)
            val isError = result.contains("Error:", ignoreCase = true) || 
                          (result.contains("failed", ignoreCase = true) && !result.contains("Warning:", ignoreCase = true))

            if (isError) {
                Result.failure(Exception(result.trim()))
            } else {
                Result.success(result.trim())
            }
        } catch (e: Exception) {
            
            Result.failure(e)
        }
    }
}
