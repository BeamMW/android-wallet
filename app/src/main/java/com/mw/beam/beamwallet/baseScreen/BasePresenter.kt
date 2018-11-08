package com.mw.beam.beamwallet.baseScreen

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/1/18.
 */
abstract class BasePresenter<T : MvpView>(var view: T?) : MvpPresenter<T> {
    private val disposable = CompositeDisposable()

    override fun detachView() {
        view = null
    }

    override fun onStart() {
    }

    override fun onResume() {
        if (getSubscriptions() != null) {
            disposable.addAll(*getSubscriptions()!!)
        }
    }

    override fun onPause() {
        disposable.dispose()
    }

    override fun onStop() {
    }

    protected fun isViewAttached(): Boolean {
        return view != null
    }

    override fun viewIsReady() {
    }

    override fun getSubscriptions(): Array<Disposable>? = null
}
