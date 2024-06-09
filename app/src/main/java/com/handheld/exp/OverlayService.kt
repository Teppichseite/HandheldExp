package com.handheld.exp

import ItemAdapter
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.handheld.exp.items.MainItemSetup
import com.handheld.exp.receivers.DataReceiver
import java.io.File


class OverlayService : Service() {

    private var dataReceiver: DataReceiver? = null

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    private var params: LayoutParams? = null;

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

        createDataReceiver()

        createMenuItemUi()

        createGameContextUi()

        handleOverlayDisplay()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY;
    }

    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForeground(){
        val CHANNEL_ID = "channel1"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Overlay notification",
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Title")
            .setContentText("Text")
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    private fun createWindowManager(){
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LayoutParams.TYPE_APPLICATION_OVERLAY
        else LayoutParams.TYPE_PHONE

        params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            type,
            0
                    or LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.RGBA_8888
        )

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        params!!.gravity = Gravity.TOP or Gravity.LEFT
        params!!.x = 0
        params!!.y = 0
    }

    private fun createOverlayView(){
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        overlayView!!.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        windowManager?.addView(overlayView, params)
    }

    private fun createDataReceiver(){
        dataReceiver = DataReceiver(this, overlayViewModel)
        registerReceiver(dataReceiver, dataReceiver!!.gameFilter)
        registerReceiver(dataReceiver, dataReceiver!!.overlayFilter)
    }

    private fun handleOverlayDisplay(){
        overlayViewModel.overlayOpened.observeForever(Observer {
            if (!it){
                overlayView?.visibility = View.GONE;
            }else{
                overlayView?.visibility = View.VISIBLE;
            }
        })
    }

    private fun createMenuItemUi(){
        val deviceItemConfig = MainItemSetup(this, overlayViewModel)
        deviceItemConfig.setupItems()

        val recyclerView: RecyclerView = overlayView!!.findViewById(R.id.itemList)

        val adapter = ItemAdapter(overlayViewModel.menuItems.value!!)
        adapter.setHasStableIds(true)

        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null

        recyclerView.layoutManager = LinearLayoutManager(this)

        overlayViewModel.menuItems.observeForever(Observer {
            adapter.setItems(it)
        })
    }

    private fun createGameContextUi(){

        val gameContextHolder: View = overlayView!!.findViewById(R.id.gameContextHolder)
        val gameContextImage: ImageView = overlayView!!.findViewById(R.id.gameContextImage)
        val gameContextGameName: TextView = overlayView!!.findViewById(R.id.gameContextGameName)
        val gameContextSystemName: TextView = overlayView!!.findViewById(R.id.gameContextSystemName)

        gameContextHolder.visibility = View.GONE

        overlayViewModel.currentGameContext.observeForever(Observer {
            if(it == null){
                gameContextHolder.visibility = View.GONE
            }else{
                gameContextGameName.text = it.game.name
                gameContextSystemName.text = it.system.fullName
                gameContextHolder.visibility = View.VISIBLE

                val mixImage = File(it.gameMedia.mixImagePath!!)
                val coverImage = File(it.gameMedia.coverPath!!)

                if(mixImage.exists()){
                    gameContextImage.setImageURI(Uri.fromFile(mixImage))
                }else if(coverImage.exists()){
                    gameContextImage.setImageURI(Uri.fromFile(coverImage))
                }
            }
        })
    }

}