package com.example.takupdf.activities

import android.animation.ValueAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import com.example.takupdf.R
import java.util.Timer
import kotlin.concurrent.timerTask

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val i = Intent(this, LoginActivity::class.java)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val progressAnimator = ValueAnimator.ofInt(0, 10)
        progressAnimator.duration = 1000 // 5 seconds
        progressAnimator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            progressBar.progress = progress
        }

        progressAnimator.start()
        Timer().schedule(timerTask {
            startActivity(i)
            finish()
        }, 1000)

    }
}