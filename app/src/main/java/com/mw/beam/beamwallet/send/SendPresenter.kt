package com.mw.beam.beamwallet.send

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.convertToGroth

/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendPresenter(currentView: SendContract.View, private val repository: SendContract.Repository, private val state: SendState)
    : BasePresenter<SendContract.View>(currentView),
        SendContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onSend() {
        if (view?.hasErrors() == false) {
            val amount = view?.getAmount()
            val fee = view?.getFee()
            val comment = view?.getComment()
            val token = view?.getToken()

            if (amount != null && fee != null && token != null) {
                repository.sendMoney(token, comment, amount.convertToGroth(), fee)
            }
        }
    }

    override fun onTokenChanged(isTokenEmpty: Boolean) {
        if (isTokenEmpty != state.isTokenEmpty) {
            view?.updateUI(!isTokenEmpty)
        }

        state.isTokenEmpty = isTokenEmpty
    }

    override fun onAmountChanged() {
        view?.clearErrors()
    }

    override fun hasStatus(): Boolean = true
}
