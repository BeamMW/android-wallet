package com.mw.beam.beamwallet.screens.owner_key

import com.mw.beam.beamwallet.base_screen.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class OwnerKeyPresenter(view: OwnerKeyContract.View?, repository: OwnerKeyContract.Repository, val state: OwnerKeyState)
    : BasePresenter<OwnerKeyContract.View, OwnerKeyContract.Repository>(view, repository), OwnerKeyContract.Presenter {
    private lateinit var keyDisposable: Disposable
    private val copyTag = "OwnerKey"

    override fun initSubscriptions() {
        super.initSubscriptions()
        keyDisposable = PublishSubject.fromArray(arrayOf(true))
                .map { repository.getOwnerKey() }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    state.key = it
                    view?.init(it)
                }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(keyDisposable)

    override fun onCopyPressed() {
        view?.copyToClipboard(state.key, copyTag)
        view?.showCopiedSnackBar()
    }
}