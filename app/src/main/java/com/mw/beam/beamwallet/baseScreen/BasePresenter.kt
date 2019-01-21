package com.mw.beam.beamwallet.baseScreen

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/1/18.
 */
abstract class BasePresenter<T : MvpView, R : MvpRepository>(var view: T?, var repository: R) : MvpPresenter<T> {
    private lateinit var disposable: CompositeDisposable
    private lateinit var nodeConnectionSubscription: Disposable
    private lateinit var nodeConnectionFailedSubscription: Disposable
    private lateinit var syncProgressUpdatedSubscription: Disposable

    override fun onCreate() {
    }

    override fun onViewCreated() {
        view?.initToolbar(view?.getToolbarTitle(), hasBackArrow(), hasStatus())
    }

    override fun onStart() {
        disposable = CompositeDisposable()
        initSubscriptions()

        val subscriptions = getSubscriptions()

        if (subscriptions != null) {
            disposable.addAll(*subscriptions)
        }

        disposable.apply {
            add(nodeConnectionSubscription)
            add(nodeConnectionFailedSubscription)
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

    override fun initSubscriptions() {
        nodeConnectionSubscription = repository.getNodeConnectionStatusChanged().subscribe {
            view?.configStatus(it)
        }

        nodeConnectionFailedSubscription = repository.getNodeConnectionFailed().subscribe {
            view?.configStatus(false)
        }

        syncProgressUpdatedSubscription = repository.getSyncProgressUpdated().subscribe {
            view?.configStatus(it.done == it.total)
        }
    }

    private fun detachView() {
        view = null
    }

    override fun hasStatus(): Boolean = false
    override fun hasBackArrow(): Boolean? = true
}
