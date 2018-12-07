package com.mw.beam.beamwallet.receive

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 11/13/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ReceiveRepository : BaseRepository(), ReceiveContract.Repository {

    override fun generateNewAddress(): Subject<WalletAddress> {
        return getResult({ wallet?.generateNewAddress() }, WalletListener.subOnGeneratedNewAddress, object {}.javaClass.enclosingMethod.name)
    }

    override fun saveAddress(address: WalletAddress) {
        wallet?.saveAddress(address, true)
    }
}
