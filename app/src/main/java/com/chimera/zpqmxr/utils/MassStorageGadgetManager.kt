package com.chimera.zpqmxr.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MassStorageGadgetManager {
    private const val TAG = "MassStorageGadgetManager"

    suspend fun mountIso(isoPath: String): String = withContext(Dispatchers.IO) {
        val script = """
            #!/system/bin/sh
            ISO_PATH="$isoPath"
            # Elite Base Finder
            CFG_BASE="/config/usb_gadget"
            for b in /config/usb_gadget /sys/kernel/config/usb_gadget /dev/usb-ffs; do
                if [ -d "${'$'}b" ]; then
                    CFG_BASE="${'$'}b"
                    break
                fi
            done
            BASE="${'$'}CFG_BASE/chimera_iso"

            # Disable current USB config
            setprop sys.usb.config none

            # Elite Auto Path Finder for UDC
            UDC_NAME=${'$'}(ls /sys/class/udc 2>/dev/null | head -n 1)
            if [ -z "${'$'}UDC_NAME" ]; then
                UDC_NAME=${'$'}(getprop sys.usb.controller)
            fi

            # Unbind existing gadgets
            for g in "${'$'}CFG_BASE"/*; do
              if [ -f "${'$'}g/UDC" ] && [ "${'$'}(cat "${'$'}g/UDC")" == "${'$'}UDC_NAME" ]; then
                echo "" > "${'$'}g/UDC"
              fi
            done

            mkdir -p "${'$'}BASE"
            cd "${'$'}BASE"
            echo "0x1d6b" > idVendor
            echo "0x0104" > idProduct

            mkdir -p strings/0x409
            echo "Google" > strings/0x409/manufacturer
            echo "CHIMERA ISO" > strings/0x409/product
            
            mkdir -p configs/c.1/strings/0x409
            echo "Mass Storage" > configs/c.1/strings/0x409/configuration
            
            FUNC_DIR="functions/mass_storage.0"
            mkdir -p "${'$'}FUNC_DIR" 2>/dev/null || FUNC_DIR="functions/mass_storage.1"
            mkdir -p "${'$'}FUNC_DIR" 2>/dev/null
            
            if [ -d "${'$'}FUNC_DIR" ]; then
                 # Try to set as cdrom, but some kernels don't have cdrom attribute
                 echo 1 > "${'$'}FUNC_DIR/lun.0/cdrom" 2>/dev/null
                 echo 1 > "${'$'}FUNC_DIR/lun.0/ro" 2>/dev/null
                 echo "${'$'}ISO_PATH" > "${'$'}FUNC_DIR/lun.0/file"
                 ln -s "${'$'}FUNC_DIR" configs/c.1/ 2>/dev/null
            else
                 echo "Mass storage function not supported by kernel."
                 exit 1
            fi
            
            echo "${'$'}UDC_NAME" > UDC
            if [ ${'$'}? -eq 0 ]; then
                echo "Success: ISO Mounted from ${'$'}ISO_PATH"
            else
                echo "Failed to bind UDC ${'$'}UDC_NAME"
                exit 1
            fi
        """.trimIndent()
        
        RootUtils.executeScript(script)
    }

    suspend fun unmountIso(): String = withContext(Dispatchers.IO) {
        val script = """
            #!/system/bin/sh
            # Elite Base Finder
            CFG_BASE="/config/usb_gadget"
            for b in /config/usb_gadget /sys/kernel/config/usb_gadget /dev/usb-ffs; do
                if [ -d "${'$'}b" ]; then
                    CFG_BASE="${'$'}b"
                    break
                fi
            done
            BASE="${'$'}CFG_BASE/chimera_iso"
            if [ -d "${'$'}BASE" ]; then
                echo "" > "${'$'}BASE/UDC"
                rm -f "${'$'}BASE"/configs/c.1/mass_storage.*
                echo "ISO Gadget unbound."
            else
                echo "ISO Gadget path ${'$'}BASE not found."
            fi
            
            setprop sys.usb.config mtp,adb
        """.trimIndent()
        RootUtils.executeScript(script)
    }
}
