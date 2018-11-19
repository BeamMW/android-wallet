package com.mw.beam.beamwallet.send

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendPresenter(currentView: SendContract.View, private val repository: SendContract.Repository)
    : BasePresenter<SendContract.View>(currentView),
        SendContract.Presenter {

    override fun onStart() {
        super.onStart()
        view?.init()
    }

    override fun onSend() {
        //TODO handle errors correctly
        val amount = view?.getAmount()
        val fee = view?.getFee()
        val comment = view?.getComment()
        val token = view?.getToken()

        if (amount != null && fee != null && token != null) {
            repository.sendMoney(token, comment, amount, fee)
        }
    }
}
