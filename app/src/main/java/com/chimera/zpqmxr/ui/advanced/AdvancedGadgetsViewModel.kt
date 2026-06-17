package com.chimera.zpqmxr.ui.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.zpqmxr.utils.advanced.AcmGadgetManager
import com.chimera.zpqmxr.utils.advanced.MidiGadgetManager
import com.chimera.zpqmxr.utils.advanced.RndisGadgetManager
import com.chimera.zpqmxr.utils.advanced.MassStorageManager
import com.chimera.zpqmxr.utils.advanced.HidLedDecoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class AdvancedGadgetsViewModel : ViewModel() {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _rndisEnabled = MutableStateFlow(false)
    val rndisEnabled = _rndisEnabled.asStateFlow()

    private val _acmEnabled = MutableStateFlow(false)
    val acmEnabled = _acmEnabled.asStateFlow()

    private val _midiEnabled = MutableStateFlow(false)
    val midiEnabled = _midiEnabled.asStateFlow()

    private val _massStorageEnabled = MutableStateFlow(false)
    val massStorageEnabled = _massStorageEnabled.asStateFlow()
    
    private val _lootFiles = MutableStateFlow<List<String>>(emptyList())
    val lootFiles = _lootFiles.asStateFlow()
    
    private val _hidExfilEnabled = MutableStateFlow(false)
    val hidExfilEnabled = _hidExfilEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            AcmGadgetManager.serialDataFlow.collectLatest { serialLog ->
                addLog(serialLog)
            }
        }
        viewModelScope.launch {
            HidLedDecoder.decodedDataFlow.collectLatest { decodedLog ->
                addLog(decodedLog)
            }
        }
    }

    private fun addLog(message: String) {
        val current = _logs.value.toMutableList()
        current.add(message)
        _logs.value = current
    }

    fun toggleRndis() {
        viewModelScope.launch {
            if (_rndisEnabled.value) {
                addLog("Attempting to disable RNDIS Gadget...")
                RndisGadgetManager.disableRndisGadget().onSuccess {
                    addLog("RNDIS Disabled successfully.\n$it")
                    _rndisEnabled.value = false
                }.onFailure {
                    addLog("Failed to disable RNDIS: ${it.message}")
                }
            } else {
                addLog("Attempting to enable RNDIS Gadget...")
                RndisGadgetManager.setupRndisGadget().onSuccess {
                    addLog("RNDIS Enabled successfully.\n$it")
                    _rndisEnabled.value = true
                }.onFailure {
                    addLog("Failed to enable RNDIS: ${it.message}")
                }
            }
        }
    }

    fun toggleAcm() {
        viewModelScope.launch {
            if (_acmEnabled.value) {
                addLog("Attempting to disable ACM Gadget...")
                AcmGadgetManager.disableAcmGadget().onSuccess {
                    addLog("ACM Disabled successfully.\n$it")
                    _acmEnabled.value = false
                }.onFailure {
                    addLog("Failed to disable ACM: ${it.message}")
                }
            } else {
                addLog("Attempting to enable ACM Gadget...")
                AcmGadgetManager.setupAcmGadget().onSuccess {
                    addLog("ACM Enabled successfully.\n$it")
                    _acmEnabled.value = true
                }.onFailure {
                    addLog("Failed to enable ACM: ${it.message}")
                }
            }
        }
    }

    fun toggleMidi() {
        viewModelScope.launch {
            if (_midiEnabled.value) {
                addLog("Attempting to disable MIDI Gadget...")
                MidiGadgetManager.disableMidiGadget().onSuccess {
                    addLog("MIDI Disabled successfully.\n$it")
                    _midiEnabled.value = false
                }.onFailure {
                    addLog("Failed to disable MIDI: ${it.message}")
                }
            } else {
                addLog("Attempting to enable MIDI Gadget...")
                MidiGadgetManager.setupMidiGadget().onSuccess {
                    addLog("MIDI Enabled successfully.\n$it")
                    _midiEnabled.value = true
                }.onFailure {
                    addLog("Failed to enable MIDI: ${it.message}")
                }
            }
        }
    }
    
    fun toggleMassStorage() {
        viewModelScope.launch {
            if (_massStorageEnabled.value) {
                addLog("Attempting to disable Mass Storage Gadget...")
                MassStorageManager.disableMassStorage().onSuccess {
                    addLog("Mass Storage Disabled successfully.\n$it")
                    _massStorageEnabled.value = false
                }.onFailure {
                    addLog("Failed to disable Mass Storage: ${it.message}")
                }
            } else {
                addLog("Attempting to enable Mass Storage Gadget...")
                MassStorageManager.setupMassStorage().onSuccess {
                    addLog("Mass Storage Enabled successfully.\n$it")
                    _massStorageEnabled.value = true
                    refreshLoot()
                }.onFailure {
                    addLog("Failed to enable Mass Storage: ${it.message}")
                }
            }
        }
    }
    
    fun refreshLoot() {
        viewModelScope.launch {
            MassStorageManager.listLootFiles().onSuccess {
                _lootFiles.value = it
            }.onFailure {
                addLog("Failed to read loot: ${it.message}")
            }
        }
    }
    
    fun toggleHidExfil() {
        if (_hidExfilEnabled.value) {
            addLog("Stopping HID Exfil Listener...")
            HidLedDecoder.stopDecoding()
            _hidExfilEnabled.value = false
        } else {
            addLog("Starting HID Exfil Listener...")
            HidLedDecoder.startDecoding()
            _hidExfilEnabled.value = true
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
