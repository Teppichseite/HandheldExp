package com.handheld.exp.utils

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess

class ShizukuUtils {
    companion object{

        private const val REQUEST_SHIZUKU_PERMISSION_CODE = 0

        fun exec(command: Array<String>): Process{
            // TODO: This might not work in the future anymore
            // but it right now the easiest way to spawn a ADB shell.
            // Should be replaced in the future with a proper solution
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true

            return method.invoke(null, command, null, "/") as ShizukuRemoteProcess
        }

        fun isAvailable(): Boolean{
            return Shizuku.pingBinder() && hasPermission()
        }

        private fun hasPermission(): Boolean{
            if (!isCorrectVersion()){
                return false
            }

            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }

        fun requestPermission(){
            Shizuku.requestPermission(REQUEST_SHIZUKU_PERMISSION_CODE)
        }

        private fun isCorrectVersion(): Boolean{
            return !Shizuku.isPreV11() && Shizuku.getVersion() >= 11
        }
    }

}