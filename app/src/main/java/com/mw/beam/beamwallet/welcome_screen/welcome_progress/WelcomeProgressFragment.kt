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
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import kotlinx.android.synthetic.main.fragment_welcome_progress.*

/**
 * Created by vain onnellinen on 1/24/19.
 */
class WelcomeProgressFragment : BaseFragment<WelcomeProgressPresenter>(), WelcomeProgressContract.View {
    private lateinit var presenter: WelcomeProgressPresenter
    private var openTitleString: String = ""
    private var openDescriptionString: String = ""

    companion object {
        private const val ARG_MODE = "ARG_MODE"
        fun newInstance(mode: WelcomeMode) = WelcomeProgressFragment().apply { arguments = Bundle().apply { putString(ARG_MODE, mode.name) } }
        fun getFragmentTag(): String = WelcomeProgressFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_progress
    override fun getToolbarTitle(): String? = ""

    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)
        openTitleString = getString(R.string.welcome_progress_open)
        openDescriptionString = getString(R.string.welcome_progress_open_description)
    }

    override fun onControllerContentViewCreated() {
        super.onControllerContentViewCreated()
        title.text = openTitleString
    }

    override fun updateProgress(progressData: OnSyncProgressData, mode: WelcomeMode) {
        when (mode) {
            WelcomeMode.OPEN -> {
                description.text = String.format(openDescriptionString, progressData.done, progressData.total)
                description.visibility = View.VISIBLE
                progress.progress = countProgress(progressData)
            }
            WelcomeMode.CREATE, WelcomeMode.RESTORE -> {
            }
        }
    }

    private fun countProgress(progressData: OnSyncProgressData): Int {
        return progressData.done / progressData.total * 100

    }

    //TODO decide what should be by default (arguments == null), when all modes will be available
    override fun getMode(): WelcomeMode? = WelcomeMode.valueOf(arguments?.getString(ARG_MODE)
            ?: WelcomeMode.CREATE.name)

    override fun showWallet() = (activity as WelcomeProgressFragment.WelcomeProgressHandler).showWallet()

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomeProgressPresenter(this, WelcomeProgressRepository(), WelcomeProgressState())
        return presenter
    }

    interface WelcomeProgressHandler {
        fun showWallet()
    }
}
