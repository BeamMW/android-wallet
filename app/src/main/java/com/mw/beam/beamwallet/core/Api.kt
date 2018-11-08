package com.mw.beam.beamwallet.core

import com.mw.beam.beamwallet.core.entities.Wallet

/**
 * Created by vain onnellinen on 10/1/18.
 */
object Api {

    init {
        System.loadLibrary("wallet-jni")
    }

    external fun createWallet(nodeAddr: String, dbPath: String, pass: String, seed: String): Wallet?
    external fun openWallet(nodeAddr: String, dbPath: String, pass: String): Wallet?
    external fun isWalletInitialized(dbPath: String): Boolean
}
