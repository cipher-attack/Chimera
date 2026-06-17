package com.chimera.zpqmxr.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AutoRunReceiver(private val onUsbConnected: () -> Unit) : BroadcastReceiver() {
    private var wasConnected = false
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.hardware.usb.action.USB_STATE") {
            val connected = intent.getBooleanExtra("connected", false)
            val configured = intent.getBooleanExtra("configured", false)
            
            Log.d("AutoRunReceiver", "USB State: connected=$connected, configured=$configured")
            
            if (connected && configured && !wasConnected) {
                onUsbConnected()
            }
            wasConnected = connected && configured
        }
    }
}
