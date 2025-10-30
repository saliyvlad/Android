package com.example.android

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MediaPlayerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "Медиа-плеер (будет реализован позже)"
        textView.textSize = 20f
        textView.gravity = android.view.Gravity.CENTER

        setContentView(textView)
    }
}