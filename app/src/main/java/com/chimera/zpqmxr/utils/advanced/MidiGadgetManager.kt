package com.chimera.zpqmxr.utils.advanced

import com.chimera.zpqmxr.utils.RootUtils

object MidiGadgetManager {
    suspend fun setupMidiGadget(): Result<String> {
        val gadgetPath = ConfigFSFinder.findGadgetPath()
        if (gadgetPath.isEmpty()) {
            return Result.failure(Exception("Error: No ConfigFS USB Gadget path found across the host matrix."))
        }
        val udcName = ConfigFSFinder.getUdcName()
        ConfigFSFinder.unbindUdc(gadgetPath)
        
        val script = """
            set -e
            cd "$gadgetPath"
            if [ ! -d functions/midi.usb0 ]; then
                mkdir functions/midi.usb0 2>/dev/null || {
                    modprobe usb_f_midi >/dev/null 2>&1 || true
                    mkdir functions/midi.usb0 2>/dev/null || {
                        echo "Error: Your device kernel does not support USB MIDI (missing CONFIG_USB_F_MIDI module)."
                        exit 1
                    }
                }
                echo "1" > functions/midi.usb0/in_ports || echo "Warning: could not set in_ports"
                echo "1" > functions/midi.usb0/out_ports || echo "Warning: could not set out_ports"
                echo "Created MIDI function"
            else
                echo "MIDI function already exists"
            fi
            
            if [ ! -d configs/b.1 ]; then
                echo "Error: configs/b.1 not found. USB configuration incomplete."
                exit 1
            fi
            
            if [ ! -L configs/b.1/midi.usb0 ]; then
                ln -s functions/midi.usb0 configs/b.1/ || { echo "Error: Failed to link midi.usb0."; exit 1; }
                echo "MIDI gadget linked."
            else
                echo "MIDI gadget already linked."
            fi
        """.trimIndent()
        
        return try {
            val result = RootUtils.executeScript(script)
            if (result.contains("Error:", ignoreCase = true) || result.contains("failed", ignoreCase = true) && !result.contains("Warning")) {
                Result.failure(Exception(result.trim()))
            } else {
                ConfigFSFinder.bindUdc(gadgetPath, udcName)
                Result.success(result.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun disableMidiGadget(): Result<String> {
        val gadgetPath = ConfigFSFinder.findGadgetPath()
        if (gadgetPath.isEmpty()) return Result.success("Gadget disabled")
        
        ConfigFSFinder.unbindUdc(gadgetPath)
        
        val script = """
            cd "$gadgetPath" || exit 0
            if [ -L configs/b.1/midi.usb0 ]; then
                rm configs/b.1/midi.usb0 || echo "Failed to remove link"
            fi
            if [ -d functions/midi.usb0 ]; then
                rmdir functions/midi.usb0 || echo "Failed to remove function"
            fi
            echo "MIDI gadget disabled and unlinked."
        """.trimIndent()
        
        return try {
            val result = RootUtils.executeScript(script)
            Result.success(result.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
