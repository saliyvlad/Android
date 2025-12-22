package com.example.android

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LocationActivity : Activity(), LocationListener {

    private lateinit var locationManager: LocationManager

    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvAltitude: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvLocationHistory: TextView
    private lateinit var btnGetLocation: Button
    private lateinit var btnStartUpdates: Button

    private val locationPermissionCode = 100
    private var isTracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        initializeViews()
        setupLocationManager()
        setupListeners()
        checkLocationPermissions()
    }

    private fun initializeViews() {
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvAltitude = findViewById(R.id.tvAltitude)
        tvTime = findViewById(R.id.tvTime)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvStatus = findViewById(R.id.tvStatus)
        tvLocationHistory = findViewById(R.id.tvLocationHistory)
        btnGetLocation = findViewById(R.id.btnGetLocation)
        btnStartUpdates = findViewById(R.id.btnStartUpdates)
    }

    private fun setupLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun setupListeners() {
        btnGetLocation.setOnClickListener {
            getLastLocation()
        }

        btnStartUpdates.setOnClickListener {
            toggleLocationUpdates()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (hasLocationPermissions()) {
            true
        } else {
            requestLocationPermissions()
            false
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            locationPermissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                tvStatus.text = "Разрешения получены"
                getLastLocation()
            } else {
                tvStatus.text = "Разрешения не получены. Функциональность ограничена."
                Toast.makeText(this, "Необходимы разрешения для работы с местоположением", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getLastLocation() {
        if (!hasLocationPermissions()) {
            tvStatus.text = "Нет разрешений для доступа к местоположению"
            return
        }

        tvStatus.text = "Получение местоположения..."

        try {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (location != null) {
                updateLocationUI(location)
                saveLocationToFile(location)
                tvStatus.text = "Местоположение получено успешно"
            } else {
                tvStatus.text = "Не удалось получить местоположение"
                Toast.makeText(this, "Местоположение недоступно", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            tvStatus.text = "Ошибка доступа к местоположению"
            Toast.makeText(this, "Ошибка доступа: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleLocationUpdates() {
        if (!hasLocationPermissions()) {
            tvStatus.text = "Нет разрешений для автообновления"
            return
        }

        if (!isTracking) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (hasLocationPermissions()) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )
                isTracking = true
                btnStartUpdates.text = "Остановить обновление"
                tvStatus.text = "Автообновление запущено (GPS)"
                Toast.makeText(this, "Автообновление местоположения запущено", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                tvStatus.text = "Ошибка запуска обновлений"
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
        isTracking = false
        btnStartUpdates.text = "Автообновление"
        tvStatus.text = "Автообновление остановлено"
        Toast.makeText(this, "Автообновление местоположения остановлено", Toast.LENGTH_SHORT).show()
    }

    override fun onLocationChanged(location: Location) {
        updateLocationUI(location)
        saveLocationToFile(location)
    }

    private fun updateLocationUI(location: Location) {
        tvLatitude.text = "Широта: ${location.latitude}"
        tvLongitude.text = "Долгота: ${location.longitude}"
        tvAltitude.text = "Высота: ${location.altitude} м"
        val accuracy = 1
        tvAccuracy.text = "Точность: ${accuracy} м"
        tvTime.text = "Время: ${location.time}"
    }

    private fun saveLocationToFile(location: Location) {
        try {
            val currentTime = location.time
            val fileName = "location.txt"
            val dir = getExternalFilesDir(null)
            val file = File(dir, fileName)
            file.appendText("""
                =========
                Широта: ${location.latitude}
                Долгота: ${location.longitude}
                Высота: ${location.altitude}
                Точность: ${if (location.hasAccuracy()) location.accuracy else "N/A"}
                Время: $currentTime
                =========
            """.trimIndent())
        } catch (e: Exception) {
            println("Error saving location: ${e.message}")
        }
    }




    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}