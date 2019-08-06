package com.mw.beam.beamwallet.screens.welcome_screen.restore_owner_key

import com.mw.beam.beamwallet.base_screen.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class RestoreOwnerKeyPresenter(view: RestoreOwnerKeyContract.View?, repository: RestoreOwnerKeyContract.Repository)
    : BasePresenter<RestoreOwnerKeyContract.View, RestoreOwnerKeyContract.Repository>(view, repository), RestoreOwnerKeyContract.Presenter {
    private var ownerKey: String? = null
    private var ownerKeyDisposable: Disposable? = null
    private val copyTag = "OwnerKey"

    override fun onViewCreated() {
        super.onViewCreated()
        if (ownerKey.isNullOrBlank()) {
            val pass = view?.getPassword()
            ownerKeyDisposable = PublishSubject.fromArray(arrayOf(true))
                    .map {
                        repository.createWallet(pass, view?.getSeed()?.joinToString(separator = ";", postfix = ";"))
                        repository.getOwnerKey(pass ?: "")
                    }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        ownerKey = it
                        view?.init(it)
                        ownerKeyDisposable?.dispose()
                    }
        } else {
            view?.init(ownerKey!!)
        }
    }

    override fun onCopyPressed() {
        if (!ownerKey.isNullOrBlank()) {
            view?.copyToClipboard(ownerKey, copyTag)
            view?.showCopiedSnackBar()
        }
    }

    override fun onNextPressed() {
        if (!ownerKey.isNullOrBlank()) {
            view?.navigateToEnterTrustedNode()
        }
    }
}