package com.handheld.exp.modules.devices.rp4pro

import android.content.Context
import android.view.View
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.models.Option
import com.handheld.exp.models.OptionItem
import com.handheld.exp.modules.Module

class Rp4ProModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val shellExecutor = Rp4ProShellRunner()

    private val quickSettings = NavigationItem("Quick Settings", "quick_settings", sortKey = "k0")

    private val performanceModeOptions = listOf(
        Option("Standard", PERFORMANCE_MODE_STANDARD),
        Option("Performance", PERFORMANCE_MODE_PERFORMANCE),
        Option("High Performance", PERFORMANCE_MODE_HIGH_PERFORMANCE)
    )

    private val performanceModeItem = OptionItem(
        label = "Performance Mode",
        key = PERFORMANCE_MODE,
        path = listOf("quick_settings"),
        sortKey = "a",
        options = performanceModeOptions
    ) {
        onPerformanceModeOptionChange(it)
    }

    private val fanModeOptions = listOf(
        Option("Off", FAN_MODE_OFF),
        Option("Quiet", FAN_MODE_QUIET),
        Option("Smart", FAN_MODE_SMART),
        Option("Sport", FAN_MODE_SPORT)
    )

    private val fanModeItem = OptionItem(
        label = "Fan Mode",
        key = FAN_MODE,
        path = listOf("quick_settings"),
        sortKey = "b",
        options = fanModeOptions
    ) {
        onFanModeOptionChange(it)
    }

    private val l2r2ModeOptions = listOf(
        Option("Analog", L2R2_MODE_ANALOG),
        Option("Digital", L2R2_MODE_DIGITAL),
        Option("Both", L2R2_MODE_BOTH)
    )

    private val l2r2ModeItem = OptionItem(
        label = "L2/R2 Mode",
        key = L2R2_MODE,
        path = listOf("quick_settings"),
        sortKey = "c",
        options = l2r2ModeOptions
    ) {
        onl2r2OptionChange(it)
    }

    override fun onLoad() {

        //refreshItems()
        //observeGameContext()

        overlayViewModel.menuItems.value!!.add(quickSettings)
        overlayViewModel.menuItems.value!!.add(performanceModeItem)
        overlayViewModel.menuItems.value!!.add(fanModeItem)
        overlayViewModel.menuItems.value!!.add(l2r2ModeItem)
    }

    private fun refreshItems() {
        performanceModeItem.setOption(
            getDeviceSetting(PERFORMANCE_MODE)
        )

        fanModeItem.setOption(
            getDeviceSetting(FAN_MODE)
        )

        l2r2ModeItem.setOption(
            getDeviceSetting(L2R2_MODE)
        )
    }

    private fun onPerformanceModeOptionChange(option: Option) {
        setDeviceSetting(PERFORMANCE_MODE, option.key)

        if (option.key == PERFORMANCE_MODE_PERFORMANCE || option.key == PERFORMANCE_MODE_HIGH_PERFORMANCE) {
            fanModeItem.options = fanModeOptions.filter { it.key != FAN_MODE_OFF }
        } else {
            fanModeItem.options = fanModeOptions
        }

        fanModeItem.setOption(getDeviceSetting(FAN_MODE))

        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun onFanModeOptionChange(option: Option) {
        setDeviceSetting(FAN_MODE, option.key)
        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun onl2r2OptionChange(option: Option) {
        setDeviceSetting(L2R2_MODE, option.key)
        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun getDeviceSetting(settingKey: String): String {
        return shellExecutor.getIntSystemSetting(settingKey, -1)
            .toString()
    }

    private fun setDeviceSetting(settingKey: String, value: String) {
        shellExecutor.setIntSystemSetting(settingKey, value.toInt())
    }

    private fun onGameContextEnd() {
        performanceModeItem.setOption(
            PERFORMANCE_MODE_STANDARD
        )
        performanceModeItem.notifyOnOptionChange()

        fanModeItem.setOption(FAN_MODE_OFF)
        fanModeItem.notifyOnOptionChange()
    }

    private fun observeGameContext() {
        overlayViewModel.currentGameContext.observeForever {
            if (overlayViewModel.isGameContextActive()) {
                return@observeForever
            }

            onGameContextEnd()
        }
    }

    companion object {
        private const val PERFORMANCE_MODE = "performance_mode"
        private const val FAN_MODE = "fan_mode"
        private const val L2R2_MODE = "trigger_input_mode"

        private const val PERFORMANCE_MODE_STANDARD = "0"
        private const val PERFORMANCE_MODE_PERFORMANCE = "1"
        private const val PERFORMANCE_MODE_HIGH_PERFORMANCE = "2"

        private const val FAN_MODE_OFF = "0"
        private const val FAN_MODE_QUIET = "1"
        private const val FAN_MODE_SMART = "4"
        private const val FAN_MODE_SPORT = "5"

        private const val L2R2_MODE_ANALOG = "0"
        private const val L2R2_MODE_DIGITAL = "1"
        private const val L2R2_MODE_BOTH = "2"
    }

}