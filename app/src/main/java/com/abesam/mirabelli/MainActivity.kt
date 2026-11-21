package com.abesam.mirabelli

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {

    private val overlayRequest = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val message = TextView(this).apply {
            text = "MIRABELLI BY ABESAM\n\nActivate your Golden Orb"
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
        }

        setContentView(message)

        checkOverlayPermission()
    }

    private fun checkOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            startOrb()
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, overlayRequest)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == overlayRequest) {
            if (Settings.canDrawOverlays(this)) {
                startOrb()
            }
        }
    }

    private fun startOrb() {
        val intent = Intent(this, FloatingButtonService::class.java)
        startService(intent)
    }
}
