package com.mw.beam.beamwallet.receive

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.entities.WalletAddress
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceivePresenter(currentView: ReceiveContract.View, private val repository: ReceiveContract.Repository)
    : BasePresenter<ReceiveContract.View>(currentView),
        ReceiveContract.Presenter {
    private lateinit var walletIdSubscription: Disposable
    //TODO should be in state
    private var address: WalletAddress? = null

    override fun onNextPressed() {
        if(address != null) {
            if (!view?.getComment().isNullOrBlank()) {
                address?.label = view!!.getComment()!!
            }

            repository.saveAddress(address!!)
            view?.showToken(address!!.walletID)
        }
    }

    private fun initSubscriptions() {
        walletIdSubscription = repository.generateNewAddress().subscribe {
            address = it
        }
    }

    override fun getSubscriptions(): Array<Disposable>? {
        initSubscriptions()
        return arrayOf(walletIdSubscription)
    }
}
