package com.example.app01.bluetooth


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.example.app01.bluetooth.IBluetoothEventListener
import java.io.IOException
import java.util.*

private const val BASE_UUID = "00000000-0000-1000-8000-00805F9B34FB"

class ConnectionRequest(private val context : Context, private val eventListener: IBluetoothEventListener) : IBluetoothRequest {
    private var connectionThread : ConnectionThread? = null

    fun connect(device: BluetoothDevice) {
        eventListener.onConnecting()
        connectionThread = ConnectionThread(context, device)
        { isSuccess -> eventListener.onConnected(isSuccess)}
        connectionThread?.start()
    }

    fun stopConnect() {
        if (connectionThread != null)
            connectionThread?.cancel()
    }

    override fun cleanup() {
        stopConnect()
    }


    private class ConnectionThread(
        private val context : Context,
        private val device : BluetoothDevice,
        private val onComplete: (isSuccess : Boolean) -> Unit) : Thread() {

        private var bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        private var bluetoothSocket : BluetoothSocket? = null

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        private fun createSocket() : BluetoothSocket? {
            var socket : BluetoothSocket? = null;
            bluetoothSocket = createSocket();

            try {
                val uuid = if (device.uuids.size > 0)
                    device.uuids[0].uuid
                else UUID.fromString(BASE_UUID);

                socket = device.createRfcommSocketToServiceRecord(uuid)
            }
            catch (e : IOException) {}

            return socket;
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun run() {
            super.run()

            bluetoothAdapter.cancelDiscovery()
            var isSuccess = false

            try {
                if (bluetoothSocket != null) {

                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    bluetoothSocket?.connect()
                    isSuccess = true
                }

            }
            catch (e: Exception) { }

            onComplete(isSuccess)
        }

        fun cancel() {
            if (bluetoothSocket != null)
                bluetoothSocket?.close()
        }
    }
}