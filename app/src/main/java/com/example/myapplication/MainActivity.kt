package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.mammet.downloadstateview.DownloadStateView

class MainActivity : AppCompatActivity() {
    var i = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pauseView: DownloadStateView = findViewById(R.id.download_state)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                // set the limitations for the numeric
                // text under the progress bar
                if (i <= 100) {
                    pauseView.text  = "$i%"
                    pauseView.progress = i
                    i++
                    handler.postDelayed(this, 200)
                } else {
                    handler.removeCallbacks(this)
                }
            }
        }, 200)
        pauseView.setOnClickListener {
            if (pauseView.state == 5)
                pauseView.state = 0
            else
                pauseView.state++
        }
    }
}