package com.handheld.exp

import ItemAdapter
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.Item
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.receivers.DataReceiver
import com.handheld.exp.receivers.OverlayServiceReceiver
import com.handheld.exp.utils.DisplayUtils
import com.handheld.exp.utils.HandlerUtils
import com.handheld.exp.utils.PreferenceUtils
import com.handheld.exp.utils.ShizukuUtils

class MainActivity : ComponentActivity() {

    private val preferenceUtils = PreferenceUtils(this)

    private val setEsDePathItem = ButtonItem(
        label = "Set ES-DE Location",
        key = "set_es_de_location",
        sortKey = "a"
    ) {
        requestEsDeFolderPathAndPermission()
    }

    private val setEsDeMediaPathItem = ButtonItem(
        label = "Set ES-DE Media Location",
        key = "set_es_de_media_location",
        sortKey = "b"
    ) {
        requestEsDeMediaFolderPathAndPermission()
    }

    private val grantOverlayPermissionItem = ButtonItem(
        label = "Grant Overlay Permission",
        key = "grant_overlay_permission",
        sortKey = "c"
    ) {
        requestDrawOverlayPermission()
    }

    private val grantShizukuPermission = ButtonItem(
        label = "Grant Shizuku Permission",
        key = "grant_shizuku_permission",
        sortKey = "e"
    ) {
        ShizukuUtils.requestPermission()
    }

    private val openOverlayItem = ButtonItem(
        label = "Open Overlay Menu",
        key = "open_overlay",
        sortKey = "f"
    ) {
        onOpenOverlayMenu()
    }

    private val menuItems = listOf<Item>(
        setEsDePathItem,
        setEsDeMediaPathItem,
        grantOverlayPermissionItem,
        grantShizukuPermission,
        openOverlayItem
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.main_activity_layout);
        createUi()
    }

    private fun createUi() {

        DisplayUtils.applyFixedScreenDensity(this, this.windowManager)

        val recyclerView: RecyclerView = findViewById(R.id.mainActivityItemList)

        val navigationHandler = object : ItemAdapter.NavigationHandler {
            override fun onNavigateTo(navigationItem: NavigationItem) {
            }

            override fun onNavigateBack() {
            }
        }

        val adapter = ItemAdapter(
            menuItems, navigationHandler
        )
        adapter.setHasStableIds(true)

        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun requestDrawOverlayPermission(): Boolean {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION_CODE)
        return false
    }

    private fun requestEsDeFolderPathAndPermission() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_ES_DE_FOLDER_PERMISSION_CODE)
    }

    private fun requestEsDeMediaFolderPathAndPermission() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_ES_DE_MEDIA_FOLDER_PERMISSION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ES_DE_FOLDER_PERMISSION_CODE && resultCode == RESULT_OK) {
            handleFolderPermissionGrant(data, "es_de_folder_uri")
            return
        }

        if (requestCode == REQUEST_ES_DE_MEDIA_FOLDER_PERMISSION_CODE && resultCode == RESULT_OK) {
            handleFolderPermissionGrant(data, "es_de_media_folder_uri")
            return
        }
    }

    private fun handleFolderPermissionGrant(data: Intent?, storageKey: String) {
        if (data == null) {
            return
        }

        val uri = data.data ?: return

        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        contentResolver.takePersistableUriPermission(uri, takeFlags)

        preferenceUtils.setPreference(storageKey, uri.toString())
    }

    private fun onOpenOverlayMenu() {
        val startOverlayServiceIntent = Intent(OverlayServiceReceiver.OVERLAY_SERVICE_ACTION)
        startOverlayServiceIntent.component = ComponentName(
            packageName,
            "com.handheld.exp.receivers.OverlayServiceReceiver"
        )
        sendBroadcast(startOverlayServiceIntent)

        HandlerUtils.runDelayed(200){
            val openMenuIntent = Intent(DataReceiver.OVERLAY_ACTION)
            openMenuIntent.putExtra(DataReceiver.COMMAND_EXTRA, "open")
            sendBroadcast(openMenuIntent)
        }
    }

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION_CODE = 0
        private const val REQUEST_ES_DE_FOLDER_PERMISSION_CODE = 1
        private const val REQUEST_ES_DE_MEDIA_FOLDER_PERMISSION_CODE = 2
    }
}