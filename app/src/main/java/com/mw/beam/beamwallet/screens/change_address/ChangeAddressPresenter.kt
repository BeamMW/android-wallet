package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.PermissionStatus
import com.mw.beam.beamwallet.core.helpers.QrHelper
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.disposables.Disposable

class ChangeAddressPresenter(view: ChangeAddressContract.View?, repository: ChangeAddressContract.Repository, private val state: ChangeAddressState)
    : BasePresenter<ChangeAddressContract.View, ChangeAddressContract.Repository>(view, repository), ChangeAddressContract.Presenter {
    private lateinit var addressesSubscription: Disposable
    private lateinit var transactionsSubscription: Disposable
    private lateinit var trashSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()

        state.viewState = if (view?.isFromReceive() != false) ChangeAddressContract.ViewState.Receive else ChangeAddressContract.ViewState.Send
        state.generatedAddress = view?.getGeneratedAddress()

        view?.init(state.viewState, state.generatedAddress)

    }

    override fun onStart() {
        super.onStart()

        if (state.scannedAddress != null) {
            state.scannedAddress?.let { view?.setAddress(it) }

            state.scannedAddress = null
        }
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        addressesSubscription = repository.getAddresses().subscribe {
            val addresses = it.addresses?.filter { walletAddress -> !walletAddress.isContact && !walletAddress.isExpired }
            state.updateAddresses(addresses)
            state.deleteAddresses(repository.getAllAddressesInTrash())

            onChangeSearchText(view?.getSearchText() ?: "")
        }

        transactionsSubscription = repository.getTxStatus().subscribe {
            state.updateTransactions(it.tx)
            state.deleteTransactions(repository.getAllTransactionInTrash())
        }

        trashSubscription = repository.getTrashSubject().subscribe {
            when (it.type) {
                TrashManager.ActionType.Added -> {
                    state.deleteTransactions(it.data.transactions)
                    state.deleteAddresses(it.data.addresses)
                    onChangeSearchText(view?.getSearchText() ?: "")
                }

                TrashManager.ActionType.Restored -> {
                    state.updateAddresses(it.data.addresses)
                    state.updateTransactions(it.data.transactions)
                    onChangeSearchText(view?.getSearchText() ?: "")
                }

                TrashManager.ActionType.Removed -> {}
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, transactionsSubscription, trashSubscription)

    override fun onChangeSearchText(text: String) {
        if (text.isBlank()) {
            view?.updateList(state.getAddresses().map(::addressToSearchItem))
            return
        }
        
        val searchText = text.trim().toLowerCase()
        
        val newItems = state.getAddresses().filter {
            it.label.toLowerCase().contains(searchText) ||
                    it.walletID.toLowerCase().contains(searchText) ||
                    repository.getCategoryForAddress(it.walletID)?.name?.toLowerCase()?.contains(searchText) ?: false
        }.map(::addressToSearchItem)

        view?.updateList(newItems)
    }

    private fun addressToSearchItem(walletAddress: WalletAddress): SearchItem {
        return SearchItem(walletAddress,
                state.getTransactions().firstOrNull { it.myId == walletAddress.walletID || it.peerId == walletAddress.walletID },
                repository.getCategoryForAddress(walletAddress.walletID))
    }

    override fun onItemPressed(walletAddress: WalletAddress) {
        if (walletAddress.walletID == state.generatedAddress?.walletID) {
            view?.back(null)
        } else {
            view?.back(walletAddress)
        }
    }

    override fun onScanQrPressed() {
        if (view?.isPermissionGranted() == true) {
            view?.scanQR()
        }
    }

    override fun onRequestPermissionsResult(result: PermissionStatus) {
        when (result) {
            PermissionStatus.GRANTED -> view?.scanQR()
            PermissionStatus.NEVER_ASK_AGAIN -> {
                view?.showPermissionRequiredAlert()
            }
            PermissionStatus.DECLINED -> {
                //do nothing
            }
        }
    }

    override fun onScannedQR(address: String?) {
        if (address == null) return

        val scannedAddress = QrHelper.getScannedAddress(address)
        val isValidAddress = QrHelper.isValidAddress(scannedAddress)

        if (isValidAddress) {
            state.scannedAddress = scannedAddress
        } else {
            view?.showNotBeamAddressError()
        }
    }
}