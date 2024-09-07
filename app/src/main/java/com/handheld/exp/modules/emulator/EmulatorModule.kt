package com.handheld.exp.modules.emulator

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.RelativeLayout
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.R
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.models.OverlayState
import com.handheld.exp.models.TextItem
import com.handheld.exp.modules.Module
import com.handheld.exp.utils.CommonShellRunner
import com.handheld.exp.utils.HandlerUtils
import com.handheld.exp.utils.InfoUtils

class EmulatorModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val overlayMenuFrame: View = overlayView.findViewById(R.id.overlayMenuFrame)
    private val gameContextHolder: View = overlayView.findViewById(R.id.gameContextHolder)

    private var isInKeySetup = false
    private var menuFrameAlignment = MenuFrameAlignment.CENTER
    private var oldMenuFrameWidth = 0

    private val quickLoad = ButtonItem(
        label = "Quick Load", key = "quick_load", sortKey = "b",
        disabled = true
    ) {
        onQuickLoad()
    }

    private val quickSave = ButtonItem(
        label = "Quick Save", key = "quick_save", sortKey = "c",
        disabled = true
    ) {
        onQuickSave()
    }

    private var keySetupInfo = TextItem(
        label = "Please check the HandheldExp GitHub page on how to Setup Up Save Sates",
        key = "key_setup_info",
        sortKey = "a",
        path = listOf("settings", "emulator_key_setup"),
    )

    private val emulatorKeySetup = NavigationItem(
        label = "Load/Save Setup",
        key = "emulator_key_setup",
        path = listOf("settings"),
        sortKey = "l0"
    )

    private val setQuickLoad = ButtonItem(
        label = "Set Quick Load",
        key = "set_quick_load",
        sortKey = "l3",
        path = listOf("settings", "emulator_key_setup"),
        disabled = true
    ) {
        HandlerUtils.runDelayed(0) {
            CommonShellRunner.runAdbCommands(QUICK_LOAD_CMD)
        }
    }

    private val setQuickSave = ButtonItem(
        label = "Set Quick Save",
        key = "set_quick_load",
        sortKey = "l4",
        path = listOf("settings", "emulator_key_setup"),
        disabled = true
    ) {
        HandlerUtils.runDelayed(0) {
            CommonShellRunner.runAdbCommands(QUICK_SAVE_CMD)
        }
    }

    private var toggleKeySetup = ButtonItem(
        label = "Start Load/Save Setup",
        key = "toggle_key_setup",
        sortKey = "l1",
        path = listOf("settings", "emulator_key_setup")
    ) {
        onToggleKeySetup()
    }

    private val toggleSetupAlignment = ButtonItem(
        label = "Switch Menu side",
        key = "toggle_setup_alignment",
        sortKey = "l2",
        path = listOf("settings", "emulator_key_setup"),
        disabled = true
    ) {
        when(menuFrameAlignment){
            MenuFrameAlignment.LEFT -> setMenuFrameAlignment(MenuFrameAlignment.RIGHT)
            MenuFrameAlignment.RIGHT -> setMenuFrameAlignment(MenuFrameAlignment.LEFT)
            MenuFrameAlignment.CENTER -> {}
        }
    }

    override fun onLoad() {
        overlayViewModel.menuItems.value?.add(quickLoad)
        overlayViewModel.menuItems.value?.add(quickSave)
        overlayViewModel.menuItems.value?.add(emulatorKeySetup)
        overlayViewModel.menuItems.value?.add(toggleKeySetup)
        overlayViewModel.menuItems.value?.add(toggleSetupAlignment)
        overlayViewModel.menuItems.value?.add(setQuickLoad)
        overlayViewModel.menuItems.value?.add(setQuickSave)
        overlayViewModel.menuItems.value?.add(keySetupInfo)

        overlayViewModel.currentGameContext.observeForever {
            quickLoad.disabled = it == null
            quickSave.disabled = it == null
            overlayViewModel.notifyMenuItemsChanged()
        }
    }

    private fun onQuickLoad() {
        runInputCommandsOnEmulator(arrayOf(QUICK_LOAD_CMD), "Loading...")
    }

    private fun onQuickSave() {
        runInputCommandsOnEmulator(arrayOf(QUICK_SAVE_CMD), "Saving...")
    }

    private fun onToggleKeySetup() {
        isInKeySetup = !isInKeySetup

        if (isInKeySetup) {
            oldMenuFrameWidth = overlayMenuFrame.width
        }

        val newMenuFrameWidth = if (!isInKeySetup) oldMenuFrameWidth
        else (getDisplayWidth() * 0.3f).toInt()

        overlayMenuFrame.layoutParams.width = newMenuFrameWidth

        setMenuFrameAlignment(if (!isInKeySetup) MenuFrameAlignment.CENTER else MenuFrameAlignment.RIGHT)

        overlayViewModel.overlayState.value =
            if (!isInKeySetup) OverlayState.OPENED else OverlayState.OPENED_ONLY_TOUCH

        setQuickLoad.disabled = !isInKeySetup
        setQuickSave.disabled = !isInKeySetup
        toggleSetupAlignment.disabled = !isInKeySetup

        toggleKeySetup.label = if (!isInKeySetup) "Start Load/Save Setup" else "Stop Setup"

        if (overlayViewModel.isGameContextActive()) {
            gameContextHolder.visibility = if (!isInKeySetup) View.VISIBLE else View.GONE
        }

        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun setMenuFrameAlignment(newMenuFrameAlignment: MenuFrameAlignment) {
        val layoutParams = overlayMenuFrame.layoutParams as RelativeLayout.LayoutParams

        layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL)
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT)
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        val verb = when (newMenuFrameAlignment) {
            MenuFrameAlignment.LEFT -> RelativeLayout.ALIGN_PARENT_LEFT
            MenuFrameAlignment.RIGHT -> RelativeLayout.ALIGN_PARENT_RIGHT
            MenuFrameAlignment.CENTER -> RelativeLayout.CENTER_HORIZONTAL
        }
        layoutParams.addRule(verb, RelativeLayout.TRUE)

        overlayMenuFrame.layoutParams = layoutParams

        menuFrameAlignment = newMenuFrameAlignment
    }

    private fun getDisplayWidth(): Int {
        return Resources.getSystem()
            .displayMetrics.widthPixels
    }

    private fun runInputCommandsOnEmulator(commands: Array<String>, menuTitle: String) {
        val oldMenuTitle = overlayViewModel.menuTitle.value
        overlayViewModel.overlayState.value = OverlayState.OPENED_ONLY_TOUCH
        overlayViewModel.menuTitle.value = menuTitle

        HandlerUtils.runDelayed(250) {
            CommonShellRunner.runAdbCommands(*commands)
        }

        HandlerUtils.runDelayed(500) {
            overlayViewModel.overlayState.value = OverlayState.OPENED
            overlayViewModel.menuTitle.value = oldMenuTitle
        }
    }

    companion object {
        private const val QUICK_LOAD_CMD = "input text b"
        private const val QUICK_SAVE_CMD = "input text n"

        enum class MenuFrameAlignment {
            LEFT,
            RIGHT,
            CENTER
        }
    }

}