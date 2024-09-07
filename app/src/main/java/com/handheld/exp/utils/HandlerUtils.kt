package com.handheld.exp.utils

import android.os.Handler
import android.os.Looper

class HandlerUtils {
    companion object{
        fun runDelayed(delay: Long, callback: () -> Unit): Handler {
            val handler = Handler(Looper.getMainLooper())

            handler.postDelayed(
                {
                    callback()
                }, delay
            )

            return handler
        }
    }
}