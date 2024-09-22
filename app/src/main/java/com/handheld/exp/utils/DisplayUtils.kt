package com.handheld.exp.utils

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.WindowManager

class DisplayUtils {
    companion object {
        fun applyFixedScreenDensity(context: Context, windowManager: WindowManager) {

            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(metrics)

            val isLandscape = context
                .resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            val screenHeight = if (isLandscape) {
                metrics.heightPixels
            } else {
                metrics.widthPixels
            }

            val configuration = Configuration()
            configuration.setTo(context.resources.configuration)

            val referenceDpi = 240f
            val referenceHeight = 750

            val dp = (referenceDpi * screenHeight) / referenceHeight

            configuration.densityDpi = dp.toInt()
            configuration.orientation = Configuration.ORIENTATION_LANDSCAPE

            context.resources.updateConfiguration(configuration, metrics)
        }
    }
}