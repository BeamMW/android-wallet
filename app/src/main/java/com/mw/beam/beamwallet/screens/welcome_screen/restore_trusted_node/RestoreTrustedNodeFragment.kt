package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import kotlinx.android.synthetic.main.fragment_restore_trusted_node.*

class RestoreTrustedNodeFragment : BaseFragment<RestoreTrustedNodePresenter>(), RestoreTrustedNodeContract.View {

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            nodeAddress.isStateAccent = true
            errorText.visibility = View.GONE
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_restore_trusted_node

    override fun getToolbarTitle(): String? = getString(R.string.restore_wallet)

    override fun init() {
        nodeAddress.filters = Array<InputFilter>(1) {
            object : InputFilter {
                override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
                    if (source.isNotEmpty()) {
                        val regExp = "^([^:]*):?([1-9]|[1-8][0-9]|9[0-9]|[1-8][0-9]{2}|9[0-8][0-9]|99[0-9]|[1-8][0-9]{3}|9[0-8][0-9]{2}|99[0-8][0-9]|999[0-9]|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])?$".toRegex()
                        return if (!regExp.containsMatchIn(dest.toString().substring(0 until dstart) + source + dest.substring(dend until dest.length))) {
                            ""
                        } else {
                            null
                        }
                    }

                    return null
                }
            }
        }

        nodeAddress.requestFocus()
    }

    override fun showLoading() {
        content.visibility = View.GONE
        loading.visibility = View.VISIBLE
    }

    override fun dismissLoading() {
        content.visibility = View.VISIBLE
        loading.visibility = View.GONE
    }

    override fun showError() {
        nodeAddress.isStateError = true
        errorText.visibility = View.VISIBLE
    }

    override fun addListeners() {
        btnNext.setOnClickListener {
            presenter?.onNextPressed()
        }

        nodeAddress.addTextChangedListener(textWatcher)
    }

    override fun clearListeners() {
        btnNext.setOnClickListener(null)
        nodeAddress.removeTextChangedListener(textWatcher)
    }

    override fun navigateToProgress() {
        findNavController().navigate(RestoreTrustedNodeFragmentDirections.actionRestoreTustedNodeFragmentToWelcomeProgressFragment(null, WelcomeMode.OPEN.name, null))
    }

    override fun getNodeAddress(): String {
        return nodeAddress.text?.toString() ?: ""
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return RestoreTrustedNodePresenter(this, RestoreTrustedNodeRepository())
    }
}