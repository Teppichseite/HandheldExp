package com.handheld.exp.modules.content

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.R
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.modules.Module
import java.io.File

class ContentModule(context: Context, overlayViewModel: OverlayViewModel, overlayView: View) :
    Module(context, overlayViewModel, overlayView) {

    private val pdfViewer = PdfViewer(overlayView)

    override fun onLoad() {
        createGameContextUi()

        val showManual = ButtonItem(label = "Show Manual", key = "show_manual", sortKey = "d") {
            val path = overlayViewModel.currentGameContext.value!!.gameMedia.manualPath!!
            pdfViewer.open(overlayViewModel.currentGameContext.value!!.game, path)
        }

        overlayViewModel.menuItems.value?.add(showManual)

        overlayViewModel.overlayOpened.observeForever {
            if(it && pdfViewer.isOpened()){
                pdfViewer.focus()
            }
        }

        overlayViewModel.currentGameContext.observeForever {
            if(it == null){
                pdfViewer.close()
            }
        }
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
            } else {
                gameContextGameName.text = it.game.name
                gameContextSystemName.text = it.system.fullName
                gameContextHolder.visibility = View.VISIBLE

                val mixImage = File(it.gameMedia.mixImagePath!!)
                val coverImage = File(it.gameMedia.coverPath!!)

                if (mixImage.exists()) {
                    gameContextImage.setImageURI(Uri.fromFile(mixImage))
                } else if (coverImage.exists()) {
                    gameContextImage.setImageURI(Uri.fromFile(coverImage))
                }
            }
        }
    }
}