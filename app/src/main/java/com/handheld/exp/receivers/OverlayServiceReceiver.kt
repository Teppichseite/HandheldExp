package com.handheld.exp.receivers

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.handheld.exp.OverlayService

class OverlayServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != OVERLAY_SERVICE_ACTION) {
            return
        }

        if(isOverlayServiceRunning(context)){
            return
        }

        val newIntent = Intent(context, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(newIntent)
        } else {
            context.startService(newIntent)
        }

        return
    }

    private fun isOverlayServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            return OverlayService::class.java.name.equals(service.service.className)
        }
        return false
    }

    companion object {
        const val OVERLAY_SERVICE_ACTION = "com.handheld.exp.OVERLAY_SERVICE"
    }
}