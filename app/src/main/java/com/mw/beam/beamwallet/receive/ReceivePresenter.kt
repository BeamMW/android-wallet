package com.mw.beam.beamwallet.receive

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.toHex
import com.mw.beam.beamwallet.core.utils.LogUtils
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceivePresenter(currentView: ReceiveContract.View, private val repository: ReceiveContract.Repository)
    : BasePresenter<ReceiveContract.View>(currentView),
        ReceiveContract.Presenter {
    private lateinit var walletIdSubscription: Disposable

    override fun onStart() {
        super.onStart()
        view?.init()
    }

    private fun initSubscriptions() {
        walletIdSubscription = repository.generateWalletId().subscribe {
            //TODO should be done by pressing button
            repository.createNewAddress(it, "temp comment")
            LogUtils.log(it.toHex())
        }
    }

    override fun getSubscriptions(): Array<Disposable>? {
        initSubscriptions()
        return arrayOf(walletIdSubscription)
    }
}
