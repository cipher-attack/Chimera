package com.chimera.zpqmxr.utils.advanced

import com.chimera.zpqmxr.utils.RootUtils

object ConfigFSFinder {
    
    suspend fun findGadgetPath(): String {
        val script = """
            GADGET_PATH=""
            for base in /config/usb_gadget /sys/kernel/config/usb_gadget /dev/usb-ffs; do
                if [ -d "${'$'}base" ]; then
                    found=${'$'}(ls -d ${'$'}base/* 2>/dev/null | grep -Ev "dummy" | head -n 1)
                    if [ -n "${'$'}found" ] && [ -d "${'$'}found" ]; then
                        GADGET_PATH="${'$'}found"
                        break
                    fi
                fi
            done
            if [ -z "${'$'}GADGET_PATH" ]; then
                # Try to create a new g1 if base configfs exists
                if [ -d /config/usb_gadget ]; then
                    mkdir -p /config/usb_gadget/g1 2>/dev/null && GADGET_PATH="/config/usb_gadget/g1"
                    echo "0x1d6b" > /config/usb_gadget/g1/idVendor 2>/dev/null
                    echo "0x0104" > /config/usb_gadget/g1/idProduct 2>/dev/null
                    echo "0x0100" > /config/usb_gadget/g1/bcdDevice 2>/dev/null
                    echo "0x0200" > /config/usb_gadget/g1/bcdUSB 2>/dev/null
                    mkdir -p /config/usb_gadget/g1/strings/0x409 2>/dev/null
                    echo "Chimera" > /config/usb_gadget/g1/strings/0x409/manufacturer 2>/dev/null
                    echo "Toolkit" > /config/usb_gadget/g1/strings/0x409/product 2>/dev/null
                    mkdir -p /config/usb_gadget/g1/configs/b.1/strings/0x409 2>/dev/null
                fi
            fi
            echo "${'$'}GADGET_PATH"
        """.trimIndent()
        
        return RootUtils.executeScript(script).trim()
    }
    
    suspend fun getUdcName(): String {
        var udc = RootUtils.executeScript("ls /sys/class/udc | head -n 1").trim()
        if (udc.isEmpty() || udc.contains("No such file", ignoreCase = true)) {
            udc = RootUtils.executeScript("getprop sys.usb.controller").trim()
        }
        return udc
    }
    
    suspend fun unbindUdc(gadgetPath: String) {
        val script = """
            if [ -n "$gadgetPath" ] && [ -d "$gadgetPath" ]; then
                echo "" > "$gadgetPath/UDC" 2>/dev/null || true
            fi
        """.trimIndent()
        RootUtils.executeScript(script)
    }
    
    suspend fun bindUdc(gadgetPath: String, udcName: String) {
        val script = """
            if [ -n "$gadgetPath" ] && [ -d "$gadgetPath" ]; then
                echo "$udcName" > "$gadgetPath/UDC" 2>/dev/null || true
            fi
        """.trimIndent()
        RootUtils.executeScript(script)
    }
}
