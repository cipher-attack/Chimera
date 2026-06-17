package com.chimera.zpqmxr.utils.advanced

import com.chimera.zpqmxr.utils.RootUtils

object MassStorageManager {
    const val IMAGE_PATH = "/data/local/tmp/mass_storage.img"
    const val MOUNT_PATH = "/data/local/tmp/loot_mount"

    suspend fun setupMassStorage(): Result<String> {
        val gadgetPath = ConfigFSFinder.findGadgetPath()
        if (gadgetPath.isEmpty()) {
            return Result.failure(Exception("Error: No ConfigFS USB Gadget path found across the host matrix."))
        }
        val udcName = ConfigFSFinder.getUdcName()
        ConfigFSFinder.unbindUdc(gadgetPath)
        
        val script = """
            set -e
            if [ ! -f $IMAGE_PATH ]; then
                echo "Creating backing image (64MB)..."
                dd if=/dev/zero of=$IMAGE_PATH bs=1048576 count=64
                newfs_msdos -F 32 $IMAGE_PATH || mkfs.vfat $IMAGE_PATH || mke2fs -t ext4 $IMAGE_PATH || echo "Warning: Formatting failed, might need formatting from host"
            fi
            
            mkdir -p $MOUNT_PATH
            mount -o loop,rw $IMAGE_PATH $MOUNT_PATH || echo "Warning: local mount failed"
            
            cd "$gadgetPath"
            if [ ! -d functions/mass_storage.0 ]; then
                mkdir functions/mass_storage.0 2>/dev/null || {
                    modprobe usb_f_mass_storage >/dev/null 2>&1 || true
                    mkdir functions/mass_storage.0 2>/dev/null || {
                        echo "Error: Your device kernel does not support USB Mass Storage (missing usb_f_mass_storage module)."
                        exit 1
                    }
                }
                echo 1 > functions/mass_storage.0/lun.0/removable || true
                echo 0 > functions/mass_storage.0/lun.0/ro || true
                echo $IMAGE_PATH > functions/mass_storage.0/lun.0/file || { echo "Error: Failed to set backing file"; exit 1; }
                echo "Created Mass Storage function"
            else
                echo "Mass Storage already exists"
                echo $IMAGE_PATH > functions/mass_storage.0/lun.0/file || true
            fi
            
            if [ ! -d configs/b.1 ]; then
                echo "Error: configs/b.1 not found. USB configuration incomplete."
                exit 1
            fi
            
            if [ ! -L configs/b.1/mass_storage.0 ]; then
                ln -s functions/mass_storage.0 configs/b.1/ || { echo "Error: Failed to link mass_storage.0"; exit 1; }
                echo "Mass Storage gadget linked successfully. Drop-zone ready."
            else
                echo "Mass Storage gadget already linked"
            fi
        """.trimIndent()
        
        val setupRes = executeAndVerify(script)
        if (setupRes.isFailure) return setupRes

        ConfigFSFinder.bindUdc(gadgetPath, udcName)
        return setupRes
    }

    suspend fun disableMassStorage(): Result<String> {
        val gadgetPath = ConfigFSFinder.findGadgetPath()
        if (gadgetPath.isEmpty()) return Result.success("Gadget disabled")
        
        ConfigFSFinder.unbindUdc(gadgetPath)
        
        val script = """
            set -e
            umount $MOUNT_PATH || true
            
            cd "$gadgetPath"
            if [ -L configs/b.1/mass_storage.0 ]; then
                rm configs/b.1/mass_storage.0 || echo "Failed to remove link"
            fi
            if [ -d functions/mass_storage.0 ]; then
                echo "" > functions/mass_storage.0/lun.0/file || true
                rmdir functions/mass_storage.0 || echo "Failed to remove function"
            fi
            echo "Mass Storage gadget disabled."
        """.trimIndent()
        return executeAndVerify(script)
    }
    
    suspend fun listLootFiles(): Result<List<String>> {
        val script = """
            ls -1 $MOUNT_PATH
        """.trimIndent()
        return try {
            val result = RootUtils.executeScript(script)
            if (result.contains("No such file or directory", ignoreCase = true)) {
                Result.success(emptyList())
            } else {
                Result.success(result.split("\n").filter { it.isNotBlank() })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun executeAndVerify(script: String): Result<String> {
        return try {
            val result = RootUtils.executeScript(script)
            if (result.contains("Error:", ignoreCase = true) || (result.contains("failed", ignoreCase = true) && !result.contains("Warning", ignoreCase = true))) {
                Result.failure(Exception(result.trim()))
            } else {
                Result.success(result.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
