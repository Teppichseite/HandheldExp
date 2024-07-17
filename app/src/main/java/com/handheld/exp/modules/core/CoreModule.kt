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

class CoreModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    override fun onLoad() {

        createMenuItemUi()

        val closeItem =
            ButtonItem(label = "Resume", key = "close", sortKey = "a") {
                overlayViewModel.closeOverlay()
            }

        val exitItem =
            ButtonItem(label = "Exit Game", key = "exit", sortKey = "z") {
                val startMain = Intent(Intent.ACTION_MAIN)
                startMain.addCategory(Intent.CATEGORY_HOME)
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                overlayViewModel.closeOverlay()
                context.startActivity(startMain)
            }

        val otherSettings =
            NavigationItem(label = "Other Settings", key = "other_settings", sortKey = "l")

        overlayViewModel.menuItems.value?.add(closeItem)
        overlayViewModel.menuItems.value?.add(otherSettings)
        overlayViewModel.menuItems.value?.add(exitItem)
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
            } else {
                overlayViewModel.menuTitle.value =
                    overlayViewModel.getCurrentNavigationItem()?.label
            }
        }

        overlayViewModel.menuTitle.observeForever {
            menuTitle.text = overlayViewModel.menuTitle.value
        }
    }
}