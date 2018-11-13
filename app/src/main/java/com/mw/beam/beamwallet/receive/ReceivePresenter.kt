package com.mw.beam.beamwallet.receive

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceivePresenter(currentView: ReceiveContract.View, private val repository: ReceiveContract.Repository)
    : BasePresenter<ReceiveContract.View>(currentView),
        ReceiveContract.Presenter {

    override fun onStart() {
        super.onStart()
        view?.init()
    }
}
