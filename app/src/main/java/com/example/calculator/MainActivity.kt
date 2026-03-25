package com.example.calculator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ZaglushkaButton(
    private val button: Button
) {
    init {
        button.setOnClickListener {
            val context = button.context
            val message = "Функционал ${button.text} временно недоступен :("
            val duration = Toast.LENGTH_SHORT
            Toast.makeText(context, message, duration).show()
        }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var GoToCalcBTN: Button
    private lateinit var GoToPlayerBTN: Button
    private lateinit var GoToLocationBTN: Button
    private lateinit var GoToMobileBTN: Button
    private lateinit var GoToZMQ: Button
    private lateinit var zaglushka: Button

    private lateinit var btnStartTelemetry: Button

    private val PERMISSION_REQUEST_TELEMETRY = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        GoToCalcBTN = findViewById(R.id.GoToCalc)
        GoToCalcBTN.setOnClickListener {
            startActivity(Intent(this, CalculatorActivity::class.java))
        }

        GoToPlayerBTN = findViewById(R.id.GoToPlayer)
        GoToPlayerBTN.setOnClickListener {
            startActivity(Intent(this, MediaPlayer::class.java))
        }

        GoToLocationBTN = findViewById(R.id.GoToGeo)
        GoToLocationBTN.setOnClickListener {
            startActivity(Intent(this, Location::class.java))
        }

        GoToMobileBTN = findViewById(R.id.GoToMobileData)
        GoToMobileBTN.setOnClickListener {
            startActivity(Intent(this, MobileData::class.java))
        }

        GoToZMQ = findViewById(R.id.goToZMQ)
        GoToZMQ.setOnClickListener {
            startActivity(Intent(this, ZeroMQ::class.java))
        }

        zaglushka = findViewById(R.id.zaglushkaBTN)
        ZaglushkaButton(zaglushka)

        btnStartTelemetry = findViewById(R.id.btnStartTelemetry)

        btnStartTelemetry.setOnClickListener {
            checkPermissionsAndStartService()
        }
    }

    private fun checkPermissionsAndStartService() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            startTelemetryService()
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_TELEMETRY
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_TELEMETRY) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startTelemetryService()
            } else {
                Toast.makeText(this, "Разрешения отклонены. Сервис не запущен.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startTelemetryService() {
        val intent = Intent(this, Telemetry::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        Toast.makeText(this, "Telemetry Service запущен! Проверь уведомления.", Toast.LENGTH_LONG).show()
    }
}