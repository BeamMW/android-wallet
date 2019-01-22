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

package com.mw.beam.beamwallet.welcomeScreen.welcomePhrase

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.utils.LogUtils

/**
 * Created by vain onnellinen on 10/30/18.
 */
class WelcomePhrasePresenter(currentView: WelcomePhraseContract.View, currentRepository: WelcomePhraseContract.Repository)
    : BasePresenter<WelcomePhraseContract.View, WelcomePhraseContract.Repository>(currentView, currentRepository),
        WelcomePhraseContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.configPhrases(repository.phrases)

        LogUtils.log("Recovery phrase: \n${preparePhrases(repository.phrases)}")
    }

    override fun onDonePressed() {
        view?.showValidationFragment(repository.phrases)
    }

    override fun onNextPressed() {
        view?.showSaveAlert()
    }

    override fun onCopyPressed() {
        view?.copyToClipboard(preparePhrases(repository.phrases))
        view?.showCopiedAlert()
    }

    private fun preparePhrases(phrases: Array<String>): String {
        val result = StringBuilder()

        for ((index, value) in phrases.withIndex()) {
            result.append((index + 1).toString())
                    .append(" ")
                    .append(value)

            if (index != phrases.lastIndex) {
                result.append("\n")
            }
        }

        return result.toString()
    }
}
