package com.mw.beam.beamwallet.service

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.Observable
import io.reactivex.subjects.Subject

class BackgroundServiceRepository: BaseRepository() {
    fun isWalletRunning(): Boolean {
        return Api.isWalletRunning()
    }

    fun getPassword(): String? {
        return PreferencesManager.getString(PreferencesManager.KEY_PASSWORD)
    }

    fun getTxStatus(): Observable<OnTxStatusData> {
        return getResult(WalletListener.obsOnTxStatus, "getTxStatus") { wallet?.getWalletStatus() }
    }
}