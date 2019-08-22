package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.DelayedTask
import com.mw.beam.beamwallet.core.helpers.EmptyDisposable
import io.reactivex.disposables.Disposable

class RestoreTrustedNodePresenter(view: RestoreTrustedNodeContract.View?, repository: RestoreTrustedNodeContract.Repository)
    : BasePresenter<RestoreTrustedNodeContract.View, RestoreTrustedNodeContract.Repository>(view, repository), RestoreTrustedNodeContract.Presenter {

    private var delayedTask: DelayedTask? = null

    private var nodeConnectionSubscription: Disposable = EmptyDisposable()
        set(value) {
            if (!field.isDisposed)
                field.dispose()

            field = value
        }

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onStart() {
        super.onStart()
        view?.dismissLoading()
    }

    override fun onNextPressed() {
        view?.showLoading()

        nodeConnectionSubscription = repository.getSyncProgressUpdated().subscribe {
            view?.navigateToProgress()
        }

        repository.connectToNode(view?.getNodeAddress() ?: "")
        repository.wallet?.syncWithNode()

        delayedTask = DelayedTask.startNew(15, {
            nodeConnectionSubscription.dispose()

            view?.dismissLoading()
            view?.showError()
        })
    }

    override fun onStop() {
        super.onStop()
        nodeConnectionSubscription.dispose()
        delayedTask?.cancel(true)
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(nodeConnectionSubscription)
}