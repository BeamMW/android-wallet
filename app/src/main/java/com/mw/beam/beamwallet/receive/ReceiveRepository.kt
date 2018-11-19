package com.mw.beam.beamwallet.receive

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 11/13/18.
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ReceiveRepository : BaseRepository(), ReceiveContract.Repository {

    override fun generateWalletId(): Subject<ByteArray> {
        return getResult({ wallet?.generateNewWalletID() }, WalletListener.subOnGeneratedNewWalletID, object {}.javaClass.enclosingMethod.name)
    }
}
