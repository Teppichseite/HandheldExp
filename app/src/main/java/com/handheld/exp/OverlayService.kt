package com.handheld.exp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.handheld.exp.models.OverlayState
import com.handheld.exp.modules.ModuleLoader
import com.handheld.exp.receivers.DataReceiver
import com.handheld.exp.utils.DisplayUtils

class OverlayService : Service() {

    private var dataReceiver: DataReceiver? = null

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    private var params: LayoutParams? = null

    private val overlayViewModel = OverlayViewModel()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= 26) {
            startForeground()
        }

        createWindowManager()

        createOverlayView()

        handleOverlayDisplay()

        val moduleLoader = ModuleLoader(this, overlayViewModel, overlayView!!)
        moduleLoader.loadAll()

        createDataReceiver()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForeground() {
        val channelId = "handheld_exp_notification_channel"
        val channel = NotificationChannel(
            channelId, "Overlay notification", NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("HandheldExp")
            .setContentText("HandheldExp is showing the in-game overlay")
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun createWindowManager() {

        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) LayoutParams.TYPE_APPLICATION_OVERLAY
            else LayoutParams.TYPE_PHONE

        params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            type,
            WINDOW_MANAGER_FOCUSED_FLAGS,
            PixelFormat.RGBA_8888
        )

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        params!!.gravity = Gravity.TOP or Gravity.LEFT
        params!!.x = 0
        params!!.y = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params!!.layoutInDisplayCutoutMode =
                LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        params!!.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        DisplayUtils.applyFixedScreenDensity(this, windowManager!!)
    }

    private fun createOverlayView() {
        overlayView = LayoutInflater.from(this)
            .inflate(R.layout.overlay_layout, null)

        overlayView!!.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        overlayView?.visibility = View.GONE
        windowManager?.addView(overlayView, params)
    }

    private fun createDataReceiver() {
        dataReceiver = DataReceiver(this, overlayViewModel)
        registerReceiver(dataReceiver, dataReceiver!!.gameFilter)
        registerReceiver(dataReceiver, dataReceiver!!.overlayFilter)
    }

    private fun handleOverlayDisplay() {
        overlayViewModel.overlayState.observeForever {
            when (it) {
                OverlayState.CLOSED -> overlayView?.visibility = View.GONE
                OverlayState.OPENED -> {
                    overlayView?.visibility = View.VISIBLE
                    setOverlayFlags(WINDOW_MANAGER_FOCUSED_FLAGS)
                }
                OverlayState.OPENED_ONLY_TOUCH -> {
                    overlayView?.visibility = View.VISIBLE
                    setOverlayFlags(WINDOW_MANAGER_ONLY_TOUCH_FLAGS)
                }
                else -> {}
            }
        }
    }

    private fun setOverlayFlags(flags: Int){
        if(params!!.flags == flags){
            return;
        }

        params!!.flags = flags
        windowManager!!.updateViewLayout(overlayView, params)
    }

    companion object {
        private const val WINDOW_MANAGER_FOCUSED_FLAGS = LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                LayoutParams.FLAG_LAYOUT_NO_LIMITS
        private const val WINDOW_MANAGER_ONLY_TOUCH_FLAGS = LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                LayoutParams.FLAG_NOT_FOCUSABLE

    }

}