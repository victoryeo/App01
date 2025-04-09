package com.example.app01

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    private var listItems: ArrayList<String> = ArrayList()

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    private var adapter: ArrayAdapter<String>? = null

    companion object {
        const val REQUEST_ENABLE_BT = 42
    }

    //private val bluetoothConnectionService : BluetoothConnectionService = BluetoothConnectionService(this)
    private val receiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("app", intent.action.toString())
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    var msg = ""
                    if (deviceName.isNullOrBlank()) {
                        msg = deviceHardwareAddress
                    } else {
                        msg = "$deviceName $deviceHardwareAddress"
                    }
                    Log.d("DISCOVERING-DEVICE", msg)
                    addItems(msg)
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("DISCOVERING-STARTED", "isDiscovering")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("DISCOVERING-FINISHED", "FinishedDiscovering")
                }
            }
        }
    }

    //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
    fun addItems(deviceName: String) {
        //listItems.add(deviceName)
        //adapter!!.notifyDataSetChanged()
        adapter!!.add(deviceName);
    }
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            listItems
        )
        val list: ListView? = findViewById(R.id.btDevices);
        list?.setAdapter(adapter)

        //val bluetoothManager: BluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        //val bluetoothAdapter = bluetoothManager.getAdapter()
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        Log.d("app", bluetoothAdapter.name)
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            // Request user to turn on Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
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
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        val permit = isPermissionsGranted(this)
        Log.d("app", "permit:$permit")
        if (!permit) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION),
                642)
        }
        //bluetoothConnectionService.discoverDevices()
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(receiver, filter)
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        this.registerReceiver(receiver, filter)
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        //bluetoothConnectionService.cleanUp()
        unregisterReceiver(receiver)
    }

    private fun isPermissionsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
}
