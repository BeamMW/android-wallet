package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

class PaymentProofDetailsRepository: BaseRepository(), PaymentProofDetailsContract.Repository {

    override fun getTxStatus(): Subject<OnTxStatusData> {
        return getResult(WalletListener.subOnTxStatus, "getTxStatus")
    }

    override fun getPaymentProofs(txId: String): Subject<PaymentProof> {
        return getResult(WalletListener.subOnPaymentProofExported, "getPaymentProofs") {
            wallet?.getPaymentInfo(txId)
        }
    }
}