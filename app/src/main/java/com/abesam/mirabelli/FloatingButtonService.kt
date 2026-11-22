package com.abesam.mirabelli

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import kotlin.math.abs

class FloatingButtonService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var orb: GoldenOrbView
    private lateinit var params: WindowManager.LayoutParams

    private val channelId = "mirabelli_service"

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForeground(101, createNotification())

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        orb = GoldenOrbView(this)

        val windowType = if (Build.VERSION.SDK_INT >= 26) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            110,
            110,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 300
        }

        var startX = 0f
        var startY = 0f
        var initialX = 0
        var initialY = 0
        var moved = false

        orb.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    initialX = params.x
                    initialY = params.y
                    moved = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startX
                    val dy = event.rawY - startY

                    if (abs(dx) > 8 || abs(dy) > 8) moved = true

                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    windowManager.updateViewLayout(orb, params)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        Toast.makeText(
                            this,
                            "Yes Boss, nakusikiliza.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }

                else -> true
            }
        }

        windowManager.addView(orb, params)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mirabelli Assistant",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("Mirabelli by ABESAM")
                .setContentText("Golden Orb is active")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Mirabelli by ABESAM")
                .setContentText("Golden Orb is active")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build()
        }
    }

    override fun onDestroy() {
        if (::orb.isInitialized && orb.isAttachedToWindow) {
            windowManager.removeView(orb)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
