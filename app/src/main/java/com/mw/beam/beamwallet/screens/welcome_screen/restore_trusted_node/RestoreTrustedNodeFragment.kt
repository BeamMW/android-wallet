package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import android.text.InputFilter
import android.text.Spanned
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import kotlinx.android.synthetic.main.fragment_restore_trusted_node.*

class RestoreTrustedNodeFragment : BaseFragment<RestoreTrustedNodePresenter>(), RestoreTrustedNodeContract.View {

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

    override fun addListeners() {
        btnNext.setOnClickListener {
            presenter?.onNextPressed()
        }
    }

    override fun clearListeners() {
        btnNext.setOnClickListener(null)
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