package com.mw.beam.beamwallet.core.helpers

import android.content.pm.PackageManager

import com.mw.beam.beamwallet.core.App
import androidx.biometric.BiometricManager


object FaceIDManager {
    private var biometricManager: BiometricManager? = null

    private var isFaceIDAvailable = false

    init {
        val pm = App.self.applicationContext.packageManager
        isFaceIDAvailable = pm.hasSystemFeature(PackageManager.FEATURE_FACE)
        //pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) FEATURE_FACE FEATURE_FINGERPRINT

        biometricManager = BiometricManager.from(App.self.applicationContext)
    }

    fun isManagerAvailable(): Boolean {
        val version = android.os.Build.VERSION.SDK_INT
        val status = biometricManager?.canAuthenticate()
        return version >= 29 && isFaceIDAvailable && status == BiometricManager.BIOMETRIC_SUCCESS
    }
}