package com.mw.beam.beamwallet.baseScreen

import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.entities.Wallet
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 10/8/18.
 */
interface MvpRepository {
    var wallet: Wallet?

    fun getNodeConnectionStatusChanged(): Subject<Boolean>
    fun getNodeConnectionFailed(): Subject<Any>
    fun getSyncProgressUpdated(): Subject<OnSyncProgressData>
}
