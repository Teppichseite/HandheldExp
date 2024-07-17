package com.handheld.exp.modules.emulator

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.View
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.R
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.modules.Module
import com.handheld.exp.utils.BasicShellRunner


class EmulatorModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val overlayMenuHolder: View = overlayView.findViewById(R.id.overlayMenuHolder)
    private val gameContextHolder: View = overlayView.findViewById(R.id.gameContextHolder)

    private val shellRunner = BasicShellRunner()

    private var isInKeySetup = false

    private val quickLoad = ButtonItem(
        label = "Quick Load", key = "quick_load", sortKey = "b"
    ) {
        runInputCommandsOnEmulator(listOf("input text b"), "Loading...")
    }

    private val quickSave = ButtonItem(
        label = "Quick Save", key = "quick_save", sortKey = "c"
    ) {
        runInputCommandsOnEmulator(listOf("input text n"), "Saving...")
    }

    private val emulatorKeySetup = NavigationItem(
        label = "Load/Save Setup",
        key = "emulator_key_setup",
        path = listOf("other_settings"),
        sortKey = "l0"
    )

    private val setQuickLoad = ButtonItem(
        label = "Set Quick Load",
        key = "set_quick_load",
        sortKey = "l2",
        path = listOf("other_settings", "emulator_key_setup"),
        disabled = true
    ) {
        shellRunner.runCommand("input text b")
    }

    private val setQuickSave = ButtonItem(
        label = "Set Quick Save",
        key = "set_quick_load",
        sortKey = "l3",
        path = listOf("other_settings", "emulator_key_setup"),
        disabled = true
    ) {
        shellRunner.runCommand("input text n")
    }

    private var startKeySetup: ButtonItem? = null

    private var oldMenuHolderPadding = Pair(0, 0)

    override fun onLoad() {

        startKeySetup = ButtonItem(
            label = "Start Load/Save Setup",
            key = "start_key_setp",
            sortKey = "l1",
            path = listOf("other_settings", "emulator_key_setup")
        ) {
            if (!isInKeySetup) {
                onStartKeySetup()
            } else {
                onEndKeySetup()
            }
        }

        overlayViewModel.menuItems.value?.add(quickLoad)
        overlayViewModel.menuItems.value?.add(quickSave)
        overlayViewModel.menuItems.value?.add(emulatorKeySetup)
        overlayViewModel.menuItems.value?.add(startKeySetup!!)
        overlayViewModel.menuItems.value?.add(setQuickLoad)
        overlayViewModel.menuItems.value?.add(setQuickSave)
    }

    private fun runInputCommandsOnEmulator(commands: List<String>, menuTitle: String) {
        val oldMenuTitle = overlayViewModel.menuTitle.value
        overlayViewModel.overlayFocused.value = false
        overlayViewModel.menuTitle.value = menuTitle

        runHandlerDelayed(250) {
            shellRunner.runCommands(commands)
        }

        runHandlerDelayed(500) {
            overlayViewModel.overlayFocused.value = true
            overlayViewModel.menuTitle.value = oldMenuTitle
        }
    }

    private fun runHandlerDelayed(delay: Long, callback: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(
            {
                callback()
            }, delay
        )
    }

    private fun onStartKeySetup() {
        gameContextHolder.visibility = View.GONE

        oldMenuHolderPadding = Pair(overlayMenuHolder.paddingLeft, overlayMenuHolder.paddingRight)

        overlayMenuHolder.setPadding(
            (getDisplayWidth() * 0.6f).toInt(),
            overlayMenuHolder.paddingTop,
            0,
            overlayMenuHolder.paddingBottom
        )
        overlayViewModel.overlayFocused.value = false

        setQuickLoad.disabled = false
        setQuickSave.disabled = false

        startKeySetup!!.label = "Stop Setup"

        overlayViewModel.notifyMenuItemsChanged()

        isInKeySetup = true
    }

    private fun onEndKeySetup() {
        gameContextHolder.visibility = View.VISIBLE
        overlayMenuHolder.setPadding(
            oldMenuHolderPadding.first,
            overlayMenuHolder.paddingTop,
            oldMenuHolderPadding.second,
            overlayMenuHolder.paddingBottom
        )
        overlayViewModel.overlayFocused.value = true

        setQuickLoad.disabled = true
        setQuickSave.disabled = true

        startKeySetup!!.label = "Start Setup"

        overlayViewModel.currentGameContext.value = overlayViewModel.currentGameContext.value

        overlayViewModel.notifyMenuItemsChanged()

        isInKeySetup = false
    }

    private fun getDisplayWidth(): Int {
        return Resources.getSystem()
            .displayMetrics.widthPixels
    }

}