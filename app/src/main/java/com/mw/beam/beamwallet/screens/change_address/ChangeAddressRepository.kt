package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryHelper
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.Observable
import io.reactivex.subjects.Subject

class ChangeAddressRepository: BaseRepository(), ChangeAddressContract.Repository {
    override fun getAddresses(): Subject<OnAddressesData> {
        return getResult(WalletListener.subOnAddresses, "getAddresses") {
            wallet?.getAddresses(true)
            wallet?.getAddresses(false)
        }
    }

    override fun getTxStatus(): Observable<OnTxStatusData> {
        return getResult(WalletListener.obsOnTxStatus, "getTxStatus") {
            wallet?.getWalletStatus()
        }
    }

    override fun getCategoryForAddress(address: String): Category? {
        return CategoryHelper.getCategoryForAddress(address)
    }

    override fun getTrashSubject(): Subject<TrashManager.Action> {
        return TrashManager.subOnTrashChanged
    }

    override fun getAllTransactionInTrash(): List<TxDescription> {
        return TrashManager.getAllData().transactions
    }

    override fun getAllAddressesInTrash(): List<WalletAddress> {
        return TrashManager.getAllData().addresses
    }
}