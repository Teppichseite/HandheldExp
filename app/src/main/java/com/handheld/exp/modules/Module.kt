package com.handheld.exp.modules

import android.content.Context
import android.view.View
import com.handheld.exp.OverlayViewModel

abstract class Module(protected val context: Context,
                      protected val overlayViewModel: OverlayViewModel,
                      protected val overlayView: View
) {
    abstract fun onLoad()
}