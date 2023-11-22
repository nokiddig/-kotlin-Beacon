package com.example.samplebeacon

import android.app.*
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import org.altbeacon.beacon.*

class BeaconReferenceApplication: Application() {
    // the region definition is a wildcard that matches all beacons regardless of identifiers.
    // if you only want to detect beacons with a specific UUID, change the id1 parameter to
    // a UUID like Identifier.parse("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6")
    val uuid = Identifier.parse("2FF34454-CF6D-4A0F-ADF2-F4911BA9FFA6")
    var region = Region("all-beacons", uuid, null, null)
    lateinit var beaconTransmitter:BeaconTransmitter
    lateinit var parser:BeaconParser
    override fun onCreate() {
        super.onCreate()

        val beaconManager = BeaconManager.getInstanceForApplication(this)
        BeaconManager.setDebug(true)

        //beaconManager.getBeaconParsers().clear();
        //beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:0-1=4c00,i:2-24v,p:24-24"));

        beaconManager.getBeaconParsers().clear()
        //BeaconManager.setDistanceModelUpdateUrl("")
        parser = BeaconParser().
        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        parser.setHardwareAssistManufacturerCodes(arrayOf(0x004c).toIntArray())
        beaconManager.getBeaconParsers().add(parser)

        // BluetoothMedic.getInstance().legacyEnablePowerCycleOnFailures(this) // Android 4-12 only
        // BluetoothMedic.getInstance().enablePeriodicTests(this, BluetoothMedic.SCAN_TEST + BluetoothMedic.TRANSMIT_TEST)
        // Tạo một transmitter

        startTransmitter()

        setupBeaconScanning()
    }

    public fun startTransmitter() {
        beaconTransmitter = BeaconTransmitter(this, parser)
        val beacon = Beacon.Builder()
            .setId1(uuid.toString()) // UUID
            .setId2("1") // major
            .setId3("2") // minor
            .build()
        beaconTransmitter.startAdvertising()
    }

    public fun stopTransmitter() {
        beaconTransmitter.stopAdvertising()
    }


    fun setupBeaconScanning() {
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        try {
            setupForegroundService()
        }
        catch (e: SecurityException) {
            // On Android TIRAMUSU + this security exception will happen
            // if location permission has not been granted when we start
            // a foreground service.  In this case, wait to set this up
            // until after that permission is granted
            Log.d(TAG, "Not setting up foreground service scanning until location permission granted by user")
            return
        }
        //bat tat quet dinh ki
        beaconManager.setEnableScheduledScanJobs(true)
        //lap lai viec quyet dinh ki
        beaconManager.setBackgroundBetweenScanPeriod(1000)
        //quet tong cong 1,1 s
        //beaconManager.setBackgroundScanPeriod(1100)

        // Ranging callbacks will drop out if no beacons are detected
        // Monitoring callbacks will be delayed by up to 25 minutes on region exit
        // beaconManager.setIntentScanningStrategyEnabled(true)

        // The code below will start "monitoring" for beacons matching the region definition at the top of this file
        // theo doi vung va do khoảng cách
        beaconManager.startMonitoring(region)
        beaconManager.startRangingBeacons(region)
        // These two lines set up a Live Data observer so this Activity can get beacon data from the Application class
        val regionViewModel = BeaconManager.getInstanceForApplication(this).getRegionViewModel(region)
        // trang thai vung: vd thiet bị đi vào ra vùng
        regionViewModel.regionState.observeForever( centralMonitoringObserver)
        //theo doi danh sach cac beacon duoc do khonag cach trong vùng đó
        regionViewModel.rangedBeacons.observeForever( centralRangingObserver)

    }

    fun setupForegroundService() {
        val builder = Notification.Builder(this, "BeaconReferenceApp")
        builder.setSmallIcon(R.drawable.ic_launcher_background)
        builder.setContentTitle("Scanning for Beacons")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)
        val channel =  NotificationChannel("beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setDescription("My Notification Channel Description")
        val notificationManager =  getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        builder.setChannelId(channel.getId())
        Log.d(TAG, "Calling enableForegroundServiceScanning")
        BeaconManager.getInstanceForApplication(this).enableForegroundServiceScanning(builder.build(), 456)
        Log.d(TAG, "Back from  enableForegroundServiceScanning")
    }

    val centralMonitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.OUTSIDE) {
            Log.d(TAG, "outside beacon region: "+region)
        }
        else {
            Log.d(TAG, "inside beacon region: "+region)
            sendNotification()
        }
    }

    val centralRangingObserver = Observer<Collection<Beacon>> { beacons ->
        val rangeAgeMillis = System.currentTimeMillis() - (beacons.firstOrNull()?.lastCycleDetectionTimestamp ?: 0)
        if (rangeAgeMillis < 10000) {
            Log.d("my-log", "Ranged: ${beacons.count()} beacons")
            for (beacon: Beacon in beacons) {
                Log.d(TAG, "$beacon about ${beacon.distance} meters away")
            }
        }
        else {
            Log.d("my-log", "Ignoring stale ranged beacons from $rangeAgeMillis millis ago")
        }
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(this, "beacon-ref-notification-id")
            .setContentTitle("Beacon Reference Application")
            .setContentText("A beacon is nearby.")
            .setSmallIcon(R.drawable.ic_launcher_background)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(resultPendingIntent)
        val channel =  NotificationChannel("beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setDescription("My Notification Channel Description")
        val notificationManager =  getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        builder.setChannelId(channel.getId())
        notificationManager.notify(1, builder.build())
    }

    companion object {
        val TAG = "BeaconReference"
    }

}