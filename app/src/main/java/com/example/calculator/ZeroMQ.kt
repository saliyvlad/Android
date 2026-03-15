//package com.example.calculator
//
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.text.method.ScrollingMovementMethod
//import android.util.Log
//import android.widget.Button
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import org.zeromq.SocketType
//import org.zeromq.ZContext
//import org.zeromq.ZMQ
//
//class ZeroMQ : AppCompatActivity() {
//    private var log_tag: String = "MY_LOG_TAG"
//    private lateinit var LogConnection: TextView
//    private lateinit var StartSession: Button
//    private lateinit var handler: Handler
//    private var zmqContext: ZContext? = null
//    private var serverThread: Thread? = null
//
//    private lateinit var ConnectToRemote: Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_zero_mq)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        StartSession = findViewById(R.id.startTrade)
//        LogConnection = findViewById(R.id.LogerTV)
//        ConnectToRemote = findViewById(R.id.startTradeWS)
//        LogConnection.movementMethod = ScrollingMovementMethod()
//        handler = Handler(Looper.getMainLooper())
//        serverThread = Thread { startServer() }
//        serverThread?.start()
//
//        StartSession.setOnClickListener {
//            StartSession.isEnabled = false
//            LogConnection.append("\n--- Starting New Session ---\n")
//            LogConnection.append("\ntcp://127.0.0.1:5555\n")
//            val clientThread = Thread { startClient("tcp://127.0.0.1:5555") }
//            clientThread.start()
//        }
//        ConnectToRemote.setOnClickListener {
//            ConnectToRemote.isEnabled = false
//            LogConnection.append("\n--- Starting New Session ---\n")
////            LogConnection.append("\ntcp://192.168.0.194:5555\n")
//            LogConnection.append("\ntcp://10.95.122.178:5555\n")
////            val clientThread = Thread { startClient("tcp://192.168.0.194:5555") }
//            val clientThread = Thread { startClient("tcp://10.95.122.178:5555") }
//            clientThread.start()
//        }
//    }
//
//    private fun logToUi(message: String) {
//        handler.post {
//            LogConnection.append("$message\n")
//            val scrollAmount = LogConnection.layout.getLineTop(LogConnection.lineCount) - LogConnection.height
//            if (scrollAmount > 0)
//                LogConnection.scrollTo(0, scrollAmount)
//        }
//    }
//
//    fun startServer() {
//        if (zmqContext == null) zmqContext = ZContext()
//        val socket = zmqContext!!.createSocket(SocketType.REP)
//        socket.linger = 0
//
//        try {
//            socket.bind("tcp://127.0.0.1:5555")
//            logToUi("SERVER: Local server ready on port 5555...")
//
//            while (!Thread.currentThread().isInterrupted) {
//                val requestBytes = socket.recv(0) ?: break
//
//                val request = String(requestBytes, ZMQ.CHARSET)
//                logToUi("SERVER: Received -> '$request'")
//                Thread.sleep(200)
//                val response = "Hello from Server!"
//                socket.send(response.toByteArray(ZMQ.CHARSET), 0)
//                logToUi("SERVER: Sent reply")
//            }
//        } catch (e: Exception) {
//            Log.e(log_tag, "Server error", e)
//        } finally {
//            socket.close()
//            logToUi("SERVER: Stopped")
//        }
//    }
//
//    fun startClient(ipAddress: String) {
//        try {
//            ZContext().use { context ->
//                val socket = context.createSocket(SocketType.REQ)
//                socket.linger = 0
//                socket.connect(ipAddress)
//                for (i in 1..10) {
//                    val request = "Hello from Client #$i"
//                    socket.send(request.toByteArray(ZMQ.CHARSET), 0)
//                    val replyBytes = socket.recv(0)
//                    if (replyBytes != null) {
//                        val reply = String(replyBytes, ZMQ.CHARSET)
//                        logToUi("CLIENT: Received -> '$reply'")
//                    }
//                    Thread.sleep(300)
//                }
//                logToUi("--- Session Finished ---")
//            }
//        } catch (e: Exception) {
//            logToUi("CLIENT ERROR: ${e.message}")
//        } finally {
//            handler.post { StartSession.isEnabled = true }
//            handler.post { ConnectToRemote.isEnabled = true }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        zmqContext?.close()
//        zmqContext = null
//        serverThread?.interrupt()
//    }
//}
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

    // Флаг, были ли получены хоть какие-то данные
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

    // IP адреса
    private val LOCAL_HOST = "tcp://127.0.0.1:5555"
    // ЗАМЕНИ ЭТОТ IP на IP твоего ПК (см. инструкцию выше про ipconfig)
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

    /**
     * Запускает поток клиента с автоматическим переподключением
     */
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

                        // Цикл отправки (например, 50 пакетов, потом можно сделать бесконечным)
                        for (i in 1..50) {
                            if (!isRunning) break

                            // Получаем актуальные данные из хранилища
                            val jsonMsg = createJsonMessage()

                            // Если данных нет (0,0), предупреждаем, но все равно отправляем (или можно skip)
                            if (LocationDataStore.latitude == 0.0 && LocationDataStore.longitude == 0.0) {
                                logToUi("Warning: GPS data is still (0,0). Sending anyway...")
                            } else {
                                logToUi("Sending [$i]: $jsonMsg")
                            }

                            // Отправка
                            socket?.send(jsonMsg.toByteArray(Charsets.UTF_8), 0)

                            // Получение ответа с таймаутом (2 секунды)
                            val replyBytes = socket?.recv(2000)

                            if (replyBytes != null) {
                                val reply = String(replyBytes, Charsets.UTF_8)
                                logToUi("Server Reply: $reply")
                            } else {
                                logToUi("Warning: No response from server (timeout)")
                                // Не прерываем цикл, пробуем отправить следующий пакет
                            }

                            Thread.sleep(2000) // Пауза 2 секунды между отправками
                        }
                    }
                } catch (e: Exception) {
                    Log.e(logTag, "Connection error", e)
                    logToUi("ERROR: ${e.message}")
                    logToUi("Attempting to reconnect in 3 seconds...")
                } finally {
                    socket?.close()
                }

                // Логика переподключения: если цикл прервался или завершился, ждем и пробуем снова
                if (isRunning) {
                    try {
                        Thread.sleep(3000)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }

            // Завершение работы
            handler.post {
                logToUi("--- Session Stopped by User ---")
                btnStartLocal.isEnabled = true
                btnStartRemote.isEnabled = true
                isRunning = false
            }
        }
        clientThread?.start()
    }

    /**
     * Формирует JSON строку с текущими координатами из LocationDataStore
     */
    private fun createJsonMessage(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(Date())

        val lat = LocationDataStore.latitude
        val lon = LocationDataStore.longitude
        val alt = LocationDataStore.altitude
        // Используем время из хранилища, если оно есть, иначе текущее
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