package com.nateapps.smartparcelapp

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LiveStatus(
    val liveTemp: String = "",
    val liveWeight: String = ""
)

// ✅ รวมค่า Process Status
data class ProcessStatus(
    var sender: String = "",
    var recipient: String = "",
    var sentTime: String = "",
    var trackId: String = "",
    var unlockCode: String = "",
    var flipCount: String = "",
    var impactCount: String = "",
    var tamperCount: String = "",
    var initialWeight: String = "",
    var currentWeight: String = "",
    var initialTemp: String = "",
    var currentTemp: String = ""
)

class HomeViewModel : ViewModel() {
    private val _statusReady = MutableStateFlow(false)
    val statusReady: StateFlow<Boolean> = _statusReady
    private val _connectedDeviceName = MutableStateFlow<String?>("SMARTPARCEL_1")
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName

    fun setConnectedDeviceName(name: String) {
        _connectedDeviceName.value = name
    }
    private val _macAddress = MutableStateFlow<String?>(null)
    val macAddress: StateFlow<String?> = _macAddress

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _currentConnectedMac = MutableStateFlow<String?>(null)
    val currentConnectedMac: StateFlow<String?> = _currentConnectedMac

    private val _connectedMac = MutableStateFlow<String?>(null)
    val connectedMac: StateFlow<String?> = _connectedMac

    private val _statusText = MutableStateFlow("")
    val statusText: StateFlow<String> = _statusText

    private val _parsedStatus = MutableStateFlow<String?>(null)
    val parsedStatus: StateFlow<String?> = _parsedStatus

    private val _processStatus = MutableStateFlow(ProcessStatus())
    val processStatus: StateFlow<ProcessStatus> = _processStatus
    private val _showSuccessState = MutableStateFlow(false)
    val showSuccessState: StateFlow<Boolean> = _showSuccessState

    private val _waitingForThank = MutableStateFlow(false)
    val waitingForThank: StateFlow<Boolean> = _waitingForThank

    // Waiting for status response
    private val _waitingForStatusResponse = MutableStateFlow(false)
    val waitingForStatusResponse: StateFlow<Boolean> = _waitingForStatusResponse

    fun setWaitingForStatusResponse(value: Boolean) {
        _waitingForStatusResponse.value = value
    }

    private val _historyFileList = MutableStateFlow<List<String>>(emptyList())
    val historyFileList: StateFlow<List<String>> = _historyFileList
    private val _hasFetchedHistory = MutableStateFlow(false)

    val hasFetchedHistory: Boolean
        get() = _hasFetchedHistory.value

    private val _waitingForStart = MutableStateFlow(false)
    val waitingForStart: StateFlow<Boolean> = _waitingForStart

    private val _liveStatus = MutableStateFlow(LiveStatus())
    val liveStatus: StateFlow<LiveStatus> = _liveStatus

    fun setLiveTemp(value: String) {
        _liveStatus.value = _liveStatus.value.copy(liveTemp = value)
    }

    fun setLiveWeight(value: String) {
        _liveStatus.value = _liveStatus.value.copy(liveWeight = value)
    }
    fun setWaitingForStart(value: Boolean) {
        _waitingForStart.value = value
    }
    fun setHasFetchedHistory(value: Boolean) {
        _hasFetchedHistory.value = value
    }
    fun updateHistoryFileList(files: List<String>) {
        _historyFileList.value = files
    }

    fun clearHistoryFileList() {
        _historyFileList.value = emptyList()
    }
    fun setShowSuccessState(value: Boolean) {
        _showSuccessState.value = value
    }

    fun setWaitingForThank(value: Boolean) {
        _waitingForThank.value = value
    }
    fun updateMac(mac: String?) {
        _macAddress.value = mac
    }

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    fun updateCurrentConnectedMac(mac: String?) {
        _currentConnectedMac.value = mac
    }

    fun updateConnectedMac(mac: String) {
        _connectedMac.value = mac
    }

    fun setStatus(text: String) {
        _statusText.value = text
        _parsedStatus.value = text.lineSequence().firstOrNull()?.trim()
        _statusReady.value = true
    }

    fun updateProcessStatus(update: ProcessStatus) {
        _processStatus.value = update
    }

    fun clearProcessStatus() {
        _processStatus.value = ProcessStatus() // รีเซ็ตค่า default
    }
    fun setCurrentTemp(value: String) {
        val current = _processStatus.value
        _processStatus.value = current.copy(currentTemp = value)
    }
    fun setCurrentWeight(value: String) {
        val current = _processStatus.value
        _processStatus.value = current.copy(currentWeight = value)
    }

    fun clearAllStateFromMain(context: android.content.Context) {
        setConnected(false)
        setStatus("")
        updateMac(null)
        updateCurrentConnectedMac(null)
        updateConnectedMac("")
        clearProcessStatus()
        clearHistoryFileList()
        setShowSuccessState(false)
        setWaitingForThank(false)
        setWaitingForStart(false)
    }

    private val _statusBuffer = MutableStateFlow<List<String>>(emptyList())
    val statusBuffer: StateFlow<List<String>> = _statusBuffer

    fun resetStatusBuffer(line: String) {
        _statusBuffer.value = listOf(line)
    }

    fun appendStatusLine(line: String) {
        _statusBuffer.value = _statusBuffer.value + line
    }
    fun clearStatusBuffer() {
        _statusBuffer.value = emptyList()
    }

    fun updateProcessStatusFromBuffer() {
        val lines = _statusBuffer.value
        val first = lines.firstOrNull()?.trim()?.lowercase()

        if (first in listOf("done", "unregistered") && lines.isNotEmpty()) {
            Log.d("BLE", "✅ Parsed: $first")
            Log.d("BLE", "📝 Raw Text:\n${_statusText.value}")
            _statusText.value = lines.joinToString("\n")
            _parsedStatus.value = first
            _statusReady.value = true
        } else if (first == "process" && lines.size >= 11) {
            _processStatus.value = ProcessStatus(
                sender = lines.getOrNull(1)?.trim() ?: "",
                recipient = lines.getOrNull(2)?.trim() ?: "",
                unlockCode = lines.getOrNull(3)?.trim() ?: "",
                trackId = lines.getOrNull(4)?.trim() ?: "",
                sentTime = lines.getOrNull(5)?.trim() ?: "",
                flipCount = lines.getOrNull(6)?.trim() ?: "",
                impactCount = lines.getOrNull(7)?.trim() ?: "",
                tamperCount = lines.getOrNull(8)?.trim() ?: "",
                initialWeight = lines.getOrNull(9)?.trim() ?: "",
                initialTemp = lines.getOrNull(10)?.trim() ?: ""
            )
            _statusText.value = lines.joinToString("\n")
            _parsedStatus.value = first
            _statusReady.value = true
        }
    }
}