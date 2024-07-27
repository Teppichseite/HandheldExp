package com.handheld.exp

import androidx.lifecycle.MutableLiveData
import com.handheld.exp.models.AppContext
import com.handheld.exp.models.GameContext
import com.handheld.exp.models.Item
import com.handheld.exp.models.NavigationItem

class OverlayViewModel {

    val overlayOpened = MutableLiveData<Boolean>()
    val overlayFocused = MutableLiveData<Boolean>()

    val menuTitle = MutableLiveData<String>()

    val currentGameContext = MutableLiveData<GameContext?>()

    val menuItems = MutableLiveData<MutableList<Item>>()
    val pathHistory = MutableLiveData<ArrayDeque<List<NavigationItem>>>()

    val currentAppContext = MutableLiveData<AppContext?>()

    init {
        menuItems.value = mutableListOf()
        pathHistory.value = ArrayDeque()
        overlayOpened.value = false

        menuTitle.value = ""
    }

    fun getCurrentMenuItems(): List<Item> {
        val currentPathStr = pathHistory.value?.lastOrNull()
            ?.joinToString(".") { it.key } ?: ""

        return menuItems.value!!
            .filter { !it.disabled }
            .filter {
                val itemPathStr = it.path.joinToString(".")

                itemPathStr == currentPathStr
            }
            .sortedBy { it.sortKey }
    }

    fun getCurrentNavigationItem(): NavigationItem? {
        return pathHistory.value?.lastOrNull()
            ?.lastOrNull()
    }

    fun navigateTo(path: List<String>) {
        val items = path
            .mapNotNull { pathPart ->
                menuItems.value!!.find { item -> item.key == pathPart }
            }
            .map { it as NavigationItem }

        pathHistory.value!!.add(items)
        pathHistory.value = pathHistory.value
    }

    fun navigateBack() {
        val last = pathHistory.value!!.removeLastOrNull()
        if (last == null) {
            closeOverlay()
            return
        }
        pathHistory.value = pathHistory.value
    }

    fun closeOverlay() {
        overlayOpened.value = false
    }

    fun openOverlay() {
        overlayOpened.value = true
    }

    fun toggleOverlay() {
        overlayOpened.value = !(overlayOpened.value!!)
    }

    fun startGameContext(
        gameContext: GameContext
    ) {
        currentGameContext.value = gameContext
    }

    fun endGameContext() {
        currentGameContext.value = null
    }

    fun isGameContextActive(): Boolean {
        return currentGameContext.value != null
    }

    fun notifyMenuItemsChanged() {
        menuItems.value = menuItems.value
    }

    fun startAppContext(appContext: AppContext?) {
        currentAppContext.value = appContext
    }

}