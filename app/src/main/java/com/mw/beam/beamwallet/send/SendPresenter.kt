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
}
