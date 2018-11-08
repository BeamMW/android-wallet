package com.mw.beam.beamwallet.welcomeScreen.welcomeDescription

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import kotlinx.android.synthetic.main.fragment_welcome_description.*

/**
 * Created by vain onnellinen on 10/22/18.
 */
class WelcomeDescriptionFragment  : BaseFragment<WelcomeDescriptionPresenter>(), WelcomeDescriptionContract.View {
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

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_welcome_description, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = WelcomeDescriptionPresenter(this, WelcomeDescriptionRepository())
        configPresenter(presenter)

        btnGenerate.setOnClickListener {
            presenter.onGeneratePhrase()
        }
    }

    override fun generatePhrase() {
        (activity as GeneratePhraseHandler).generatePhrase()
    }

    interface GeneratePhraseHandler {
        fun generatePhrase()
    }
}

