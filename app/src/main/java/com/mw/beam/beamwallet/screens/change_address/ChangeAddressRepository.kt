package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryHelper
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

class ChangeAddressRepository: BaseRepository(), ChangeAddressContract.Repository {
    override fun getAddresses(): Subject<OnAddressesData> {
        return getResult(WalletListener.subOnAddresses, "getAddresses") {
            wallet?.getAddresses(true)
            wallet?.getAddresses(false)
        }
    }

    override fun getTxStatus(): Subject<OnTxStatusData> {
        return getResult(WalletListener.subOnTxStatus, "getTxStatus") {
            wallet?.getWalletStatus()
        }
    }

    override fun getCategoryForAddress(address: String): Category? {
        return CategoryHelper.getCategoryForAddress(address)
    }
}