package com.example.calculator

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.text.SimpleDateFormat
import java.util.*

object LocationDataStore {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0
    var timestamp: String = "No time"

    var isDataReady: Boolean = false
}
class ZeroMQ : AppCompatActivity() {
    private val logTag: String = "ZMQ_CLIENT"
    private lateinit var logConnection: TextView
    private lateinit var btnStartLocal: Button
    private lateinit var btnStartRemote: Button

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var clientThread: Thread? = null
    private var isRunning = false

    private val LOCAL_HOST = "tcp://127.0.0.1:5555"
    private val REMOTE_HOST = "tcp://192.168.0.194:5555"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_zero_mq)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnStartLocal = findViewById(R.id.startTrade)
        btnStartRemote = findViewById(R.id.startTradeWS)
        logConnection = findViewById(R.id.LogerTV)
        logConnection.movementMethod = ScrollingMovementMethod()

        btnStartLocal.setOnClickListener {
            if (!isRunning) startClientLoop(LOCAL_HOST, "Localhost")
        }

        btnStartRemote.setOnClickListener {
            if (!isRunning) startClientLoop(REMOTE_HOST, "Remote PC")
        }
    }

    private fun startClientLoop(address: String, label: String) {
        isRunning = true
        btnStartLocal.isEnabled = false
        btnStartRemote.isEnabled = false
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


                        for (i in 1..50) {
                            if (!isRunning) break

                            val jsonMsg = createJsonMessage()

                            if (LocationDataStore.latitude == 0.0 && LocationDataStore.longitude == 0.0) {
                                logToUi("Warning: GPS data is still (0,0). Sending anyway...")
                            } else {
                                logToUi("Sending [$i]: $jsonMsg")
                            }

                            socket?.send(jsonMsg.toByteArray(Charsets.UTF_8), 0)

                            val replyBytes = socket?.recv(2000)

                            if (replyBytes != null) {
                                val reply = String(replyBytes, Charsets.UTF_8)
                                logToUi("Server Reply: $reply")
                            } else {
                                logToUi("Warning: No response from server (timeout)")
                            }

                            Thread.sleep(2000)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(logTag, "Connection error", e)
                    logToUi("ERROR: ${e.message}")
                    logToUi("Attempting to reconnect in 3 seconds...")
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
                btnStartLocal.isEnabled = true
                btnStartRemote.isEnabled = true
                isRunning = false
            }
        }
        clientThread?.start()
    }

    private fun createJsonMessage(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(Date())

        val lat = LocationDataStore.latitude
        val lon = LocationDataStore.longitude
        val alt = LocationDataStore.altitude

        val ts = if (LocationDataStore.timestamp.isNotEmpty()) LocationDataStore.timestamp else currentTime

        return """{"latitude": $lat, "longitude": $lon, "altitude": $alt, "timestamp": "$ts"}"""
    }

    private fun logToUi(message: String) {
        handler.post {
            logConnection.append("$message\n")
            // Автопрокрутка вниз
            val scrollAmount = logConnection.layout.getLineTop(logConnection.lineCount) - logConnection.height
            if (scrollAmount > 0)
                logConnection.scrollTo(0, scrollAmount)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        clientThread?.interrupt()
    }
}