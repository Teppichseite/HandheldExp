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
import com.handheld.exp.utils.CommonShellRunner

class CoreModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val appUtils = AppUtils(context)

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

        overlayViewModel.menuItems.value?.add(closeItem)
        overlayViewModel.menuItems.value?.add(exitItem)

        overlayViewModel.currentGameContext.observeForever {
            if (overlayViewModel.isGameContextActive()) {
                return@observeForever
            }

            closeLatestApp()
        }

        handleAppResolving()
    }

    private fun onExit() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        overlayViewModel.closeOverlay()
        context.startActivity(startMain)
    }

    private fun createMenuItemUi() {
        val recyclerView: RecyclerView = overlayView.findViewById(R.id.itemList)

        recyclerView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            run {
                if (!hasFocus) {
                    recyclerView.requestFocus()
                }
            }
        }

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

        val currentApps = appUtils.getCurrentlyOpenedApps()

        currentApps
            .filter {
                !(it.isEsDe || it.isSelf)
            }
            .forEach {

                if (it.taskId == null) {
                    return@forEach
                }

                appUtils.removeTask(it.taskId)
            }
    }

    private fun handleAppResolving() {
        overlayViewModel.currentGameContext.observeForever {

        }
    }
}