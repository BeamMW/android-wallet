package com.mw.beam.beamwallet.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.ChangeAction
import com.mw.beam.beamwallet.core.helpers.Status
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.screens.welcome_screen.WelcomeActivity
import io.reactivex.disposables.Disposable

class BackgroundService : JobService() {
    private val CHANNEL_ID = "com.mw.beam.beamwallet.service.BackgroundService"
    private val REQUEST_CODE = 86131

    private val repository = BackgroundServiceRepository()
    private var txDisposable: Disposable? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        val password = repository.getPassword()

        if (repository.isWalletRunning() || password.isNullOrBlank()) {
            return false
        }

        val status = repository.openWallet(password)

        if (status != Status.STATUS_OK) {
            return false
        }


        txDisposable = repository.getTxStatus().subscribe {
            if (it.action == ChangeAction.ADDED && App.showNotification) {
                createNotificationChannel()

                val txDescription = it.tx?.firstOrNull()

                if (txDescription != null && txDescription.sender == TxSender.RECEIVED) {
                    val txId = txDescription.id

                    val intent = Intent(applicationContext, WelcomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }

                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle(getString(R.string.notification_receive_content_title))
                            .setContentText(getString(R.string.notification_receive_content_text))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .setVibrate(longArrayOf(500, 500, 500, 500))
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setLights(getColor(R.color.received_color), 2000, 2000)
                            .setContentIntent(PendingIntent.getActivity(applicationContext, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                            .build()

                    with(NotificationManagerCompat.from(applicationContext)) {
                        notify(txId.hashCode(), notification)
                    }
                }
            }
        }

        return true
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}
