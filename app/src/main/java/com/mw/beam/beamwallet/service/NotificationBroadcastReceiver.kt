package com.mw.beam.beamwallet.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.screens.main.MainActivity
import com.mw.beam.beamwallet.screens.welcome_screen.WelcomeActivity

class NotificationBroadcastReceiver: BroadcastReceiver() {
    companion object {
        const val ACTION = "com.mw.beam.beamwallet.service.NotificationBroadcastReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val activityClass = if (App.isAuthenticated) MainActivity::class.java else WelcomeActivity::class.java

        val startIntent = Intent(context, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        context?.startActivity(startIntent)
    }
}