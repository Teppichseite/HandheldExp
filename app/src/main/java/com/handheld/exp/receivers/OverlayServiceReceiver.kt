package com.handheld.exp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.handheld.exp.OverlayService


class OverlayServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == OVERLAY_SERVICE_ACTION){
            val newIntent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(newIntent)
            } else {
                context.startService(newIntent)
            }
            return
        }
    }

    companion object {
        private const val OVERLAY_SERVICE_ACTION = "com.handheld.exp.OVERLAY_SERVICE"
    }
}