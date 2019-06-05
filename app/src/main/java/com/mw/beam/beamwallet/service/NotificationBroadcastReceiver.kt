package com.mw.beam.beamwallet.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mw.beam.beamwallet.screens.AppActivity

class NotificationBroadcastReceiver: BroadcastReceiver() {
    companion object {
        const val ACTION = "com.mw.beam.beamwallet.service.NotificationBroadcastReceiver"
        const val TRANSACTION_ID = "TRANSACTION_ID"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val startIntent = Intent(context, AppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AppActivity.TRANSACTION_ID, intent?.extras?.getString(TRANSACTION_ID))
        }

        context?.startActivity(startIntent)
    }
}