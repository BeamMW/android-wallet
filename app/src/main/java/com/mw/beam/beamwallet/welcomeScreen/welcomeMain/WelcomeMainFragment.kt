package com.mw.beam.beamwallet.welcomeScreen.welcomeMain

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_welcome_main.*

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomeMainFragment : BaseFragment<WelcomeMainPresenter>(), WelcomeMainContract.View {
    private lateinit var presenter: WelcomeMainPresenter

    companion object {
        fun newInstance(): WelcomeMainFragment {
            val args = Bundle()
            val fragment = WelcomeMainFragment()
            fragment.arguments = args

            return fragment
        }

        fun getFragmentTag(): String {
            return WelcomeMainFragment::class.java.simpleName
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_welcome_main, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = WelcomeMainPresenter(this, WelcomeMainRepository())
        configPresenter(presenter)

        btnCreate.setOnClickListener {
            presenter.onCreateWallet()
        }

        btnRestore.setOnClickListener {
            presenter.onRestoreWallet()
        }

        btnOpen.setOnClickListener {
            presenter.onOpenWallet()
        }

        btnChange.setOnClickListener {
            presenter.onChangeWallet()
        }

        pass.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                clearError()
            }
        })
    }

    override fun configScreen(isWalletInitialized: Boolean) {
        btnCreate.visibility = if (isWalletInitialized) View.GONE else View.VISIBLE
        btnRestore.visibility = if (isWalletInitialized) View.GONE else View.VISIBLE
        btnOpen.visibility = if (isWalletInitialized) View.VISIBLE else View.GONE
        btnChange.visibility = if (isWalletInitialized) View.VISIBLE else View.GONE
        passTitle.visibility = if (isWalletInitialized) View.VISIBLE else View.GONE
        pass.visibility = if (isWalletInitialized) View.VISIBLE else View.GONE
    }

    override fun hasValidPass(): Boolean {
        val context = context ?: return false
        var hasErrors = false
        passError.visibility = View.INVISIBLE
        pass.setTextColor(ContextCompat.getColor(context, R.color.common_text_color))

        if (pass.text.isNullOrBlank()) {
            passError.text = getString(R.string.welcome_pass_empty_error)
            passError.visibility = View.VISIBLE
            pass.setTextColor(ContextCompat.getColor(context, R.color.common_error_color))
            hasErrors = true
        }

        return !hasErrors
    }

    override fun clearError() {
        val context = context ?: return
        passError.visibility = View.INVISIBLE
        pass.setTextColor(ContextCompat.getColor(context, R.color.common_text_color))
    }

    override fun getPass(): String = pass.text.trim().toString()
    override fun createWallet() = (activity as WelcomeMainHandler).createWallet()

    override fun openWallet() {
        (activity as WelcomeMainHandler).openWallet()
    }

    override fun showOpenWalletError() {
        val context = context ?: return
        pass.setTextColor(ContextCompat.getColor(context, R.color.common_error_color))
        passError.text = getString(R.string.welcome_pass_wrong)
        passError.visibility = View.VISIBLE
    }

    override fun showChangeAlert() {
        showAlert(getString(R.string.welcome_change_alert), R.string.welcome_change, R.drawable.ic_btn_change_dark) { presenter.onChangeConfirm() }
    }

    override fun restoreWallet() {
        (activity as WelcomeMainHandler).recoverWallet()
    }

    interface WelcomeMainHandler {
        fun createWallet()
        fun openWallet()
        fun recoverWallet()
    }
}
