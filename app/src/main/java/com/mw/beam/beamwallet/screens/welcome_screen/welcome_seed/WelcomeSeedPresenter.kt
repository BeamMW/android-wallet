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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_seed

import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.utils.LogUtils

/**
 *  10/30/18.
 */
class WelcomeSeedPresenter(currentView: WelcomeSeedContract.View, currentRepository: WelcomeSeedContract.Repository)
    : BasePresenter<WelcomeSeedContract.View, WelcomeSeedContract.Repository>(currentView, currentRepository),
        WelcomeSeedContract.Presenter {
    private val COPY_TAG = "RECOVERY SEED"

    override fun onViewCreated() {
        super.onViewCreated()
        view?.configSeed(repository.seed)

        if (BuildConfig.DEBUG) {
            LogUtils.log("Seed phrase: \n${prepareSeed(repository.seed)}")
        }
    }

    override fun onDonePressed() {
        view?.showConfirmFragment(repository.seed)
    }

    override fun onNextPressed() {
        view?.showSaveAlert()
    }

    override fun onCopyPressed() {
        view?.copyToClipboard(prepareSeed(repository.seed), COPY_TAG)
        view?.showCopiedAlert()
    }

    private fun prepareSeed(seed: Array<String>): String {
        val result = StringBuilder()

        for ((index, value) in seed.withIndex()) {
            result.append((index + 1).toString())
                    .append(" ")
                    .append(value)

            if (index != seed.lastIndex) {
                result.append("\n")
            }
        }

        return result.toString()
    }
}
