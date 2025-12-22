package com.example.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.*
//import android.content.Intent
//import androidx.annotation.RequiresPermission
//import androidx.annotation.RequiresApi
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.LinearLayout

class TelephonyActivity : Activity() {

    private lateinit var tvNetworkInfo: TextView
    private lateinit var btnFetchData: Button  
    private val REQUEST_PHONE_STATE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val scrollView = ScrollView(this)
        tvNetworkInfo = TextView(this).apply {
            text = "Нажмите кнопку, чтобы получить данные о сети"
            setPadding(16, 16, 16, 16)
            textSize = 14f
        }
        btnFetchData = Button(this).apply {
            text = "Получить данные о сети"
        }

        scrollView.addView(tvNetworkInfo)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(btnFetchData)
            addView(scrollView)
            setPadding(16, 16, 16, 16)
        }

        setContentView(layout)

        btnFetchData.setOnClickListener {
            fetchData()
        }
    }

    private fun fetchData() {
        if (!hasPermissions()) {
            requestPermissions()
        } else {
            loadNetworkInfo()
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_PHONE_STATE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_STATE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                loadNetworkInfo()
            } else {
                tvNetworkInfo.text = "Разрешения не предоставлены. Данные недоступны."
                Toast.makeText(this, "Требуются разрешения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadNetworkInfo() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            tvNetworkInfo.text = "Разрешение на местоположение отсутствует"
            return
        }

        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val allCellInfo = telephonyManager.allCellInfo

        if (allCellInfo.isNullOrEmpty()) {
            tvNetworkInfo.text = "Нет данных о сотовых сетях"
            return
        }

        val sb = StringBuilder()

        for (cellInfo in allCellInfo) {
            sb.append("=== Сеть ===\n")

            when (cellInfo) {
                is CellInfoGsm -> {
                    sb.append("Тип: 2G (GSM)\n")
                    val identity = cellInfo.cellIdentity
                    val signal = cellInfo.cellSignalStrength

                    sb.append("MCC: ${identity.mccString}\n")
                    sb.append("MNC: ${identity.mncString}\n")
                    sb.append("LAC: ${identity.lac}\n")
                    sb.append("CID: ${identity.cid}\n")
                    sb.append("ARFCN: ${identity.arfcn}\n")
                    sb.append("BSIC: ${identity.bsic}\n")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sb.append("RSSI (dBm): ${signal.dbm}\n")
                        sb.append("Timing Advance: ${signal.timingAdvance}\n")
                    } else {
                        sb.append("RSSI/Timing Advance: недоступны\n")
                    }
                }

                is CellInfoLte -> {
                    sb.append("Тип: 4G (LTE)\n")
                    val identity = cellInfo.cellIdentity
                    val signal = cellInfo.cellSignalStrength

                    sb.append("MCC: ${identity.mccString}\n")
                    sb.append("MNC: ${identity.mncString}\n")
                    sb.append("TAC: ${identity.tac}\n")
                    sb.append("PCI: ${identity.pci}\n")
                    sb.append("CI: ${identity.ci}\n")
                    sb.append("EARFCN: ${identity.earfcn}\n")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sb.append("RSRP: ${signal.rsrp} dBm\n")
                        sb.append("RSRQ: ${signal.rsrq} dB\n")
                        sb.append("RSSI: ${signal.rssi} dBm\n")
                        sb.append("RSSNR: ${signal.rssnr}\n")
                        sb.append("CQI: ${signal.cqi}\n")
                        sb.append("ASU: ${signal.asuLevel}\n")
                        sb.append("Timing Advance: ${signal.timingAdvance}\n")
                    } else {
                        sb.append("RSRP/RSRQ/CQI: недоступны\n")
                    }
                }


                else -> {
                    sb.append("Тип: ${cellInfo.javaClass.simpleName}\n")
                }

            }
            sb.append("\n")
        }

        tvNetworkInfo.text = sb.toString()
    }
}