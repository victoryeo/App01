package com.example.app01.bluetooth;

interface IBluetoothEventListener {
    fun onDiscovering()
    fun onDiscovered()
    fun onConnecting()
    fun onConnected(isSuccess: Boolean)
    fun onPairing()
    fun onPaired()
    fun onEnable()
}