package com.handheld.exp.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageEvents.Event
import android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED
import android.app.usage.UsageStatsManager
import android.content.Context
import com.handheld.exp.models.AppContext

class AppContextResolver(private val context: Context) {

    fun resolve() : AppContext? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 30

        val stats = usageStatsManager.queryEvents(
            beginTime, endTime
        )

        val events: MutableList<Event> = mutableListOf()
        while (stats.hasNextEvent()){
            val event = UsageEvents.Event()
            stats.getNextEvent(event)
            events.add(event)
        }
        events.sortByDescending { it.timeStamp }

        val foundEvent = events.find { it.eventType == ACTIVITY_RESUMED } ?: return null

        val packageName = foundEvent.packageName

        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)

        val appName = context.packageManager.getApplicationLabel(appInfo).toString()

        return AppContext(
            appName,
            packageName
        )
    }

}