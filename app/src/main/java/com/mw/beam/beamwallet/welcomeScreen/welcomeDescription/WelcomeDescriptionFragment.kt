package com.mw.beam.beamwallet.welcomeScreen.welcomeDescription

import android.os.Bundle
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import kotlinx.android.synthetic.main.fragment_welcome_description.*

/**
 * Created by vain onnellinen on 10/22/18.
 */
class WelcomeDescriptionFragment : BaseFragment<WelcomeDescriptionPresenter>(), WelcomeDescriptionContract.View {
    private lateinit var presenter: WelcomeDescriptionPresenter

    companion object {
        fun newInstance(): WelcomeDescriptionFragment {
            val args = Bundle()
            val fragment = WelcomeDescriptionFragment()
            fragment.arguments = args

            return fragment
        }

        fun getFragmentTag(): String {
            return WelcomeDescriptionFragment::class.java.simpleName
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_description

    override fun onControllerStart() {
        super.onControllerStart()

        btnGenerate.setOnClickListener {
            presenter.onGeneratePhrase()
        }
    }

    override fun generatePhrase() = (activity as GeneratePhraseHandler).generatePhrase()

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = WelcomeDescriptionPresenter(this, WelcomeDescriptionRepository())
        return presenter
    }

    interface GeneratePhraseHandler {
        fun generatePhrase()
    }
}

