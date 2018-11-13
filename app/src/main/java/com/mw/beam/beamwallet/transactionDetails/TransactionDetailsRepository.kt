package com.mw.beam.beamwallet.transactionDetails

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.entities.TxDescription

/**
 * Created by vain onnellinen on 10/18/18.
 */
class TransactionDetailsRepository : BaseRepository(), TransactionDetailsContract.Repository{

    override var txDescription: TxDescription? = null
}
