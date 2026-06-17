package com.chimera.zpqmxr.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Advanced ConfigFS Gadget Setup
 * Dynamically discovers the active gadget configuration, checks for existing HID functions
 * (like hid.gs1, hid.gs2, etc.), and binds them to the current configuration without breaking
 * the existing USB properties.
 */
object RuckyGadgetSetup {

    suspend fun setupAndEnableGadget(customGadgetPath: String = ""): String = withContext(Dispatchers.IO) {
        val script = """
            #!/system/bin/sh
            echo "Starting Advanced Dynamic Gadget Setup..."
            
            # Find the active gadget path
            GADGET_PATH=""
            CUSTOM_PATH="$customGadgetPath"
            
            if [ -n "${'$'}CUSTOM_PATH" ] && [ -d "${'$'}CUSTOM_PATH" ]; then
                GADGET_PATH="${'$'}CUSTOM_PATH"
            else
                # Elite Auto Path Finder: Aggressively hunt down active USB ConfigFS routes across Android
                for base in /config/usb_gadget /sys/kernel/config/usb_gadget /dev/usb-ffs; do
                    if [ -d "${'$'}base" ]; then
                        found=${'$'}(ls -d ${'$'}base/* 2>/dev/null | grep -Ev "dummy" | head -n 1)
                        if [ -n "${'$'}found" ] && [ -d "${'$'}found" ]; then
                            GADGET_PATH="${'$'}found"
                            break
                        fi
                    fi
                done
            fi
            
            if [ -z "${'$'}GADGET_PATH" ]; then
                echo "Error: Could not find active gadget path in /config/usb_gadget/"
                exit 1
            fi
            echo "Found Gadget Path: ${'$'}GADGET_PATH"
            
            # Find active config
            CONFIG_PATH=${'$'}(ls -d ${'$'}GADGET_PATH/configs/* | head -n 1)
            echo "Found Config Path: ${'$'}CONFIG_PATH"
            
            # Disable UDC temporarily to modify configs
            UDC_NAME=${'$'}(cat ${'$'}GADGET_PATH/UDC 2>/dev/null)
            if [ -z "${'$'}UDC_NAME" ]; then
                UDC_NAME=${'$'}(ls /sys/class/udc 2>/dev/null | head -n 1)
            fi
            if [ -z "${'$'}UDC_NAME" ]; then
                UDC_NAME=${'$'}(getprop sys.usb.controller)
            fi
            
            if [ -n "${'$'}UDC_NAME" ]; then
                echo "Unbinding UDC: ${'$'}UDC_NAME"
                echo "" > ${'$'}GADGET_PATH/UDC 2>/dev/null
            fi
            echo "Target UDC: ${'$'}UDC_NAME"
            
            # Find all available HID functions
            HID_FUNCS=${'$'}(ls ${'$'}GADGET_PATH/functions | grep hid)
            if [ -z "${'$'}HID_FUNCS" ]; then
                 echo "Warning: No pre-existing hid functions found in ${'$'}GADGET_PATH/functions. Attempting to create one..."
                 mkdir -p ${'$'}GADGET_PATH/functions/hid.keyboard > /dev/null 2>&1
                 echo "1" > ${'$'}GADGET_PATH/functions/hid.keyboard/protocol
                 echo "1" > ${'$'}GADGET_PATH/functions/hid.keyboard/subclass
                 echo "8" > ${'$'}GADGET_PATH/functions/hid.keyboard/report_length
                 echo -n -e '\x05\x01\x09\x06\xa1\x01\x05\x07\x19\xe0\x29\xe7\x15\x00\x25\x01\x75\x01\x95\x08\x81\x02\x95\x01\x75\x08\x81\x03\x95\x05\x75\x01\x05\x08\x19\x01\x29\x05\x91\x02\x95\x01\x75\x03\x91\x03\x95\x06\x75\x08\x15\x00\x25\x65\x05\x07\x19\x00\x29\x65\x81\x00\xc0' > ${'$'}GADGET_PATH/functions/hid.keyboard/report_desc
                 HID_FUNCS="hid.keyboard"
            fi
            
            echo "Available HID Functions: ${'$'}HID_FUNCS"
            
            # Link all HID functions to the active config
            for func in ${'$'}HID_FUNCS; do
                if [ ! -L "${'$'}CONFIG_PATH/${'$'}func" ]; then
                    echo "Linking function: ${'$'}func to ${'$'}CONFIG_PATH"
                    ln -s ${'$'}GADGET_PATH/functions/${'$'}func ${'$'}CONFIG_PATH/${'$'}func
                else
                    echo "Function ${'$'}func is already linked."
                fi
            done
            
            # Fix permissions on all possible hid character devices
            chmod 0666 /dev/hidg* 2>/dev/null
            chmod 0666 /dev/hid.* 2>/dev/null
            
            # Rebind UDC
            if [ -n "${'$'}UDC_NAME" ]; then
                echo "Rebinding UDC to apply changes..."
                echo "${'$'}UDC_NAME" > ${'$'}GADGET_PATH/UDC
                echo "Setup complete. Attack mode enabled."
            else
                echo "Could not resolve UDC name to rebind gadget."
            fi
        """.trimIndent()
        
        RootUtils.executeScript(script)
    }

