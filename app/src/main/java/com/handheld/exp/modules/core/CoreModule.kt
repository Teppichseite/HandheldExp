package com.handheld.exp.modules.core

import ItemAdapter
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.R
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.modules.Module
import com.handheld.exp.utils.AppContextResolver
import com.handheld.exp.utils.CommonShellRunner

class CoreModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val shellRunner = CommonShellRunner()
    private val appContextResolver = AppContextResolver(context)

    private val closeItem = ButtonItem(
        label = "Resume", key = "close", sortKey = "a"
    ) {
        overlayViewModel.closeOverlay()
    }

    private val exitItem = ButtonItem(
        label = "Exit Game", key = "exit", sortKey = "z"
    ) {
        onExit()
    }

    private val otherSettings =
        NavigationItem(label = "Other Settings", key = "other_settings", sortKey = "l")

    override fun onLoad() {
        createMenuItemUi()

        overlayViewModel.menuItems.value?.add(closeItem)
        overlayViewModel.menuItems.value?.add(otherSettings)
        overlayViewModel.menuItems.value?.add(exitItem)

        overlayViewModel.currentGameContext.observeForever {
            if (overlayViewModel.isGameContextActive()) {
                return@observeForever
            }

            closeLatestApp()
        }

        handleAppResolving()
    }

    private fun onExit(){
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        overlayViewModel.closeOverlay()
        context.startActivity(startMain)
    }

    private fun createMenuItemUi() {
        val recyclerView: RecyclerView = overlayView.findViewById(R.id.itemList)

        val navigationHandler = object : ItemAdapter.NavigationHandler {
            override fun onNavigateTo(navigationItem: NavigationItem) {
                val path = navigationItem.path + navigationItem.key
                overlayViewModel.navigateTo(path)
            }

            override fun onNavigateBack() {
                overlayViewModel.navigateBack()
            }
        }

        val adapter = ItemAdapter(
            overlayViewModel.getCurrentMenuItems(), navigationHandler
        )
        adapter.setHasStableIds(true)

        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null

        recyclerView.layoutManager = LinearLayoutManager(context)

        overlayViewModel.menuItems.observeForever {
            adapter.setItems(overlayViewModel.getCurrentMenuItems())
        }

        val menuTitle: TextView = overlayView.findViewById(R.id.menuTitle)
        overlayViewModel.pathHistory.observeForever {
            adapter.setItems(overlayViewModel.getCurrentMenuItems())
            if (overlayViewModel.getCurrentNavigationItem() == null) {
                overlayViewModel.menuTitle.value = "Play Menu"
                return@observeForever
            }

            overlayViewModel.menuTitle.value =
                overlayViewModel.getCurrentNavigationItem()?.label
        }

        overlayViewModel.menuTitle.observeForever {
            menuTitle.text = overlayViewModel.menuTitle.value
        }
    }

    private fun closeLatestApp() {

        val dimensions = Resources.getSystem().displayMetrics

        val swipeFrom = Pair(dimensions.widthPixels / 2, dimensions.heightPixels / 2)

        val swipeTo = Pair(swipeFrom.first, dimensions.heightPixels * 0.1)

        val swipe = listOf(swipeFrom.first, swipeFrom.second, swipeTo.first, swipeTo.second)
            .map { it.toInt() }
            .joinToString(" ")

        val closeTap = "${dimensions.widthPixels * 0.95} ${swipeFrom.second}"

        val commands = listOf(
            // Open latest open apps
            "input keyevent KEYCODE_APP_SWITCH",
            "sleep 0.5",
            // Close app on the left side
            "input touchscreen swipe $swipe 50",
            "sleep 0.5",
            // Go back to ES_DE
            "input tap ${closeTap}"
        )

        shellRunner.runCommands(commands)
    }

    private fun handleAppResolving(){
        overlayViewModel.currentGameContext.observeForever{
            overlayViewModel.startAppContext(appContextResolver.resolve())
        }
        overlayViewModel.overlayOpened.observeForever{
            overlayViewModel.startAppContext(appContextResolver.resolve())
        }
    }
}