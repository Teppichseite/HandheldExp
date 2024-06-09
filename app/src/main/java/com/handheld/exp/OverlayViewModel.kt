package com.handheld.exp
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import com.handheld.exp.models.Game
import com.handheld.exp.models.GameContext
import com.handheld.exp.models.GameMedia
import com.handheld.exp.models.Item
import com.handheld.exp.models.System

class OverlayViewModel {
    val overlayOpened = MutableLiveData<Boolean>()

    val currentGameContext = MutableLiveData<GameContext?>()

    val menuItems = MutableLiveData<List<Item>>()

    init {
        menuItems.value = listOf()
        overlayOpened.value = false
    }

    fun closeOverlay(){
        overlayOpened.value = false
    }

    fun openOverlay(){
        if(!isGameContextActive()){
            return
        }
        overlayOpened.value = true
    }

    fun toggleOverlay(){
        if(!isGameContextActive()){
            return
        }
        overlayOpened.value = !(overlayOpened.value!!)
    }

    fun startGameContext(
        esDeFolderPath: String?,
        gameName: String,
        gamePath: String,
        systemName: String,
        systemFullName: String){

        val game = Game(
            name = gameName
        )

        val gameMedia = GameMedia(
            mixImagePath = getImagePath(esDeFolderPath, gamePath, systemName, "miximages"),
            coverPath = getImagePath(esDeFolderPath, gamePath, systemName, "covers")
        )

        val system = System(
            name = systemName,
            fullName = systemFullName
        )

        currentGameContext.value = GameContext(
            game,
            system,
            gameMedia
        )
    }

    private fun getImagePath(esDeFolderPath: String?, gamePath: String, systemName: String, imageType: String): String{
        val gameName = gamePath.split("/")
            .last()
            .split(".")
            .first()
            .replace("\\", "")

        val imagePath = "$esDeFolderPath/downloaded_media/$systemName/$imageType/$gameName.png"

        return imagePath
    }

    fun endGameContext(){
        currentGameContext.value = null
    }

    fun isGameContextActive() : Boolean{
        return currentGameContext.value != null;
    }

    fun notifyMenuItemsChanged(){
        menuItems.value = menuItems.value
    }

}