    suspend fun disableGadget(customGadgetPath: String = ""): String = withContext(Dispatchers.IO) {
        val script = """
            #!/system/bin/sh
            echo "Disabling Advanced Dynamic Gadget Setup..."
            GADGET_PATH=""
            CUSTOM_PATH="$customGadgetPath"
            
            if [ -n "${'$'}CUSTOM_PATH" ] && [ -d "${'$'}CUSTOM_PATH" ]; then
                GADGET_PATH="${'$'}CUSTOM_PATH"
            else
                for base in /config/usb_gadget /sys/kernel/config/usb_gadget /dev/usb-ffs; do
                    if [ -d "${'$'}base" ]; then
                        found=${'$'}(ls -d ${'$'}base/* 2>/dev/null | grep -Ev "dummy" | head -n 1)
                        if [ -n "${'$'}found" ] && [ -d "${'$'}found" ]; then
                            GADGET_PATH="${'$'}found"
                            break
                        fi
                    fi
                done
            fi
            
            if [ -z "${'$'}GADGET_PATH" ]; then
                echo "No gadget path found."
                exit 0
            fi
            
            CONFIG_PATH=${'$'}(ls -d ${'$'}GADGET_PATH/configs/* | head -n 1)
            UDC_NAME=${'$'}(cat ${'$'}GADGET_PATH/UDC 2>/dev/null)
            if [ -z "${'$'}UDC_NAME" ]; then
                UDC_NAME=${'$'}(ls /sys/class/udc 2>/dev/null | head -n 1)
            fi
            if [ -z "${'$'}UDC_NAME" ]; then
                UDC_NAME=${'$'}(getprop sys.usb.controller)
            fi
            
            echo "Unbinding UDC..."
            echo "" > ${'$'}GADGET_PATH/UDC 2>/dev/null
            
            HID_FUNCS=${'$'}(ls ${'$'}CONFIG_PATH | grep hid)
            for func in ${'$'}HID_FUNCS; do
                echo "Unlinking function: ${'$'}func from ${'$'}CONFIG_PATH"
                rm ${'$'}CONFIG_PATH/${'$'}func
            done
            
            if [ -z "${'$'}UDC_NAME" ]; then
                UDC_NAME=${'$'}(getprop sys.usb.controller)
            fi
            
            if [ -n "${'$'}UDC_NAME" ]; then
                echo "Rebinding original UDC: ${'$'}UDC_NAME"
                echo "${'$'}UDC_NAME" > ${'$'}GADGET_PATH/UDC
            fi
            echo "Dynamic Attack mode disabled."
        """.trimIndent()
        
        RootUtils.executeScript(script)
    }
}
