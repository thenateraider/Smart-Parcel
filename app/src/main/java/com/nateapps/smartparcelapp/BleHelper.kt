package com.nateapps.smartparcelapp

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import java.util.*

object BleHelper {

    private var bluetoothGatt: BluetoothGatt? = null
    private var commCharacteristic: BluetoothGattCharacteristic? = null
    private var onMessageReceived: ((String) -> Unit)? = null

    private val SERVICE_UUID = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
    private val CHAR_UUID = UUID.fromString("0000dcba-0000-1000-8000-00805f9b34fb")
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    fun connect(
        context: Context,
        macAddress: String,
        onConnected: () -> Unit,
        onFail: () -> Unit
    ) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (!BluetoothAdapter.checkBluetoothAddress(macAddress)) {
            Log.e("BLE", "Invalid MAC Address")
            onFail()
            return
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("BLE", "Permission denied: BLUETOOTH_CONNECT")
            onFail()
            return
        }

        val device = bluetoothAdapter.getRemoteDevice(macAddress)
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i("BLE", "Connected to $macAddress")
                    onConnected()
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e("BLE", "Disconnected from $macAddress")
                    onFail()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED
                ) return

                val service = gatt.getService(SERVICE_UUID)
                if (service == null) {
                    Log.e("BLE", "Service not found")
                    return
                }

                val characteristic = service.getCharacteristic(CHAR_UUID)
                if (characteristic == null) {
                    Log.e("BLE", "Characteristic not found")
                    return
                }

                commCharacteristic = characteristic
                gatt.setCharacteristicNotification(characteristic, true)

                val descriptor = characteristic.getDescriptor(CCCD_UUID)
                descriptor?.let {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(it)
                }

                Log.i("BLE", "Ready to communicate")

                // ✅ ส่งข้อความ smpc_hi เมื่อพร้อมจริง
                sendMessage(context, "smpc_hi")
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                val msg = characteristic.getStringValue(0)
                Log.d("BLE", "Received: $msg")
                onMessageReceived?.invoke(msg)
            }
        })
    }

    fun sendMessage(context: Context, message: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("BLE", "❌ Missing BLUETOOTH_CONNECT permission")
            return
        }

        commCharacteristic?.let {
            it.setValue(message)
            val success = bluetoothGatt?.writeCharacteristic(it) ?: false
            Log.d("BLE", "Sending: $message - success: $success")
        } ?: Log.e("BLE", "❌ Characteristic is null when sending: $message")
    }

    fun setMessageListener(listener: (String) -> Unit) {
        onMessageReceived = listener
    }

    fun disconnect(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) return

        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        commCharacteristic = null
    }
}