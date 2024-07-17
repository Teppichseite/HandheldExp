package com.handheld.exp

import androidx.lifecycle.MutableLiveData
import com.handheld.exp.models.Game
import com.handheld.exp.models.GameContext
import com.handheld.exp.models.GameMedia
import com.handheld.exp.models.Item
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.models.System

class OverlayViewModel {
    val overlayOpened = MutableLiveData<Boolean>()
    val overlayFocused = MutableLiveData<Boolean>()

    val menuTitle = MutableLiveData<String>()

    val currentGameContext = MutableLiveData<GameContext?>()

    val menuItems = MutableLiveData<MutableList<Item>>()
    val pathHistory = MutableLiveData<ArrayDeque<List<NavigationItem>>>()
    val focusedItem = MutableLiveData<Item?>()

    init {
        menuItems.value = mutableListOf()
        pathHistory.value = ArrayDeque()
        focusedItem.value = null
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
        val items = path.map { pathPart ->
            menuItems.value!!.find { item -> item.key == pathPart }
        }
            .filterNotNull()
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
        esDeFolderPath: String?,
        gameName: String,
        gamePath: String,
        systemName: String,
        systemFullName: String
    ) {

        val game = Game(
            name = gameName
        )

        val gameMedia = GameMedia(
            mixImagePath = getImagePath(
                esDeFolderPath, gamePath, systemName, "miximages"
            ),
            coverPath = getImagePath(
                esDeFolderPath, gamePath, systemName, "covers"
            ),
            manualPath = getImagePath(
                esDeFolderPath, gamePath, systemName, "manuals"
            )
        )

        val system = System(
            name = systemName, fullName = systemFullName
        )

        currentGameContext.value = GameContext(
            game, system, gameMedia
        )
    }

    private fun getImagePath(
        esDeFolderPath: String?,
        gamePath: String,
        systemName: String,
        imageType: String
    ): String {
        val gameName = gamePath.split("/")
            .last()
            .split(".")
            .first()
            .replace("\\", "")

        val extension = if (imageType == "manuals") "pdf" else "png"

        val imagePath =
            "$esDeFolderPath/downloaded_media/$systemName/$imageType/$gameName.$extension"

        return imagePath
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

}