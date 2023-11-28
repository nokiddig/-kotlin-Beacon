package com.example.samplebeacon

import android.app.Activity
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.util.Log
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconTransmitter
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region

class BeaconService(val context: Activity) {
    private val beaconParser = BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT)
    private val beaconTransmitter: BeaconTransmitter = BeaconTransmitter(context, beaconParser)
    lateinit var beaconManager: BeaconManager
    private val uuid: String = "2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6"
    private val major:Int = 1
    private val minor:Int = 2
    private val region = Region("myBeaconRegion", Identifier.parse(uuid), null, null)
    private val checkPermission:CheckPermission = CheckPermission(context)

    private val TAG = "Beacon Service"

    fun scanBeacons(onScanResult: (List<Beacon>) -> Unit) {
        beaconManager = BeaconManager.getInstanceForApplication(context)
        beaconManager.beaconParsers.add(beaconParser)

        var beacon:Beacon = Beacon.Builder()
            .setId1(uuid) // UUID của Beacon
            .setId2("1") // Major
            .setId3("2") // Minor
            .setManufacturer(0xFFFF) // Nhà sản xuất (đây là ví dụ với nhà sản xuất AltBeacon)
            .setTxPower(-59) // Công suất phát tín hiệu
            .setDataFields(listOf(0L)) // Dữ liệu tùy chỉnh (nếu cần)
            .build()
        onScanResult(listOf(beacon))
        beaconManager.addRangeNotifier { beacons, _ ->
            beacons.add(beacon)
            onScanResult(beacons.toList())
        }

        beaconManager.startRangingBeaconsInRegion(region)
    }

    fun startAdvertising(beacon: Beacon) {
        val checkPermission = CheckPermission(context = context)
        Log.d(TAG, checkPermission.CheckBluetoothPermission().toString())
        stopAdvertising()
        if (CheckPermission(context = context).CheckBluetoothPermission() == false){
            CheckPermission(context = context).requestPermission(1)
        }
        // Xác thực và phát tín hiệu Beacon
        beaconTransmitter.startAdvertising(beacon, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d(TAG, "phát thành công ${settingsInEffect}")
            }

            override fun onStartFailure(errorCode: Int) {
                Log.d(TAG, "phát thất bại, mã lỗi: ${errorCode}")
            }
        })
    }

    fun stopAdvertising() {
        beaconTransmitter.stopAdvertising()
    }
}