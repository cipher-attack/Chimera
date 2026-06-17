package com.chimera.zpqmxr.utils

object NativeBridge {
    init {
        System.loadLibrary("nethunter_core")
    }

    @JvmStatic external fun enableRndisNative(udcName: String): Int
    @JvmStatic external fun performDeepHardwareScan(): String
}
