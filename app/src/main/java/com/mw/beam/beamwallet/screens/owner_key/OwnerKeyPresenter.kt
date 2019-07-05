/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

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