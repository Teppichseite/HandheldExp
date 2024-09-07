package com.handheld.exp.modules.quickactions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import com.handheld.exp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class StatusReceiver(
    private val context: Context,
    overlayView: View
): BroadcastReceiver() {

    private val timeFilter = IntentFilter(Intent.ACTION_TIME_TICK)
    private val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

    private val timeInfoView: TextView = overlayView.findViewById(R.id.timeInfo)
    private val batteryInfoView: TextView = overlayView.findViewById(R.id.batteryInfo)

    fun register(){
        context.registerReceiver(this, timeFilter);
        context.registerReceiver(this, batteryFilter);

        updateBatteryInfo()
        updateTimeInfo()
    }

    override fun onReceive(context: Context, intent: Intent) {

        if(intent.action == Intent.ACTION_TIME_TICK){
            updateBatteryInfo()
            return
        }

        if(intent.action == Intent.ACTION_BATTERY_CHANGED) {
            updateTimeInfo()
            return
        }
    }

    private fun updateBatteryInfo(){
        val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        batteryInfoView.text = "$batteryLevel%"
    }

    private fun updateTimeInfo(){
        timeInfoView.text = formatTimeBasedOnSettings(context, Date())
    }

    private fun formatTimeBasedOnSettings(context: Context, date: Date): String {
        val is24HourFormat = DateFormat.is24HourFormat(context)

        val pattern = if (is24HourFormat) "HH:mm" else "hh:mm a"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())

        return simpleDateFormat.format(date)
    }

}