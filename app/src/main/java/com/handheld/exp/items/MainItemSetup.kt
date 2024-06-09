package com.handheld.exp.items

import android.content.Context
import android.content.Intent
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.devices.rp4pro.RP4ProItemSetup
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.Item

class MainItemSetup(
    context: Context,
    overlayViewModel: OverlayViewModel) : ItemSetup(context, overlayViewModel) {

    override fun setupItems() {

        val exitItem = ButtonItem("Exit Game", "exit") {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            overlayViewModel.closeOverlay()
            context.startActivity(startMain)
        }

        val closeItem = ButtonItem("Resume", "close") {
            overlayViewModel.closeOverlay()
        }

        overlayViewModel.menuItems.value = listOf(
            closeItem
        )

        handleOtherDeviceItemConfigs()

        overlayViewModel.menuItems.value = overlayViewModel
            .menuItems.value as List<Item> + exitItem as Item
    }

    private fun handleOtherDeviceItemConfigs(){
        val rP4ProDeviceItemConfig = RP4ProItemSetup(context, overlayViewModel)
        rP4ProDeviceItemConfig.setupItems()
    }

}