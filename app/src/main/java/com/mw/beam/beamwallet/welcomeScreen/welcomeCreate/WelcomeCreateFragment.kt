package com.mw.beam.beamwallet.welcomeScreen.welcomeCreate

import android.os.Bundle
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import kotlinx.android.synthetic.main.fragment_welcome_create.*

/**
 * Created by vain onnellinen on 12/4/18.
 */
class WelcomeCreateFragment : BaseFragment<WelcomeCreatePresenter>(), WelcomeCreateContract.View {
    private lateinit var presenter: WelcomeCreatePresenter

    companion object {
        fun newInstance() = WelcomeCreateFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WelcomeCreateFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_create
    override fun getToolbarTitle(): String? = ""

    override fun addListeners() {
        btnCreate.setOnClickListener {
            presenter.onCreateWallet()
        }

        btnRestore.setOnClickListener {
            presenter.onRestoreWallet()
        }
    }

    override fun clearListeners() {
        btnCreate.setOnClickListener(null)
        btnRestore.setOnClickListener(null)
    }

    override fun createWallet() = (activity as WelcomeCreateHandler).createWallet()
    override fun restoreWallet() = (activity as WelcomeCreateHandler).restoreWallet()

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomeCreatePresenter(this, WelcomeCreateRepository())
        return presenter
    }

    interface WelcomeCreateHandler {
        fun createWallet()
        fun restoreWallet()
    }
}

