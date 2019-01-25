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

package com.mw.beam.beamwallet.welcome_screen.welcome_progress

import android.os.Bundle
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

/**
 * Created by vain onnellinen on 1/24/19.
 */
class WelcomeProgressFragment : BaseFragment<WelcomeProgressPresenter>(), WelcomeProgressContract.View {
    private lateinit var presenter: WelcomeProgressPresenter

    companion object {
        fun newInstance() = WelcomeProgressFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WelcomeProgressFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_progress
    override fun getToolbarTitle(): String? = ""

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomeProgressPresenter(this, WelcomeProgressRepository())
        return presenter
    }

    interface WelcomeProgressHandler {
        fun proceedToWallet()
    }
}
