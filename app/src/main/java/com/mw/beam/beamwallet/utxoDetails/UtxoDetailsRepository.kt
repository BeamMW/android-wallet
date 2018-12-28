package com.mw.beam.beamwallet.utxoDetails

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo

/**
 * Created by vain onnellinen on 12/20/18.
 */
class UtxoDetailsRepository : BaseRepository(), UtxoDetailsContract.Repository {
    override var utxo: Utxo? = null
    override var relatedTransactions: ArrayList<TxDescription>? = null
}
