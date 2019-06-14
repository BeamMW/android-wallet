package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.WalletAddress
import io.reactivex.disposables.Disposable

class ChangeAddressPresenter(view: ChangeAddressContract.View?, repository: ChangeAddressContract.Repository, private val state: ChangeAddressState)
    : BasePresenter<ChangeAddressContract.View, ChangeAddressContract.Repository>(view, repository), ChangeAddressContract.Presenter {
    private lateinit var addressesSubscription: Disposable
    private lateinit var transactionsSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()

        state.viewState = if (view?.isFromReceive() != false) ChangeAddressContract.ViewState.Receive else ChangeAddressContract.ViewState.Send

        view?.init(state.viewState)

    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        addressesSubscription = repository.getAddresses().subscribe {
            val addresses = it.addresses?.filter { walletAddress -> !walletAddress.isContact && !walletAddress.isExpired }
            state.updateAddresses(addresses)
        }

        transactionsSubscription = repository.getTxStatus().subscribe {
            state.updateTransactions(it.tx)
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, transactionsSubscription)

    override fun onChangeSearchText(text: String) {
        if (text.isBlank()) {
            view?.updateList(listOf())
            return
        }
        
        val searchText = text.trim().toLowerCase()
        
        val newItems = state.getAddresses().filter {
            it.label.toLowerCase().contains(searchText) ||
                    it.walletID.toLowerCase().contains(searchText) ||
                    repository.getCategoryForAddress(it.walletID)?.name?.toLowerCase()?.contains(searchText) ?: false
        }.map { walletAddress ->
            SearchItem(walletAddress,
                    state.getTransactions().firstOrNull { it.myId == walletAddress.walletID || it.peerId == walletAddress.walletID },
                    repository.getCategoryForAddress(walletAddress.walletID))
        }

        view?.updateList(newItems)
    }

    override fun onItemPressed(walletAddress: WalletAddress) {
        view?.back(walletAddress)
    }

    override fun onScanQrPressed() {
        view?.showScanQr()
    }

    override fun onScannedQr(address: String) {

    }
}