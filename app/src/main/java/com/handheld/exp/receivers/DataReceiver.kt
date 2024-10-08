package com.handheld.exp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.handheld.exp.OverlayViewModel
import com.handheld.exp.models.OverlayState
import com.handheld.exp.utils.GameContextResolver
import com.handheld.exp.utils.InfoUtils

class DataReceiver(
    private val context: Context,
    private val overlayViewModel: OverlayViewModel
): BroadcastReceiver() {

    val overlayFilter = IntentFilter(OVERLAY_ACTION)
    val gameFilter = IntentFilter(GAME_ACTION)

    override fun onReceive(context: Context, intent: Intent) {

        if(!InfoUtils.checkIfShizukuIsAvailable(context)){
            return
        }

        if(intent.action == OVERLAY_ACTION){
            handleOverlayAction(context, intent)
            return
        }

        if(intent.action == GAME_ACTION) {
            handleGameAction(context, intent)
            return
        }
    }

    private fun handleOverlayAction(context: Context, intent: Intent){
        val command = intent.getStringExtra(COMMAND_EXTRA)

        if(command == null || command == "toggle"){
            overlayViewModel.toggleOverlay()
            return
        }

        if(command == "open"){
            overlayViewModel.setOverlay(OverlayState.OPENED)
            return
        }

        if(command == "close"){
            overlayViewModel.closeOverlay()
            return
        }
    }

    private fun handleGameAction(context: Context, intent: Intent){
        val command = intent.getStringExtra(COMMAND_EXTRA)

        if(command == "start"){
            val gameName = intent.getStringExtra("game_name")!!
            val gamePath = intent.getStringExtra("game_path")!!
            val systemName = intent.getStringExtra("system_name")!!
            val systemFullName = intent.getStringExtra("system_full_name")!!

            val gameContextResolver = GameContextResolver(context)

            val gameContext = gameContextResolver.resolve(gameName, gamePath, systemName, systemFullName)

            overlayViewModel.startGameContext(gameContext)
            return
        }

        if(command == "end"){
            overlayViewModel.endGameContext()
            return
        }
    }

    companion object {
        const val OVERLAY_ACTION = "com.handheld.exp.OVERLAY"
        const val GAME_ACTION = "com.handheld.exp.GAME"
        const val COMMAND_EXTRA = "command"
    }

}