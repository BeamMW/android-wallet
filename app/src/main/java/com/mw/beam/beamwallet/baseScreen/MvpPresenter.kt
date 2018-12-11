package com.mw.beam.beamwallet.baseScreen

import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/1/18.
 */
interface MvpPresenter<V : MvpView> {
    fun onCreate()
    fun onViewCreated()
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onDestroy()
    fun hasStatus(): Boolean = false
    fun hasBackArrow(): Boolean? = false
    fun getSubscriptions(): Array<Disposable>?
}
