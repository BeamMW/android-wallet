package com.mw.beam.beamwallet.receive

import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod

/**
 * Created by vain onnellinen on 1/4/19.
 */
class ReceiveState {
    var address: WalletAddress? = null
    var wasAddressSaved: Boolean = false
    var expirePeriod: ExpirePeriod = ExpirePeriod.DAY
}
