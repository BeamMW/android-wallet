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
        fun newInstance() = WelcomeDescriptionFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WelcomeDescriptionFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_description

    override fun generatePhrase() = (activity as GeneratePhraseHandler).generatePhrase()

    override fun addListeners() {
        btnGenerate.setOnClickListener {
            presenter.onGeneratePhrase()
        }

        setTitle(getString(R.string.welcome_title))
    }

    override fun clearListeners() {
        btnGenerate.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = WelcomeDescriptionPresenter(this, WelcomeDescriptionRepository())
        return presenter
    }

    interface GeneratePhraseHandler {
        fun generatePhrase()
    }
}

