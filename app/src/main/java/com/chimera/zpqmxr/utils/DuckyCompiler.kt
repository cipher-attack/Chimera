package com.chimera.zpqmxr.utils

object DuckyCompiler {
    
    val languages = mapOf(
        "US" to 0, "TR" to 1, "SV" to 2, "SI" to 3, "RU" to 4, 
        "PT" to 5, "NO" to 6, "IT" to 7, "HR" to 8, "GB" to 9, 
        "FR" to 10, "FI" to 11, "ES" to 12, "DK" to 13, "DE" to 14, 
        "CA" to 15, "BR" to 16, "BE" to 17, "HU" to 18
    )

    fun compile(duckyScript: String, languageId: Int = 0, customHidPath: String = ""): String {
        val sb = java.lang.StringBuilder()
        
        sb.append("""
            #!/system/bin/sh
            HID_DEV=""
            CUSTOM_PATH="$customHidPath"
            
            # Elite Auto Path Finder: Dynamically hunt down HID interfaces across the system
            for dev in "${'$'}CUSTOM_PATH" ${'$'}(find /dev -maxdepth 1 -name "hid*" 2>/dev/null) ${'$'}(ls /dev/hid* 2>/dev/null); do
                if [ -n "${'$'}dev" ]; then
                    if [ -c "${'$'}dev" ] || [ -e "${'$'}dev" ]; then
                        # Verify it's not a directory
                        if [ ! -d "${'$'}dev" ]; then
                            HID_DEV="${'$'}dev"
                            break
                        fi
                    fi
                fi
            done
            
            if [ -z "${'$'}HID_DEV" ]; then
                echo "Error: HID Gadget not found."
                echo "Required: Magisk + HID module, or a kernel with HID patch (like Chimera)."
                exit 1
            fi
            
            echo "Successfully targeted HID interface: ${'$'}HID_DEV"
            
            function send_keys() {
                echo -ne "${'$'}1" > "${'$'}HID_DEV"
                echo -ne "\x00\x00\x00\x00\x00\x00\x00\x00" > "${'$'}HID_DEV"
            }

            function send_mouse() {
                echo -ne "${'$'}1" > "${'$'}HID_DEV"
                echo -ne "\x00\x00\x00\x00" > "${'$'}HID_DEV"
            }
            
            function send_raw() {
                echo -ne "${'$'}1" > "${'$'}HID_DEV"
            }
            
            
        """.trimIndent())
        sb.append("\n")

        val hidParser = HID(languageId)
        hidParser.parse(duckyScript)
        
        for (cmd in hidParser.cmd) {
             sb.append(cmd)
        }
        
        return sb.toString()
    }
}
