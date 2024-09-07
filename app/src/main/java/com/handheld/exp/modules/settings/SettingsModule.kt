package com.handheld.exp.modules.settings

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.View
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.Item
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.modules.Module
import com.handheld.exp.utils.AppUtils
import com.handheld.exp.utils.HandlerUtils


class SettingsModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    val appUtils = AppUtils(context)

    private val settings =
        NavigationItem(
            label = "Settings",
            key = "settings",
            sortKey = "l"
        )

    private val openSettingsApp =
        ButtonItem(
            label = "Open Settings",
            key = "open_settings_app",
            sortKey = "l1",
            path = listOf("settings"),
        ) {
            openApp(SETTINGS_PACKAGE_NAME)
        }

    private val allApps =
        NavigationItem(
            label = "All Apps",
            key = "all_apps",
            sortKey = "l3",
            path = listOf("settings"),
        )

    private val reopenCurrentApp =
        ButtonItem(
            label = "Reopen current app",
            key = "reopen_current_app",
            sortKey = "l4",
            path = listOf("settings")
        ){
            reopenCurrentApp()
        }

    override fun onLoad() {
        overlayViewModel.menuItems.value?.add(settings)
        overlayViewModel.menuItems.value?.add(openSettingsApp)
        overlayViewModel.menuItems.value?.add(allApps)
        overlayViewModel.menuItems.value?.add(reopenCurrentApp)

        overlayViewModel.pathHistory.observeForever {
            val currentNavItem = it?.lastOrNull()
                ?.lastOrNull()

            val isAppItemPredicate = { item: Item ->
                item.key.startsWith("all_apps_")
            }

            val contains = overlayViewModel.menuItems.value?.any(isAppItemPredicate)

            if (currentNavItem == allApps) {
                if (contains == false) {
                    val appItems = getAppItems()

                    overlayViewModel.menuItems.value?.addAll(appItems)

                    overlayViewModel.notifyMenuItemsChanged()
                }
            } else {
                if (contains == true) {
                    overlayViewModel.menuItems.value?.removeAll(isAppItemPredicate)
                    overlayViewModel.notifyMenuItemsChanged()
                }
            }
        }
    }

    private fun getAppItems(): List<ButtonItem> {
        val apps = appUtils.getLaunchAbleApps()

        return apps
            .map {
                ButtonItem(
                    label = it.name,
                    key = "all_apps_${it.packageName}",
                    sortKey = "a",
                    path = listOf("settings", "all_apps"),
                ) {
                    openApp(it.packageName)
                }
            }
    }

    private fun reopenCurrentApp(){

        overlayViewModel.closeOverlay()

        HandlerUtils.runDelayed(0) {
            if(appUtils.getCurrentApp().isEsDe){
                return@runDelayed
            }
            appUtils.reopenCurrentApp()
        }
    }

    private fun openApp(packageName: String) {

        overlayViewModel.closeOverlay()

        HandlerUtils.runDelayed(0) {
            appUtils.openApp(packageName)
        }
    }


    companion object {
        private const val SETTINGS_PACKAGE_NAME = "com.android.settings"
    }

}