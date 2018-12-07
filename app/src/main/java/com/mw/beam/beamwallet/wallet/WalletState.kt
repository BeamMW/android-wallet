package com.mw.beam.beamwallet.wallet

/**
 * Created by vain onnellinen on 12/4/18.
 */
class WalletState {
    var maturing: Long = 0L
    var receiving: Long = 0L
    var sending: Long = 0L
    var available: Long = 0L

    var shouldExpandAvailable = false
    var shouldExpandInProgress = false
}
