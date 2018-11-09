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
    }

    override fun onStart() {
        if (getSubscriptions() != null) {
            disposable.addAll(*getSubscriptions()!!)
        }
    }

    override fun onResume() {
    }

    override fun onPause() {
    }

    override fun onStop() {
        disposable.dispose()
        view?.dismissAlert()
    }

    override fun onDestroy() {
        detachView()
    }

    override fun getSubscriptions(): Array<Disposable>? = null

    private fun detachView() {
        view = null
    }
}
