package com.example.samplebeacon

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CheckPermission(val context: Activity) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    fun checkBluetoothPermission(): Boolean {
        return if (bluetoothAdapter != null && bluetoothAdapter!!.isEnabled) {
            // Kiểm tra xem ứng dụng đã được cấp quyền truy cập Bluetooth hay chưa
            val permission = Manifest.permission.BLUETOOTH
            val result = ContextCompat.checkSelfPermission(context, permission)
            return result == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    fun requestPermission(requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = Manifest.permission.BLUETOOTH
            val permissions = arrayOf(permission)
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, permissions, requestCode)
            }
        }
    }

}