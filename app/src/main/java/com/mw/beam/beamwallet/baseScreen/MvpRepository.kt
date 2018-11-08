package com.mw.beam.beamwallet.baseScreen

import com.mw.beam.beamwallet.core.entities.Wallet

/**
 * Created by vain onnellinen on 10/8/18.
 */
interface MvpRepository {
    fun getWallet() : Wallet?
    fun setWallet(wallet : Wallet)
}
