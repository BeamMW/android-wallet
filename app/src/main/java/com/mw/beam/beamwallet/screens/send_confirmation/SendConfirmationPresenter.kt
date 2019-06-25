package com.mw.beam.beamwallet.screens.send_confirmation

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import io.reactivex.disposables.Disposable

class SendConfirmationPresenter(view: SendConfirmationContract.View?, repository: SendConfirmationContract.Repository, private val state: SendConfirmationState)
    : BasePresenter<SendConfirmationContract.View, SendConfirmationContract.Repository>(view, repository), SendConfirmationContract.Presenter {
    private lateinit var addressesSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            state.token = getAddress()
            state.outgoingAddress = getOutgoingAddress()
            state.amount = getAmount()
            state.fee = getFee()


            init(state.token, state.outgoingAddress, state.amount.convertToBeam(), state.fee)
        }
    }

    override fun onSendPressed() {
        if (state.contact == null) {
            view?.showSaveContactDialog()
        } else {
            showWallet()
        }
    }

    override fun initSubscriptions() {
        addressesSubscription = repository.getAddresses().subscribe {
            it.addresses?.forEach { address ->
                state.addresses[address.walletID] = address
            }

            val findAddress = state.addresses.values.find { it.walletID == state.token }
            if (findAddress != null) {
                state.contact = findAddress
                view?.configurateContact(findAddress, repository.getCategory(findAddress.walletID))
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription)

    private fun showWallet() {
        state.apply { view?.delaySend(outgoingAddress, token, comment, amount, fee) }
        view?.showWallet()
    }

    override fun onCancelSaveContactPressed() {
        showWallet()
    }

    override fun onSaveContactPressed() {
        state.apply { view?.delaySend(outgoingAddress, token, comment, amount, fee) }
        view?.showSaveAddressFragment(state.token)
    }



}