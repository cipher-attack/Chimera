package com.chimera.zpqmxr.utils

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class BluetoothHidManager(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    
    private var hidDevice: BluetoothHidDevice? = null
    var connectedDevice: BluetoothDevice? = null
        private set

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered = _isRegistered.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()
    
    private val executor = Executors.newSingleThreadExecutor()

    private val compositeDescriptor = intArrayOf(
        
        0x05, 0x01, 0x09, 0x06, 0xa1, 0x01, 0x85, 0x01,
        0x05, 0x07, 0x19, 0xe0, 0x29, 0xe7, 0x15, 0x00,
        0x25, 0x01, 0x75, 0x01, 0x95, 0x08, 0x81, 0x02,
        0x95, 0x01, 0x75, 0x08, 0x81, 0x01, 0x95, 0x05,
        0x75, 0x01, 0x05, 0x08, 0x19, 0x01, 0x29, 0x05, 0x91, 0x02,
        0x95, 0x01, 0x75, 0x03, 0x91, 0x01, 0x95, 0x06,
        0x75, 0x08, 0x15, 0x00, 0x25, 0x65, 0x05, 0x07, 0x19, 0x00,
        0x29, 0x65, 0x81, 0x00, 0xc0,
        
        0x05, 0x01, 0x09, 0x02, 0xa1, 0x01, 0x85, 0x02,
        0x09, 0x01, 0xa1, 0x00, 0x05, 0x09, 0x19, 0x01,
        0x29, 0x03, 0x15, 0x00, 0x25, 0x01, 0x95, 0x03,
        0x75, 0x01, 0x81, 0x02, 0x95, 0x01, 0x75, 0x05,
        0x81, 0x03, 0x05, 0x01, 0x09, 0x30, 0x09, 0x31,
        0x09, 0x38, 0x15, 0x81, 0x25, 0x7f, 0x75, 0x08,
        0x95, 0x03, 0x81, 0x06, 0xc0, 0xc0
    ).map { it.toByte() }.toByteArray()

    private val sdpSettings = BluetoothHidDeviceAppSdpSettings(
        "Rucky Composite", "Rucky", "Google",
        BluetoothHidDevice.SUBCLASS1_COMBO.toByte(),
        compositeDescriptor
    )

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            _isRegistered.value = registered
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device
                _isConnected.value = true
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevice = null
                _isConnected.value = false
            }
        }
    }

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                try {
                    hidDevice?.registerApp(
                        sdpSettings, null, null,
                        executor, callback
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
                _isRegistered.value = false
                _isConnected.value = false
            }
        }
    }

    private val autoPairReceiver = object : android.content.BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: android.content.Intent?) {
            val action = intent?.action
            val device = intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            
            if (action == BluetoothDevice.ACTION_PAIRING_REQUEST) {
                val variant = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR)
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (variant == BluetoothDevice.PAIRING_VARIANT_PIN || variant == 7) { 
                            val setPinMethod = device?.javaClass?.getMethod("setPin", ByteArray::class.java)
                            
                            setPinMethod?.invoke(device, "0000".toByteArray())
                            
                            val confirmMethod = device?.javaClass?.getMethod("setPairingConfirmation", Boolean::class.javaPrimitiveType)
                            confirmMethod?.invoke(device, true)
                        } else {
                            val method = device?.javaClass?.getMethod("setPairingConfirmation", Boolean::class.javaPrimitiveType)
                            method?.invoke(device, true)
                        }
                    } catch (e: Exception) {
                         
                    }
                    
                    
                    RootUtils.executeScript("sleep 0.3 && input keyevent 22 && input keyevent 22 && input keyevent 66")
                    RootUtils.executeScript("sleep 0.2 && input keyevent 66")
                }
            } else if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                if (bondState == BluetoothDevice.BOND_BONDED && device != null) {
                    
                    try {
                         val connectMethod = hidDevice?.javaClass?.getMethod("connect", BluetoothDevice::class.java)
                         connectMethod?.invoke(hidDevice, device)
                    } catch(e: Exception) {
                         
                    }
                }
            }
        }
    }

    fun init() {
        if (bluetoothAdapter?.isEnabled == true) {
            bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
            
            val filter = android.content.IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST)
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(autoPairReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(autoPairReceiver, filter)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun pairDeviceRoot(macAddress: String, onLog: (String) -> Unit) {
        try {
            val device = bluetoothAdapter?.getRemoteDevice(macAddress.uppercase())
            if (device == null) {
                onLog("Invalid MAC Address: $macAddress")
                return
            }
            onLog("Initiating stealth pairing to $macAddress ...")
            device.createBond()
        } catch (e: Exception) {
            onLog("Pairing Error: ${e.message}")
        }
    }

    fun teardown() {
        if (hidDevice != null) {
            hidDevice?.unregisterApp()
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
        }
    }

    fun sendKeys(modifier: Byte, key: Byte) {
        val device = connectedDevice ?: return
        val report = ByteArray(8)
        report[0] = modifier
        report[2] = key
        
        hidDevice?.sendReport(device, 1, report)
        
        val releaseReport = ByteArray(8)
        hidDevice?.sendReport(device, 1, releaseReport)
    }

    fun sendRaw(reportId: Int, report: ByteArray) {
        val device = connectedDevice ?: return
        hidDevice?.sendReport(device, reportId, report)
    }
}
