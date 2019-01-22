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

package com.mw.beam.beamwallet.welcomeScreen.welcomeDescription

import android.os.Bundle
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import kotlinx.android.synthetic.main.fragment_welcome_description.*

/**
 * Created by vain onnellinen on 10/22/18.
 */
class WelcomeDescriptionFragment : BaseFragment<WelcomeDescriptionPresenter>(), WelcomeDescriptionContract.View {
    private lateinit var presenter: WelcomeDescriptionPresenter

    companion object {
        fun newInstance() = WelcomeDescriptionFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WelcomeDescriptionFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_description
    override fun getToolbarTitle(): String? = getString(R.string.welcome_title)

    override fun generatePhrase() = (activity as GeneratePhraseHandler).generatePhrase()

    override fun addListeners() {
        btnGenerate.setOnClickListener {
            presenter.onGeneratePhrase()
        }
    }

    override fun clearListeners() {
        btnGenerate.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomeDescriptionPresenter(this, WelcomeDescriptionRepository())
        return presenter
    }

    interface GeneratePhraseHandler {
        fun generatePhrase()
    }
}

