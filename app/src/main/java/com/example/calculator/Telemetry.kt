package com.example.calculator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.json.JSONObject
import org.zeromq.SocketType
import org.zeromq.ZContext
import java.text.SimpleDateFormat
import java.util.*
import android.telephony.CellIdentityNr
import android.telephony.CellSignalStrengthNr

class Telemetry : Service(), LocationListener {

    private val LOG_TAG = "TelemetryService"
    private val CHANNEL_ID = "TelemetryChannel"
    private val NOTIFICATION_ID = 5001
    
    private val SERVER_URL = "tcp://172.26.237.178:5555"

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var locationManager: LocationManager? = null
    private var telephonyManager: TelephonyManager? = null
    private var lastLocation: Location? = null
    private var zmqJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        Log.d(LOG_TAG, "Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d(LOG_TAG, "Service Started")
        startListeningLocation()
        startTelemetryLoop()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Telemetry Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Telemetry Service")
            .setContentText("Sending CellInfo & Location to server...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun startListeningLocation() {
        try {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {

                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )
                Log.d(LOG_TAG, "GPS Listener started")
            } else {
                Log.e(LOG_TAG, "No GPS Permission in Service")
            }
        } catch (e: SecurityException) {
            Log.e(LOG_TAG, "GPS Error", e)
        }
    }

    private fun startTelemetryLoop() {
        zmqJob = serviceScope.launch {
            ZContext().use { context ->
                val socket = context.createSocket(SocketType.REQ)
                socket.linger = 0

                while (isActive) {
                    try {
                        socket.connect(SERVER_URL)
                        Log.d(LOG_TAG, "Connected to $SERVER_URL")

                        var packetCount = 0
                        while (isActive) {
                            packetCount++
                            val telemetryJson = collectAndBuildJson()

                            socket.send(telemetryJson.toByteArray(Charsets.UTF_8), 0)
                            Log.d(LOG_TAG, "Sent Packet #$packetCount: $telemetryJson")

                            val reply = socket.recvStr(2000)
                            if (reply != null) {
                                Log.d(LOG_TAG, "Server Reply: $reply")
                            } else {
                                Log.w(LOG_TAG, "Server timeout (no reply)")
                            }

                            delay(1000)
                        }
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "ZMQ Connection Error: ${e.message}")
                        try {
                            socket.close()
                        } catch (closeException: Exception) {
                        }

                        if (isActive) {
                            Log.d(LOG_TAG, "Attempting to reconnect in 3 seconds...")
                            try {
                                delay(3000)
                            } catch (e: InterruptedException) {
                                break
                            }
                        } else {
                            break
                        }
                    }
                }
            }
        }
    }

    private fun collectAndBuildJson(): String {
        val json = JSONObject()
        val loc = lastLocation
        val now = Date()

        json.put("latitude", loc?.latitude ?: 0.0)
        json.put("longitude", loc?.longitude ?: 0.0)
        json.put("altitude", loc?.altitude ?: 0.0)
        json.put("accuracy", loc?.accuracy ?: 0f)
        json.put("timestamp", loc?.time ?: now.time)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        json.put("current_time", sdf.format(Date(loc?.time ?: now.time)))

        var cellType = "UNKNOWN"


        var signalDbm = 0
        var asuLevel = 0
        var timingAdvance = -1

        var mcc: String? = null
        var mnc: String? = null
        var cid: String? = null
        var tac: String? = null
        var pci: String? = null
        var band: String? = null
        var arfcn: Int? = null

        var rsrp: Int? = null
        var rsrq: Int? = null
        var rssi: Int? = null
        var rssnr: Int? = null
        var cqi: Int? = null
        var sinr: Int? = null
        var bsic: String? = null

        try {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {

                val cellInfoList = telephonyManager?.allCellInfo
                val activeCell = cellInfoList?.firstOrNull { cell: CellInfo -> cell.isRegistered }

                when (activeCell) {
                    is CellInfoLte -> {
                        cellType = "LTE"
                        val identity = activeCell.cellIdentity
                        val strength = activeCell.cellSignalStrength

                        @Suppress("DEPRECATION")
                        mcc = identity.mccString
                        @Suppress("DEPRECATION")
                        mnc = identity.mncString

                        cid = identity.ci.toString()
                        tac = identity.tac.toString()
                        pci = identity.pci.toString()
                        band = identity.bands.firstOrNull()?.toString()
                        arfcn = identity.earfcn

                        signalDbm = strength.rsrp
                        asuLevel = strength.asuLevel
                        timingAdvance = strength.timingAdvance
                        rsrp = strength.rsrp
                        rsrq = strength.rsrq
                        rssi = strength.rssi
                        rssnr = strength.rssnr
                        cqi = strength.cqi
                    }

                    is CellInfoGsm -> {
                        cellType = "GSM"
                        val identity = activeCell.cellIdentity
                        val strength = activeCell.cellSignalStrength

                        @Suppress("DEPRECATION")
                        mcc = identity.mccString
                        @Suppress("DEPRECATION")
                        mnc = identity.mncString

                        cid = identity.cid.toString()
                        tac = identity.lac.toString()
                        pci = identity.psc.toString()
                        arfcn = identity.arfcn
                        bsic = identity.bsic.toString()

                        signalDbm = strength.dbm
                        asuLevel = strength.asuLevel
                        timingAdvance = strength.timingAdvance
                        rssi = strength.rssi
                    }

                    is CellInfoNr -> {
                        cellType = "NR"
                        val identity = activeCell.cellIdentity as CellIdentityNr
                        val strength = activeCell.cellSignalStrength as CellSignalStrengthNr

                        @Suppress("DEPRECATION")
                        mcc = identity.mccString
                        @Suppress("DEPRECATION")
                        mnc = identity.mncString

                        cid = identity.nci.toString()
                        tac = identity.tac.toString()
                        pci = identity.pci.toString()
                        band = identity.bands.firstOrNull()?.toString()
                        arfcn = identity.nrarfcn

                        signalDbm = strength.ssRsrp
                        asuLevel = strength.asuLevel
                        rsrp = strength.ssRsrp
                        rsrq = strength.ssRsrq
                        sinr = strength.ssSinr
                    }

                    else -> {
                        cellType = "UNKNOWN"
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(LOG_TAG, "Telephony permission error", e)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error reading CellInfo", e)
        }

        json.put("cell_type", cellType)

        json.put("signal_dbm", signalDbm)
        json.put("asu_level", asuLevel)
        if (timingAdvance >= 0) json.put("timing_advance", timingAdvance)

        mcc?.let { json.put("mcc", it) }
        mnc?.let { json.put("mnc", it) }
        cid?.let { json.put("cid", it) }
        tac?.let { json.put("tac", it) }
        pci?.let { json.put("pci", it) }
        band?.let { json.put("band", it) }
        arfcn?.let { json.put("arfcn", it) }

        rsrp?.let { json.put("rsrp", it) }
        rsrq?.let { json.put("rsrq", it) }
        rssi?.let { json.put("rssi", it) }
        rssnr?.let { json.put("rssnr", it) }
        cqi?.let { json.put("cqi", it) }
        sinr?.let { json.put("sinr", it) }
        bsic?.let { json.put("bsic", it) }

        return json.toString()
    }

    override fun onLocationChanged(location: Location) {
        lastLocation = location
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        zmqJob?.cancel()
        locationManager?.removeUpdates(this)
        Log.d(LOG_TAG, "Service Destroyed")
    }
}