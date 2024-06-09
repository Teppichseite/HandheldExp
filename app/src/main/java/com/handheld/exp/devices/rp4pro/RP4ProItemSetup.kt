package com.handheld.exp.devices.rp4pro

import android.content.Context
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.items.ItemSetup
import com.handheld.exp.models.Item
import com.handheld.exp.models.Option
import com.handheld.exp.models.OptionItem

class RP4ProItemSetup(
    context: Context,
    overlayViewModel: OverlayViewModel) : ItemSetup(context, overlayViewModel) {

    private val performanceModeOptions = listOf(
        Option("Standard", PERFORMANCE_MODE_STANDARD),
        Option("Performance", PERFORMANCE_MODE_PERFORMANCE),
        Option("High Performance", PERFORMANCE_MODE_HIGH_PERFORMANCE)
    )

    private val performanceModeItem = OptionItem(
        "Performance Mode",
        PERFORMANCE_MODE,
        performanceModeOptions){
        onPerformanceModeOptionChange(it)
    }

    private val fanModeOptions = listOf(
        Option("Off", FAN_MODE_OFF),
        Option("Quiet", FAN_MODE_QUIET),
        Option("Smart", FAN_MODE_SMART),
        Option("Sport", FAN_MODE_SPORT)
    )

    private val fanModeItem = OptionItem(
        "Fan Mode",
        FAN_MODE,
        fanModeOptions){
        onFanModeOptionChange(it)
    }

    private val l2r2ModeOptions = listOf(
        Option("Analog", L2R2_MODE_ANALOG),
        Option("Digital", L2R2_MODE_DIGITAL),
        Option("Both", L2R2_MODE_BOTH)
    )

    private val l2r2ModeItem = OptionItem(
        "L2/R2 Mode",
        L2R2_MODE,
        l2r2ModeOptions){
        onl2r2OptionChange(it)
    }

    private val shellExecutor = ShellExecutor()

    override fun setupItems() {
        val items: List<Item> = listOf(
            performanceModeItem,
            fanModeItem,
            l2r2ModeItem
        )

        refreshItems()

        observeGameContext()

        overlayViewModel.menuItems.value = overlayViewModel.menuItems.value!! + items
    }

    private fun refreshItems(){
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

    private fun onPerformanceModeOptionChange(option: Option){
        setDeviceSetting(PERFORMANCE_MODE, option.key)

        if(option.key == PERFORMANCE_MODE_PERFORMANCE || option.key == PERFORMANCE_MODE_HIGH_PERFORMANCE) {
            fanModeItem.options = fanModeOptions.filter { it.key != FAN_MODE_OFF }
        }else{
            fanModeItem.options = fanModeOptions
        }

        fanModeItem.setOption(getDeviceSetting(FAN_MODE))

        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun onFanModeOptionChange(option: Option){
        setDeviceSetting(FAN_MODE, option.key)
        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun onl2r2OptionChange(option: Option){
        setDeviceSetting(L2R2_MODE, option.key)
        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun getDeviceSetting(settingKey: String): String {
        return shellExecutor.getIntSystemSetting(settingKey, -1).toString()
    }

    private fun setDeviceSetting(settingKey: String, value: String){
        shellExecutor.setIntSystemSetting(settingKey, value.toInt())
    }

    private fun onGameContextEnd(){
        performanceModeItem.setOption(
            PERFORMANCE_MODE_STANDARD
        )
        performanceModeItem.notifyOnOptionChange()

        fanModeItem.setOption(FAN_MODE_OFF)
        fanModeItem.notifyOnOptionChange()

        shellExecutor.executeAsRoot(getCloseLatestAppScript())
    }

    private fun observeGameContext(){
        overlayViewModel.currentGameContext.observeForever{
            if(!overlayViewModel.isGameContextActive()){
                onGameContextEnd()
            }
        }
    }

    private fun getCloseLatestAppScript(): String{
        return listOf(
            // Open latest open apps
            "input keyevent KEYCODE_APP_SWITCH",
            "sleep 0.5",
            // Close app on the left side
            "input touchscreen swipe 150 380 150 50 50",
            "sleep 0.5",
            // Go back to ES-DE
            "input tap 1200 350",
            ""
        ).joinToString(" /\n")
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