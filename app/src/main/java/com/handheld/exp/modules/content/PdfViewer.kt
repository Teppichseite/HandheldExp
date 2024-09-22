package com.handheld.exp.modules.content

import android.content.Context
import android.graphics.PointF
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.handheld.exp.R
import com.handheld.exp.models.Game
import java.io.File


class PdfViewer(val context: Context, overlayView: View) {

    private val pdfView: PDFView = overlayView.findViewById(R.id.pdfView)
    private val pdfViewHolder: View = overlayView.findViewById(R.id.pdfViewHolder)
    private val pdfViewTitle: TextView = overlayView.findViewById(R.id.pdfViewTitle)
    private val overlayMenuFrame: View = overlayView.findViewById(R.id.overlayMenuFrame)

    private var isInZoomState = false

    private var pdfPath: String? = null

    init {
        pdfView.isFocusable = true
        pdfView.isFocusableInTouchMode = true
        pdfView.setOnKeyListener { _, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_DOWN) {
                return@setOnKeyListener handleKeyDown(keyCode)
            }
            false
        }
    }

    fun open(game: Game, pdfPath: String) {
        if (this.pdfPath != pdfPath) {
            val file = File(pdfPath)
            pdfView
                .fromFile(file)
                .pageFitPolicy(FitPolicy.HEIGHT)
                .fitEachPage(true)
                .load()
            this.pdfPath = pdfPath
        }

        pdfViewTitle.text = "${game.name} - Manual"

        pdfViewHolder.visibility = View.VISIBLE
        overlayMenuFrame.visibility = View.INVISIBLE
        focus()
    }

    fun focus() {
        pdfView.requestFocus()
    }

    fun close() {
        pdfViewHolder.visibility = View.GONE
        overlayMenuFrame.visibility = View.VISIBLE
    }

    fun isOpened(): Boolean {
        return pdfViewHolder.visibility == View.VISIBLE
    }

    private fun handleKeyDown(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                close()
                return true
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {

                if (isInZoomState) {
                    pdfView.moveRelativeTo(-100f, 0f)
                    refresh()
                    return true
                }

                pdfView.jumpTo(pdfView.currentPage + 1)
                return true
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {

                if (isInZoomState) {
                    pdfView.moveRelativeTo(+100f, 0f)
                    refresh()
                    return true
                }

                pdfView.jumpTo(pdfView.currentPage - 1)
                return true
            }

            KeyEvent.KEYCODE_DPAD_UP -> {
                pdfView.positionOffset = pdfView.positionOffset - 0.005f
                return true
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                pdfView.positionOffset = pdfView.positionOffset + 0.005f
                return true
            }

            KeyEvent.KEYCODE_BUTTON_R1 -> {

                val newZoom = pdfView.zoom + 0.5f

                if (newZoom > 1.1f) {
                    isInZoomState = true
                }

                if (newZoom > 4f) {
                    return true
                }

                pdfView.zoomCenteredTo(newZoom, getScreenCenter())
                refresh()
                return true
            }

            KeyEvent.KEYCODE_BUTTON_L1 -> {

                val newZoom = pdfView.zoom - 0.5f

                if (newZoom <= 1.05f) {
                    isInZoomState = false
                }

                if (newZoom < 0.95f) {
                    return true
                }

                pdfView.zoomCenteredTo(newZoom, getScreenCenter())
                refresh()
                return true
            }
        }
        return false
    }

    private fun getScreenCenter(): PointF
    {
        val metrics = context.resources.displayMetrics;
        return PointF((metrics.widthPixels/2).toFloat(), (metrics.heightPixels/2).toFloat());
    }

    private fun refresh(){
        pdfView.positionOffset = pdfView.positionOffset
    }
}