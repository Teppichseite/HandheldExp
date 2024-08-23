package com.handheld.exp.utils

import android.content.Context
import android.widget.Toast


class InfoUtils {

    companion object{
        fun showError(context: Context, message: String){
            Toast.makeText(
                context, message,
                Toast.LENGTH_LONG
            ).show()
        }

        fun checkIfShizukuIsAvailable(context: Context): Boolean{
            if(ShizukuUtils.isAvailable()){
                return true
            }

            showError(context, "Shizuku is not available. Please follow the guide to turn it on.")

            return false
        }
    }
}