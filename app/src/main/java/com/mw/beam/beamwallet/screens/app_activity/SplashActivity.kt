package com.mw.beam.beamwallet.screens.app_activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.mw.beam.beamwallet.R


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, AppActivity::class.java)
        startActivity(intent)

        finish()
    }

}