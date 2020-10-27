package com.mw.beam.beamwallet.screens.app_activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mw.beam.beamwallet.core.AppManager


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent.action
        // using action u can detect
        // using action u can detect
        val data: Uri? = intent.data

        if(data != null)
        {
            val amount: String? = data.getQueryParameter("amount")
            val userId: String? = data.getQueryParameter("user_id")

            if (amount != null && userId != null) {
                AppActivity.withdrawAmount = amount.toInt()
                AppActivity.withdrawUserId = userId
            }
        }


        val intent = Intent(this, AppActivity::class.java)
        startActivity(intent)

        finish()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)

        androidx.multidex.MultiDex.install(this);
    }

}