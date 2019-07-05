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

package com.mw.beam.beamwallet.screens.language

import com.mw.beam.beamwallet.base_screen.BasePresenter

class LanguagePresenter(view: LanguageContract.View?, repository: LanguageContract.Repository)
    : BasePresenter<LanguageContract.View, LanguageContract.Repository>(view, repository), LanguageContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(repository.getLanguages(), repository.getCurrentLanguageIndex())
    }

    override fun onRestartPressed(index: Int) {
        repository.setLanguage(index)
        view?.logOut()
    }

    override fun onSelectLanguage(index: Int) {
        if (index != repository.getCurrentLanguageIndex()) {
            view?.showConfirmDialog(index)
        }
    }

}