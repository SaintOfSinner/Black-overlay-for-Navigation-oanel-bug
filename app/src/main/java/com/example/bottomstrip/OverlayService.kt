package com.example.bottomstrip

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class OverlayService : Service() {

    private var strip: View? = null
    private lateinit var wm: WindowManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("strip", "Strip Bawah", NotificationManager.IMPORTANCE_MIN)
        )
        val notif = Notification.Builder(this, "strip")
            .setContentTitle("Strip hitam aktif")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            Prefs.height(this),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.OPAQUE
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            y = 0
        }

        val v = strip
        if (v == null) {
            val nv = View(this).apply {
                setBackgroundColor(Color.BLACK)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    systemGestureExclusionRects = listOf()
                }
            }
            wm.addView(nv, params)
            strip = nv
        } else {
            wm.updateViewLayout(v, params)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        strip?.let { wm.removeView(it) }
        strip = null
        super.onDestroy()
    }
}
