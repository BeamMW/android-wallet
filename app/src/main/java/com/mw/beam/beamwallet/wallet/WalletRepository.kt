package com.mw.beam.beamwallet.wallet

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxPeer
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject


/**
 * Created by vain onnellinen on 10/1/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class WalletRepository : BaseRepository(), WalletContract.Repository {

    override fun getWalletStatus(): Subject<WalletStatus> {
        return getResult({}, WalletListener.subOnStatus, object {}.javaClass.enclosingMethod.name)
    }

    override fun getTxStatus(): Subject<OnTxStatusData> {
        return getResult({getWallet()?.getWalletStatus()}, WalletListener.subOnTxStatus, object {}.javaClass.enclosingMethod.name)
    }

    override fun getTxPeerUpdated(): Subject<Array<TxPeer>?> {
        return getResult({}, WalletListener.subOnTxPeerUpdated, object {}.javaClass.enclosingMethod.name)
    }
}
