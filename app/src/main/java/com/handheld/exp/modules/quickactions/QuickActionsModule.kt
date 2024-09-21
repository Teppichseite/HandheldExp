package com.handheld.exp.modules.quickactions

import android.content.Context
import android.provider.Settings
import android.view.View
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.models.Option
import com.handheld.exp.models.OptionItem
import com.handheld.exp.models.OverlayState
import com.handheld.exp.modules.Module
import com.handheld.exp.utils.CommonShellRunner
import kotlin.math.ceil

class QuickActionsModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val quickActions =
        NavigationItem(
            label = "Quick Actions",
            key = "quick_actions",
            sortKey = "k0"
        )

    private val screenBrightnessOptions = (5..100 step 5).toList()
        .map {
            Option("${it}%", it.toString())
        }

    private val screenBrightness =
        OptionItem(
            label = "Screen Brightness",
            key = "screen_brightness",
            path = listOf("quick_actions"),
            sortKey = "a0",
            options = screenBrightnessOptions
        ) {
            onScreenBrightnessChange(it)
        }

    private val airplaneModeOptions = listOf(
        Option("On", "true"),
        Option("Off", "false"),
    )

    private val airplaneMode =
        OptionItem(
            label = "Airplane Mode",
            key = "airplane_mode",
            path = listOf("quick_actions"),
            sortKey = "a1",
            options = airplaneModeOptions
        ) {
            onAirplaneModeChange(it)
        }

    private val wifiModeOptions = listOf(
        Option("On", "true"),
        Option("Off", "false"),
    )

    private val wifiMode =
        OptionItem(
            label = "Wifi",
            key = "wifi_mode",
            path = listOf("quick_actions"),
            sortKey = "a2",
            options = wifiModeOptions
        ) {
            onWifiModeChange(it)
        }

    private val bluetoothOptions = listOf(
        Option("On", "true"),
        Option("Off", "false"),
    )

    private val bluetoothMode =
        OptionItem(
            label = "Bluetooth",
            key = "bluetooth_mode",
            path = listOf("quick_actions"),
            sortKey = "a3",
            options = bluetoothOptions
        ) {
            onBluetoothModeChange(it)
        }

    override fun onLoad() {
        val statusReceiver = StatusReceiver(context, overlayView)
        statusReceiver.register()

        refreshItems()

        overlayViewModel.menuItems.value?.add(quickActions)
        overlayViewModel.menuItems.value?.add(screenBrightness)
        overlayViewModel.menuItems.value?.add(airplaneMode)
        overlayViewModel.menuItems.value?.add(wifiMode)
        overlayViewModel.menuItems.value?.add(bluetoothMode)

        overlayViewModel.overlayState.observeForever {
            if (it != OverlayState.OPENED) {
                return@observeForever
            }

            refreshItems()
            overlayViewModel.notifyMenuItemsChanged()
        }
    }

    private fun refreshItems() {
        screenBrightness.setOption(getScreenBrightness())
        airplaneMode.setOption(getAirplaneMode())
        wifiMode.setOption(getWifiMode())
        bluetoothMode.setOption(getBluetoothMode())

    }

    private fun onScreenBrightnessChange(option: Option) {

        setScreenBrightness(option.key)

        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun setScreenBrightness(level: String) {
        val value = (level.toFloat() / 100) * 255;
        CommonShellRunner.runAdbCommands("settings put system screen_brightness ${value.toInt()}")
    }

    private fun getScreenBrightness(): String {
        val value = Settings.System.getInt(context.contentResolver, "screen_brightness")
            .toString()

        val level = ((value.toFloat() / 255) * 100).toInt()

        return roundByStep(level, 5).toString()
    }

    private fun onAirplaneModeChange(option: Option) {
        setAirplaneMode(option.key)
        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun setAirplaneMode(option: String) {
        val value = if (option == "true") "enable" else "disable"

        CommonShellRunner.runAdbCommands(
            "cmd connectivity airplane-mode $value",
        )
    }

    private fun getAirplaneMode(): String {
        val value = Settings.Global.getInt(context.contentResolver, "airplane_mode_on")

        return if (value == 1) "true" else "false"
    }

    private fun onWifiModeChange(option: Option) {
        setWifiMode(option.key)
        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun setWifiMode(option: String) {
        val value = if (option == "true") "enable" else "disable"

        CommonShellRunner.runAdbCommands(
            "svc wifi $value",
        )
    }

    private fun getWifiMode(): String {
        val value = Settings.Global.getInt(context.contentResolver, "wifi_on")

        return if (value == 1) "true" else "false"
    }

    private fun onBluetoothModeChange(option: Option) {
        setBluetoothMode(option.key)
        overlayViewModel.notifyMenuItemsChanged()
    }

    private fun setBluetoothMode(option: String) {
        val value = if (option == "true") "enable" else "disable"

        CommonShellRunner.runAdbCommands(
            "cmd bluetooth_manager $value",
        )
    }

    private fun getBluetoothMode(): String {
        val value = Settings.Global.getInt(context.contentResolver, "bluetooth_on")

        return if (value == 1) "true" else "false"
    }

    private fun roundByStep(value: Int, step: Int): Int {
        if(value <= step){
            return step;
        }
        return (ceil((value / step).toDouble()) * step).toInt();
    }

}