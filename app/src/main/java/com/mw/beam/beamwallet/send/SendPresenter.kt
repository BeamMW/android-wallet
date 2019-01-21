package com.mw.beam.beamwallet.send

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.convertToGroth
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendPresenter(currentView: SendContract.View, currentRepository: SendContract.Repository, private val state: SendState)
    : BasePresenter<SendContract.View, SendContract.Repository>(currentView, currentRepository),
        SendContract.Presenter {
    private lateinit var walletStatusSubscription: Disposable
    private val tokenRegex = Regex("[^A-Za-z0-9]")
    private val MAX_TOKEN_LENGTH = 80

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onSend() {
        if (view?.hasErrors(state.walletStatus?.available ?: 0) == false) {
            val amount = view?.getAmount()
            val fee = view?.getFee()
            val comment = view?.getComment()
            val token = view?.getToken()

            if (amount != null && fee != null && token != null) {
                repository.sendMoney(token, comment, amount.convertToGroth(), fee)
                view?.close()
            }
        }
    }

    override fun onTokenChanged(rawToken: String?) {
        var clearedToken = rawToken?.replace(tokenRegex, "")

        if (!clearedToken.isNullOrEmpty() && clearedToken.length > MAX_TOKEN_LENGTH) {
            clearedToken = clearedToken.substring(0, MAX_TOKEN_LENGTH)
        }

        if (rawToken == clearedToken) {
            val isTokenEmpty = rawToken.isNullOrEmpty()

            if (isTokenEmpty != state.isTokenEmpty) {
                view?.updateUI(!isTokenEmpty)
            }

            state.isTokenEmpty = isTokenEmpty
        } else {
            view?.clearToken(clearedToken)
        }
    }

    override fun onAmountChanged() {
        view?.clearErrors()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletStatusSubscription = repository.getWalletStatus().subscribe {
            state.walletStatus = it
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletStatusSubscription)

    override fun hasStatus(): Boolean = true
}
