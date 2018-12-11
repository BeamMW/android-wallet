package com.mw.beam.beamwallet.baseScreen

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/1/18.
 */
abstract class BasePresenter<T : MvpView>(var view: T?) : MvpPresenter<T> {
    private val disposable = CompositeDisposable()

    override fun onCreate() {
    }

    override fun onViewCreated() {
        view?.initToolbar(view?.getToolbarTitle(), hasBackArrow(), hasStatus())
    }

    override fun onStart() {
        if (getSubscriptions() != null) {
            disposable.addAll(*getSubscriptions()!!)
        }

        view?.addListeners()

    }

    override fun onResume() {
    }

    override fun onPause() {
    }

    // Why you need to unregister listeners? See https://stackoverflow.com/q/38368391
    override fun onStop() {
        disposable.dispose()
        view?.dismissAlert()
        view?.clearListeners()
    }

    override fun onDestroy() {
        detachView()
    }

    override fun getSubscriptions(): Array<Disposable>? = null

    private fun detachView() {
        view = null
    }

    override fun hasStatus(): Boolean = false
    override fun hasBackArrow(): Boolean? = true
}
