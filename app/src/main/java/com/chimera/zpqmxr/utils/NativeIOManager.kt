package com.chimera.zpqmxr.utils

object NativeIOManager {
    init {
        System.loadLibrary("nethunter_core")
    }

    @JvmStatic external fun checkRootNative(): Boolean
    @JvmStatic external fun writeBytesNative(path: String, data: ByteArray): Int
    @JvmStatic external fun writeStringNative(path: String, value: String): Int
}
