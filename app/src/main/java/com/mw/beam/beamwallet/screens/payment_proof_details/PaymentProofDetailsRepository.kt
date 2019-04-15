package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

class PaymentProofDetailsRepository: BaseRepository(), PaymentProofDetailsContract.Repository