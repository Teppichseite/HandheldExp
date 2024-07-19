package com.handheld.exp

import ItemAdapter
import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.handheld.exp.models.ButtonItem
import com.handheld.exp.models.Item
import com.handheld.exp.models.NavigationItem
import com.handheld.exp.utils.PreferenceUtils

class MainActivity : ComponentActivity() {

    private var esDeFolderButton: Button? = null
    private var esDeFolderLabel: TextView? = null

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

    private val grantUsageStatsPermissionItem = ButtonItem(
        label = "Grant Usage Stats Permission",
        key = "grant_usage_stats_permission",
        sortKey = "d"
    ) {
        requestUsageStatsPermission()
    }

    private val openOverlayItem = ButtonItem(
        label = "Open Overlay Menu",
        key = "open_overlay",
        sortKey = "e"
    ) {
        onOpenOverlayMenu()
    }

    private val menuItems = listOf<Item>(
        setEsDePathItem,
        setEsDeMediaPathItem,
        grantOverlayPermissionItem,
        grantUsageStatsPermissionItem,
        openOverlayItem
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_layout);
        createUi()
    }

    private fun createUi() {
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

    private fun requestUsageStatsPermission() {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), packageName
        )

        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
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

    private fun handleOverlayPermissionGrant() {
        if (!Settings.canDrawOverlays(this)) {
            return
        }

        startOverlayService()
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

    private fun populateEsDeFolderLabel() {
        val folderUri = preferenceUtils.getPreference("es_de_folder_uri")

        if (folderUri == null) {
            this.esDeFolderLabel!!.setText("ES-DE Path: Not set")
        } else {
            this.esDeFolderLabel!!.setText("ES-DE Path: $folderUri")
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun onOpenOverlayMenu() {
        val startOverlayServiceIntent = Intent("com.handheld.exp.OVERLAY_SERVICE")
        startOverlayServiceIntent.component = ComponentName(
            "com.handheld.exp",
            "com.handheld.exp.receivers.OverlayServiceReceiver"
        )
        sendBroadcast(startOverlayServiceIntent)

        val openMenuIntent = Intent("com.handheld.exp.OVERLAY")
        openMenuIntent.putExtra("command", "open")
        sendBroadcast(openMenuIntent)
    }

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION_CODE = 0
        private const val REQUEST_ES_DE_FOLDER_PERMISSION_CODE = 1
        private const val REQUEST_ES_DE_MEDIA_FOLDER_PERMISSION_CODE = 2
    }
}