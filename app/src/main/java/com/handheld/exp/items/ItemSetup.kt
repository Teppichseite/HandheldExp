package com.handheld.exp.items

import android.content.Context
import com.handheld.exp.OverlayViewModel

abstract class ItemSetup(
    protected val context: Context,
    protected val overlayViewModel: OverlayViewModel){

    abstract fun setupItems()
}