package com.handheld.exp.modules

import android.content.Context
import android.view.View
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.modules.content.ContentModule
import com.handheld.exp.modules.core.CoreModule
import com.handheld.exp.modules.devices.rp4pro.Rp4ProModule
import com.handheld.exp.modules.emulator.EmulatorModule

class ModuleLoader(
    private val context: Context,
    private val viewModel: OverlayViewModel,
    private val overlayView: View
    ) {

    fun loadAll(){
        val coreModule = CoreModule(context, viewModel, overlayView)
        coreModule.onLoad()

        val contentModule = ContentModule(context, viewModel, overlayView)
        contentModule.onLoad()

        val emulatorModule = EmulatorModule(context, viewModel, overlayView)
        emulatorModule.onLoad()

        val rp4ProModule = Rp4ProModule(context, viewModel, overlayView)
        rp4ProModule.onLoad()

        viewModel.notifyMenuItemsChanged()
    }
}