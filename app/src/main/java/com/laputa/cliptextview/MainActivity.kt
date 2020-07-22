package com.laputa.cliptextview

import android.animation.ObjectAnimator
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var animator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // tv_info.colors = ClipColorTextView.DEFAULT_COLORS
        startAnimation()
    }

    private fun startAnimation() {
        animator?.cancel()
        animator = ObjectAnimator.ofFloat(tv_info, "progress", 0f, 1f).apply {
            startDelay = 1000L
            duration = 2000
            repeatCount = -1
            addUpdateListener {
                val progress = it.animatedValue as Float
                tv_info.progress = progress
            }
        }.also {
            it.start()
        }
    }

    private fun stopAnimation() {
        animator?.cancel()
    }
}