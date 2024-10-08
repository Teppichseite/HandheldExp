package com.handheld.exp.modules.content

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.R
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.OverlayState
import com.handheld.exp.modules.Module
import java.io.File

class ContentModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val pdfViewer = PdfViewer(context, overlayView)

    private val showManual = ButtonItem(
        label = "Show Manual",
        key = "show_manual",
        sortKey = "d",
        disabled = true
    ) {
        onShowManual()
    }

    override fun onLoad() {
        createGameContextUi()

        overlayViewModel.menuItems.value?.add(showManual)

        overlayViewModel.overlayState.observeForever {
            if (it == OverlayState.CLOSED || !pdfViewer.isOpened()) {
                return@observeForever
            }

            pdfViewer.focus()
        }

        overlayViewModel.currentGameContext.observeForever {
            if (it == null) {
                showManual.disabled = true
                pdfViewer.close()
                overlayViewModel.notifyMenuItemsChanged()
                return@observeForever
            }

            showManual.disabled = it.gameMedia.manualPath == null
            overlayViewModel.notifyMenuItemsChanged()
        }
    }

    private fun onShowManual() {
        val gameContext = overlayViewModel.currentGameContext.value!!
        val path = gameContext.gameMedia.manualPath!!
        pdfViewer.open(gameContext.game, path)
    }

    private fun createGameContextUi() {

        val gameContextHolder: View = overlayView.findViewById(R.id.gameContextHolder)
        val gameContextImage: ImageView = overlayView.findViewById(R.id.gameContextImage)
        val gameContextGameName: TextView = overlayView.findViewById(R.id.gameContextGameName)
        val gameContextSystemName: TextView = overlayView.findViewById(R.id.gameContextSystemName)

        gameContextHolder.visibility = View.GONE

        overlayViewModel.currentGameContext.observeForever {
            if (it == null) {
                gameContextHolder.visibility = View.GONE
                return@observeForever
            }

            gameContextGameName.text = it.game.name
            gameContextSystemName.text = it.system.fullName
            gameContextHolder.visibility = View.VISIBLE

            val mixImage = it.gameMedia.mixImagePath
            val coverImage = it.gameMedia.coverPath

            if (mixImage != null) {
                gameContextImage.setImageURI(Uri.fromFile(File(mixImage)))
            } else if (coverImage != null) {
                gameContextImage.setImageURI(Uri.fromFile(File(coverImage)))
            } else {
                // TODO: Set proper fallback image
                gameContextImage.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }
}