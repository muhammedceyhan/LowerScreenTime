package com.example.lowerscreentime.domain

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

class LockManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun startLockMode(activity: Activity) {
        // 1. Start Screen Pinning
        try {
            activity.startLockTask()
            
            // 2. Enable Do Not Disturb if permission granted
            if (isDndPermissionGranted()) {
                setDndMode(true)
            } else {
                Toast.makeText(context, "DND Permission not granted", Toast.LENGTH_SHORT).show()
                requestDndPermission(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not start lock mode", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopLockMode(activity: Activity) {
        try {
            activity.stopLockTask()
            if (isDndPermissionGranted()) {
                setDndMode(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isDndPermissionGranted(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun requestDndPermission(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    private fun setDndMode(enable: Boolean) {
        if (enable) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        } else {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}
