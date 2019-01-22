// Copyright 2018 Beam Development
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.mw.beam.beamwallet.utxoDetails

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 12/20/18.
 */
class UtxoDetailsPresenter(currentView: UtxoDetailsContract.View, currentRepository: UtxoDetailsContract.Repository)
    : BasePresenter<UtxoDetailsContract.View, UtxoDetailsContract.Repository>(currentView, currentRepository),
        UtxoDetailsContract.Presenter {

    override fun onCreate() {
        super.onCreate()
        repository.utxo = view?.getUtxoDetails()
        repository.relatedTransactions = view?.getRelatedTransactions()
    }

    override fun onStart() {
        super.onStart()
        view?.init(repository.utxo ?: return, repository.relatedTransactions ?: return)
    }
}
