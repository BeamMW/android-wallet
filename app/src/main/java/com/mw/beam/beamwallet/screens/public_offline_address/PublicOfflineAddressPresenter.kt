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

package com.mw.beam.beamwallet.screens.public_offline_address

import android.graphics.Bitmap
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import io.reactivex.disposables.Disposable

/**
 *  3/4/19.
 */
class PublicOfflineAddressPresenter(currentView: PublicOfflineAddressContract.View, currentRepository: PublicOfflineAddressContract.Repository, private val state: PublicOfflineAddressState)
    : BasePresenter<PublicOfflineAddressContract.View, PublicOfflineAddressContract.Repository>(currentView, currentRepository),
        PublicOfflineAddressContract.Presenter {

    private val COPY_TAG = "ADDRESS"
    private lateinit var addressSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        addressSubscription = AppManager.instance.subOnPublicAddress.subscribe(){
            if(it!=null) {
                AppActivity.self.runOnUiThread {
                    view?.init(it)
                }
                state.token = it
            }
        }

        if(state.token == null) {
            AppManager.instance.getPublicAddress()
        }
        else {
            view?.init(state.token ?: return)
        }
    }

    override fun onSharePressed(image: Bitmap) {
        view?.shareQR(repository.saveImage(image))
    }

    override fun onCopyToken() {
        view?.copyToClipboard(state.token ?: return, COPY_TAG)
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressSubscription)

}
