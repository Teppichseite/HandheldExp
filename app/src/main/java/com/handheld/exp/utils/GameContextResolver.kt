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

        val esDeFolderPath = getFolderPathFromPreference("es_de_folder_uri")
        val esDeMediaFolderPath = getFolderPathFromPreference("es_de_media_folder_uri")

        val game = Game(
            name = gameName
        )

        val system = System(
            name = systemName,
            fullName = systemFullName
        )

        val gameMedia = GameMedia(
            mixImagePath = getMediaPath(
                esDeMediaFolderPath, gamePath, systemName, "miximages", "png"
            ),
            coverPath = getMediaPath(
                esDeMediaFolderPath, gamePath, systemName, "covers", "png"
            ),
            manualPath = getMediaPath(
                esDeMediaFolderPath, gamePath, systemName, "manuals", "pdf"
            )
        )

        return GameContext(
            esDeFolderPath, game, system, gameMedia
        )
    }

    private fun getFolderPathFromPreference(key: String): String {
        val folderUri = preferenceUtils.getPreference(key)

        return "${Environment.getExternalStorageDirectory()}/${
            Uri.parse(folderUri).path!!.split(":")
                .last()
        }"
    }

    private fun getMediaPath(
        rootMediaPath: String,
        gamePath: String,
        systemName: String,
        mediaType: String,
        extension: String
    ): String? {
        val gameName = File(gamePath).nameWithoutExtension
            .replace("\\", "")

        val mediaPath = "$rootMediaPath/$systemName/$mediaType/$gameName.$extension"

        val mediaFile = File(mediaPath)

        if(mediaFile.exists()){
            return mediaPath
        }

        return null
    }


}