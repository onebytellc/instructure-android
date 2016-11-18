package com.instructure.androidfoosball.views

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.TextView

class GameTimer @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleRes: Int = 0
) : TextView(context, attrs, defStyleRes) {

    private var mStartTime = 0L
    private var mIsAttached = false

    private val mTicker: Runnable = object : Runnable {
        override fun run() {
            onTimeChanged()
            val now = SystemClock.uptimeMillis()
            val nowSystem = System.currentTimeMillis()
            val nextSystem = nowSystem + (1000 - (nowSystem - mStartTime) % 1000)
            val next = now + nextSystem - nowSystem
            handler.postAtTime(this, next)
        }
    }

    fun setStartTime(startTime: Long) {
        mStartTime = startTime
        start()
    }

    private fun onTimeChanged() {
        val time = (System.currentTimeMillis() - mStartTime) / 1000
        text = if (isInEditMode) "1:23" else "%d:%02d".format(time / 60, time % 60)
    }

    private fun start() {
        if (mIsAttached) {
            stop()
            mTicker.run()
        }
    }

    private fun stop() {
        handler.removeCallbacks(mTicker)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mIsAttached = true
        mTicker.run()
    }

    override fun onDetachedFromWindow() {
        stop()
        mIsAttached = false
        super.onDetachedFromWindow()
    }

}
