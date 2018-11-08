package com.mw.beam.beamwallet.baseScreen

import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/1/18.
 */
interface MvpPresenter<V : MvpView> {
    fun viewIsReady()
    fun detachView()
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun getSubscriptions() : Array<Disposable>?
}
