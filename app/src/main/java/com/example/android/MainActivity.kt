package com.example.android

import androidx.compose.material3.Text
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.android.ui.theme.AndroidTheme
import android.os.Bundle
import android.widget.Button
import android.app.Activity

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {

        val btnCalculator = findViewById<Button>(R.id.btnGoToCalculator)
        val btnPlayer = findViewById<Button>(R.id.btnGoToPlayer)
        val btnLocation = findViewById<Button>(R.id.btnGoToLocation)
        val btnTelephony = findViewById<Button>(R.id.btnGoToTelephony)


        if (btnCalculator != null) {
            btnCalculator.setOnClickListener {
                val intent = Intent(this, CalculatorActivity::class.java)
                startActivity(intent)
            }
        }

        if (btnPlayer != null) {
            btnPlayer.setOnClickListener {
                val intent = Intent(this, MediaPlayerActivity::class.java)
                startActivity(intent)
            }
        }

        if (btnLocation != null) {
            btnLocation.setOnClickListener {
                val intent = Intent(this, LocationActivity::class.java)
                startActivity(intent)
            }
        }

        if (btnTelephony != null) {
            btnTelephony.setOnClickListener {
                val intent = Intent(this, TelephonyActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidTheme {
        Greeting("Android")
    }
}