package com.mw.beam.beamwallet.core

import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import java.util.prefs.Preferences

class OnboardManager {

    companion object {
        private var INSTANCE: OnboardManager? = null

        val instance: OnboardManager
            get() {
                if (INSTANCE == null) {
                    INSTANCE = OnboardManager()
                }

                return INSTANCE!!
            }
    }

    private val minAmountToSecure:Double = 100.0

    var isCloseSecure = false
    var isCloseFaucet = false

    fun reset() {
        isCloseSecure = false
        isCloseFaucet = false

        PreferencesManager.putBoolean(PreferencesManager.KEY_SEED_IS_SKIP, false)
    }

    fun isSkipedSeed(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_SEED_IS_SKIP,false)
    }

    fun getSeed():String? {
        return PreferencesManager.getString(PreferencesManager.KEY_SEED)
    }

    fun canReceiveFaucet(): Boolean {
        val isInProgress = AppManager.instance.getStatus().sending > 0 || AppManager.instance.getStatus().receiving > 0
        val isBalanceZero = AppManager.instance.getStatus().available == 0L
        return !isInProgress && isBalanceZero && !isCloseFaucet
    }

    fun canMakeSecure(): Boolean {
        val available = AppManager.instance.getStatus().available

        if (available.convertToBeam() >= minAmountToSecure && !isCloseSecure && isSkipedSeed())
        {
            return  true
        }

        return  false
    }

    fun makeSecure() {
        PreferencesManager.putBoolean(PreferencesManager.KEY_SEED_IS_SKIP, false)
    }


}