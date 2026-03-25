package com.example.calculator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object LocationDataStore {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0
    var timestamp: String = "No time"
    var isDataReady: Boolean = false
}

class ZeroMQ : AppCompatActivity(), LocationListener {
    private val logTag: String = "ZMQ_CLIENT"
    private lateinit var logConnection: TextView
    private lateinit var btnToggleConnection: Button
    private lateinit var locationManager: LocationManager
    private var isGpsActive = false
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var clientThread: Thread? = null
    private var isRunning = false
    private val REMOTE_HOST = "tcp://172.26.237.178:5555"
    private val PERMISSION_REQUEST_ACCESS_LOCATION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_zero_mq)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        btnToggleConnection = findViewById(R.id.startTradeWS)
        logConnection = findViewById(R.id.LogerTV)
        logConnection.movementMethod = ScrollingMovementMethod()

        updateButtonState(false)

        btnToggleConnection.setOnClickListener {
            if (!isRunning) {
                prepareAndStartSession()
            } else {
                stopClientLoop()
            }
        }
    }

    private fun prepareAndStartSession() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                startGpsUpdates()
                startClientLoop(REMOTE_HOST, "Remote PC")
            } else {
                Toast.makeText(this, "Enable GPS in settings", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            requestPermissions()
        }
    }

    private fun startGpsUpdates() {
        if (!isGpsActive) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,
                        1f,
                        this
                    )
                    isGpsActive = true
                    logToUi("GPS Listener started.")
                }
            } catch (e: SecurityException) {
                Log.e(logTag, "GPS permission error", e)
            }
        }
    }

    private fun stopGpsUpdates() {
        if (isGpsActive) {
            locationManager.removeUpdates(this)
            isGpsActive = false
            logToUi("GPS Listener stopped.")
        }
    }

    override fun onLocationChanged(location: Location) {
        LocationDataStore.latitude = location.latitude
        LocationDataStore.longitude = location.longitude
        LocationDataStore.altitude = location.altitude

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        LocationDataStore.timestamp = sdf.format(Date(location.time))
        LocationDataStore.isDataReady = true

        saveLocationToFile(location)
    }

    private fun saveLocationToFile(location: Location) {
        val file = File("/storage/emulated/0/Download/", "locations_zmq.json")
        try {
            if (!file.exists()) file.createNewFile()
            file.appendText("""{"lat":${location.latitude},"lon":${location.longitude},"alt":${location.altitude},"time":${location.time}}""" + "\n")
        } catch (e: Exception) {
            Log.e("LocationData", "Error saving to file", e)
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private fun startClientLoop(address: String, label: String) {
        isRunning = true
        updateButtonState(true)
        logToUi("--- Starting Session ($label) ---")
        logToUi("Target: $address")
        logToUi("Waiting for GPS data...")

        clientThread = Thread {
            while (isRunning) {
                var socket: ZMQ.Socket? = null
                try {
                    ZContext().use { context ->
                        socket = context.createSocket(SocketType.REQ)
                        socket?.linger = 0

                        logToUi("Connecting to server...")
                        socket?.connect(address)
                        logToUi("Connected successfully!")

                        var packetCount = 0
                        while (isRunning) {
                            packetCount++
                            val jsonMsg = createJsonMessage()

                            if (!LocationDataStore.isDataReady) {
                                logToUi("[$packetCount] Warning: GPS not ready yet. Sending test data...")
                            } else {
                                logToUi("[$packetCount] Sending: $jsonMsg")
                            }

                            socket?.send(jsonMsg.toByteArray(Charsets.UTF_8), 0)
                            val replyBytes = socket?.recv(2000)

                            if (replyBytes != null) {
                                val reply = String(replyBytes, Charsets.UTF_8)
                                logToUi("[$packetCount] Server Reply: $reply")
                            } else {
                                logToUi("[$packetCount] Warning: No response (timeout)")
                            }

                            Thread.sleep(1000)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(logTag, "Connection error", e)
                    logToUi("ERROR: ${e.message}")
                    if (isRunning) {
                        logToUi("Attempting to reconnect in 3 seconds...")
                    }
                } finally {
                    socket?.close()
                }

                if (isRunning) {
                    try {
                        Thread.sleep(3000)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }

            handler.post {
                logToUi("--- Session Stopped by User ---")
                stopGpsUpdates()
                updateButtonState(false)
                isRunning = false
            }
        }
        clientThread?.start()
    }

    private fun stopClientLoop() {
        logToUi("Stopping session...")
        isRunning = false
        clientThread?.interrupt()
        clientThread = null
    }

    private fun createJsonMessage(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(Date())

        val lat = LocationDataStore.latitude
        val lon = LocationDataStore.longitude
        val alt = LocationDataStore.altitude
        val ts = if (LocationDataStore.timestamp.isNotEmpty()) LocationDataStore.timestamp else currentTime

        if (!LocationDataStore.isDataReady || (lat == 0.0 && lon == 0.0)) {
            return """{"latitude": 55.751244, "longitude": 37.618423, "altitude": 150.0, "timestamp": "$ts"}"""
        }

        return """{"latitude": $lat, "longitude": $lon, "altitude": $alt, "timestamp": "$ts"}"""
    }

    private fun logToUi(message: String) {
        handler.post {
            logConnection.append("$message\n")
            val scrollAmount = logConnection.layout.getLineTop(logConnection.lineCount) - logConnection.height
            if (scrollAmount > 0)
                logConnection.scrollTo(0, scrollAmount)
        }
    }

    private fun updateButtonState(isActive: Boolean) {
        if (isActive) {
            btnToggleConnection.text = "STOP SESSION"
            btnToggleConnection.setBackgroundColor(Color.parseColor("#D32F2F"))
            btnToggleConnection.setTextColor(Color.WHITE)
        } else {
            btnToggleConnection.text = "START SESSION"
            btnToggleConnection.setBackgroundColor(Color.parseColor("#388E3C"))
            btnToggleConnection.setTextColor(Color.WHITE)
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                prepareAndStartSession()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopClientLoop()
        stopGpsUpdates()
    }
}