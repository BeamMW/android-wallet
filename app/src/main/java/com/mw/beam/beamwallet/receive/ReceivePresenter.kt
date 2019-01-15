package com.mw.beam.beamwallet.receive

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceivePresenter(currentView: ReceiveContract.View, currentRepository: ReceiveContract.Repository, private val state: ReceiveState)
    : BasePresenter<ReceiveContract.View, ReceiveContract.Repository>(currentView, currentRepository),
        ReceiveContract.Presenter {
    private lateinit var walletIdSubscription: Disposable

    override fun onCopyTokenPressed() {
        saveAddress()

        if (state.address != null) {
            view?.copyToClipboard(state.address!!.walletID)
        }
    }

    override fun onShowQrPressed() {
        saveAddress()
        view?.showSnackBar("Coming soon...")
    }

    override fun onBackPressed() {
        saveAddress()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletIdSubscription = repository.generateNewAddress().subscribe {
            state.address = it
            view?.showToken(state.address!!.walletID)
        }
    }

    private fun saveAddress() {
        if (state.address != null && !state.wasAddressSaved) {
            val comment = view?.getComment()

            if (!comment.isNullOrBlank()) {
                state.address!!.label = comment
            }

            repository.saveAddress(state.address!!)
            state.wasAddressSaved = true
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletIdSubscription)
}
