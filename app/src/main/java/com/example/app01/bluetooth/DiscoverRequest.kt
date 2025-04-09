package com.example.app01.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.*

class DiscoverRequest(private val context : Context, private val eventListener: IBluetoothEventListener) : IBluetoothRequest  {

    private var discoveredDevices:MutableList<BluetoothDevice> = mutableListOf()
    //private val bluetoothManager: BluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val discoverReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context, intent: Intent) {

            Log.d("Receive", "start")

            if (BluetoothDevice.ACTION_FOUND.equals(intent.action)) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    addDiscoveredDevice(device)
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.action)) {
                eventListener.onDiscovered()
            }
        }
    }

    //init {
    //    registerReceiver()
    //}

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun discover() {

        if (bluetoothAdapter.isDiscovering)
            bluetoothAdapter.cancelDiscovery()
        Log.d("Discover", "start")

        // Register the BroadcastReceiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(discoverReceiver, filter)

        bluetoothAdapter.startDiscovery()
        //eventListener.onDiscovering()
    }

    private fun registerReceiver() {
        if (bluetoothAdapter!= null) {
            context.registerReceiver(discoverReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            context.registerReceiver(discoverReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun addDiscoveredDevice(device: BluetoothDevice) {
        if (device.bondState != BluetoothDevice.BOND_BONDED)
            return

        for (localDevice in discoveredDevices) {
            if (localDevice.address.equals(device.address))
                return
        }

        discoveredDevices.add(device)
    }

    override fun cleanup() {
        context.unregisterReceiver(discoverReceiver)
    }
}