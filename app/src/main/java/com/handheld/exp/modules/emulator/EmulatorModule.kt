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
import com.handheld.exp.utils.CommonShellRunner


class EmulatorModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val shellRunner = CommonShellRunner()

    private val overlayMenuHolder: View = overlayView.findViewById(R.id.overlayMenuHolder)
    private val gameContextHolder: View = overlayView.findViewById(R.id.gameContextHolder)

    private var isInKeySetup = false
    private var oldMenuHolderPadding = Pair(0, 0)

    private val quickLoad = ButtonItem(
        label = "Quick Load", key = "quick_load", sortKey = "b"
    ) {
        onQuickLoad()
    }

    private val quickSave = ButtonItem(
        label = "Quick Save", key = "quick_save", sortKey = "c"
    ) {
        onQuickSave()
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
        shellRunner.runCommand(QUICK_LOAD_CMD)
    }

    private val setQuickSave = ButtonItem(
        label = "Set Quick Save",
        key = "set_quick_load",
        sortKey = "l3",
        path = listOf("other_settings", "emulator_key_setup"),
        disabled = true
    ) {
        shellRunner.runCommand(QUICK_SAVE_CMD)
    }

    private var toggleKeySetup = ButtonItem(
        label = "Start Load/Save Setup",
        key = "toggle_key_setup",
        sortKey = "l1",
        path = listOf("other_settings", "emulator_key_setup")
    ) {
        onToggleKeySetup()
    }

    override fun onLoad() {
        overlayViewModel.menuItems.value?.add(quickLoad)
        overlayViewModel.menuItems.value?.add(quickSave)
        overlayViewModel.menuItems.value?.add(emulatorKeySetup)
        overlayViewModel.menuItems.value?.add(toggleKeySetup)
        overlayViewModel.menuItems.value?.add(setQuickLoad)
        overlayViewModel.menuItems.value?.add(setQuickSave)
    }

    private fun onQuickLoad() {
        runInputCommandsOnEmulator(listOf(QUICK_LOAD_CMD), "Loading...")
    }

    private fun onQuickSave() {
        runInputCommandsOnEmulator(listOf(QUICK_SAVE_CMD), "Saving...")
    }

    private fun onToggleKeySetup() {
        isInKeySetup = !isInKeySetup

        if(isInKeySetup){
            oldMenuHolderPadding = Pair(overlayMenuHolder.paddingLeft, overlayMenuHolder.paddingRight)
        }

        val newMenuHolderPadding = if(!isInKeySetup) oldMenuHolderPadding
            else Pair((getDisplayWidth() * 0.6f).toInt(), 0)

        overlayMenuHolder.setPadding(
            newMenuHolderPadding.first,
            overlayMenuHolder.paddingTop,
            newMenuHolderPadding.second,
            overlayMenuHolder.paddingBottom
        )
        overlayViewModel.overlayFocused.value = !isInKeySetup

        setQuickLoad.disabled = !isInKeySetup
        setQuickSave.disabled = !isInKeySetup

        toggleKeySetup.label = if(!isInKeySetup) "Start Setup" else "Stop Setup"

        overlayViewModel.currentGameContext.value = overlayViewModel.currentGameContext.value
        gameContextHolder.visibility = if(!isInKeySetup) View.VISIBLE else View.GONE

        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun getDisplayWidth(): Int {
        return Resources.getSystem()
            .displayMetrics.widthPixels
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

    companion object {
        private const val QUICK_LOAD_CMD = "input text b"
        private const val QUICK_SAVE_CMD = "input text n"
    }

}