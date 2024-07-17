package com.handheld.exp

import android.app.AlarmManager
import android.app.PendingIntent
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
import com.handheld.exp.utils.PreferenceUtils

class MainActivity : ComponentActivity() {

    private var esDeFolderButton: Button? = null
    private var esDeFolderLabel: TextView? = null

    private val preferenceUtils = PreferenceUtils(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_layout);

        if (requestDrawOverlayPermission()) {
            //startOverlayService();
        }

        createUi()
    }

    private fun createUi() {
        esDeFolderButton = findViewById(R.id.es_de_folder_button)
        esDeFolderLabel = findViewById(R.id.es_de_folder_label)
        esDeFolderButton!!.setOnClickListener {
            requestEsDeFolderPathAndPermission()
        }

        populateEsDeFolderLabel()
    }

    private fun requestDrawOverlayPermission(): Boolean {

        if (Settings.canDrawOverlays(this)) {
            return true;
        }

        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION_CODE)
        return false
    }

    fun requestEsDeFolderPathAndPermission() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_FOLDER_PERMISSION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_OVERLAY_PERMISSION_CODE && resultCode == RESULT_OK) {
            handleOverlayPermissionGrant()
            return
        }

        if (requestCode == REQUEST_FOLDER_PERMISSION_CODE && resultCode == RESULT_OK) {
            handleFolderPermissionGrant(data)
            return
        }
    }

    private fun handleOverlayPermissionGrant() {
        if (!Settings.canDrawOverlays(this)) {
            return
        }

        startOverlayService()
    }

    private fun handleFolderPermissionGrant(data: Intent?) {
        if (data == null) {
            return
        }

        val uri = data.data ?: return

        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        contentResolver.takePersistableUriPermission(uri, takeFlags)

        preferenceUtils.setPreference("es_de_folder_uri", uri.toString())
        populateEsDeFolderLabel()
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

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION_CODE = 0
        private const val REQUEST_FOLDER_PERMISSION_CODE = 1
    }
}