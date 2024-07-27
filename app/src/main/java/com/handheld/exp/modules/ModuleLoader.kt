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

    private val modules: MutableList<Module> = mutableListOf()

    fun loadAll(){
        loadModule { CoreModule(context, viewModel, overlayView) }

        loadModule { ContentModule(context, viewModel, overlayView) }

        loadModule { EmulatorModule(context, viewModel, overlayView) }

        loadModule {
            if(!Rp4ProModule.canLoad(context)){
                return@loadModule null
            }
            return@loadModule Rp4ProModule(context, viewModel, overlayView)
        }

        viewModel.notifyMenuItemsChanged()
    }

    private fun loadModule(constructModule: () -> Module?): Module?{
        val module = constructModule() ?: return null

        modules.add(module)
        module.onLoad()
        return module
    }
}