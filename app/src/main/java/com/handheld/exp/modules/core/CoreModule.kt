package com.handheld.exp.modules.core

import ItemAdapter
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.R
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.modules.Module
import com.handheld.exp.utils.AppUtils

class CoreModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val appUtils = AppUtils(context)

    private val backItem = ButtonItem(
        label = "Back", key = "back", sortKey = "a", path = listOf("none")
    ) {
        overlayViewModel.navigateBack()
    }

    private val closeItem = ButtonItem(
        label = "Resume", key = "close", sortKey = "a",
    ) {
        overlayViewModel.closeOverlay()
    }

    private val exitItem = ButtonItem(
        label = "Exit Game", key = "exit", sortKey = "z",
    ) {
        onExit()
    }

    override fun onLoad() {
        createMenuItemUi()

        overlayViewModel.menuItems.value?.add(backItem)
        overlayViewModel.menuItems.value?.add(closeItem)
        overlayViewModel.menuItems.value?.add(exitItem)

        overlayViewModel.currentGameContext.observeForever {
            if (overlayViewModel.isGameContextActive()) {
                return@observeForever
            }

            closeLatestApp()
        }
    }

    private fun onExit() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        overlayViewModel.closeOverlay()
        context.startActivity(startMain)
        closeLatestApp()
    }

    private fun focusItem(recyclerView: RecyclerView, index: Int){
        val firstChild = recyclerView.getChildAt(index)
        firstChild?.requestFocus()
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
            adapter.setItems(overlayViewModel.getCurrentMenuItems { item, path ->
                return@getCurrentMenuItems item == backItem && path != ""
            })
        }

        focusItem(recyclerView, 0)

        val menuTitle: TextView = overlayView.findViewById(R.id.menuTitle)
        overlayViewModel.pathHistory.observeForever {
            overlayViewModel.notifyMenuItemsChanged()
            if (overlayViewModel.getCurrentNavigationItem() == null) {
                overlayViewModel.menuTitle.value = "Play Menu"
                focusItem(recyclerView, 0)
                return@observeForever
            }

            overlayViewModel.menuTitle.value =
                overlayViewModel.getCurrentNavigationItem()?.label
            focusItem(recyclerView, 1)
        }

        overlayViewModel.menuTitle.observeForever {
            menuTitle.text = overlayViewModel.menuTitle.value
        }
    }

    private fun closeLatestApp() {

        val currentApps = appUtils.getCurrentlyOpenedApps()

        currentApps
            .filter {
                !(it.isEsDe || it.isSelf)
            }
            .forEach {

                if (it.taskId == null) {
                    return@forEach
                }

                appUtils.stopApp(it.packageName)
                appUtils.removeTask(it.taskId)
            }
    }
}