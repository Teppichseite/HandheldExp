package com.handheld.exp.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.handheld.exp.models.Game
import com.handheld.exp.models.GameContext
import com.handheld.exp.models.GameMedia
import com.handheld.exp.models.System
import java.io.File

class GameContextResolver(private val context: Context) {

    private val preferenceUtils = PreferenceUtils(context)

    fun resolve(
        gameName: String,
        gamePath: String,
        systemName: String,
        systemFullName: String
    ): GameContext {

        val esDeFolderUri = preferenceUtils.getPreference("es_de_folder_uri")

        val esDeFolderPath = "${Environment.getExternalStorageDirectory()}/${
            Uri.parse(esDeFolderUri).path!!.split(":")
                .last()
        }"

        val game = Game(
            name = gameName
        )

        val system = System(
            name = systemName,
            fullName = systemFullName
        )

        val defaultRootMediaPath = "$esDeFolderPath/downloaded_media"

        val gameMedia = GameMedia(
            mixImagePath = getMediaPath(
                defaultRootMediaPath, gamePath, systemName, "miximages", "png"
            ),
            coverPath = getMediaPath(
                defaultRootMediaPath, gamePath, systemName, "covers", "png"
            ),
            manualPath = getMediaPath(
                defaultRootMediaPath, gamePath, systemName, "manuals", "pdf"
            )
        )

        return GameContext(
            esDeFolderPath, game, system, gameMedia
        )
    }

    private fun getMediaPath(
        rootMediaPath: String,
        gamePath: String,
        systemName: String,
        mediaType: String,
        extension: String
    ): String? {
        val gameName = gamePath.split("/")
            .last()
            .split(".")
            .first()
            .replace("\\", "")

        val mediaPath = "$rootMediaPath/$systemName/$mediaType/$gameName.$extension"

        val mediaFile = File(mediaPath)

        if(mediaFile.exists()){
            return mediaPath
        }

        return null
    }


}