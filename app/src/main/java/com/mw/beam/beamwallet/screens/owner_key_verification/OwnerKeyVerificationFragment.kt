package com.mw.beam.beamwallet.screens.owner_key_verification

import android.text.Editable
import android.view.View
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import com.mw.beam.beamwallet.screens.fingerprint_dialog.FingerprintDialog
import kotlinx.android.synthetic.main.fragment_owner_key_verification.*

class OwnerKeyVerificationFragment: BaseFragment<OwnerKeyVerificationPresenter>(), OwnerKeyVerificationContract.View {
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(password: Editable?) {
            presenter?.onChangePassword()
        }
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_owner_key_verification

    override fun getToolbarTitle(): String? = getString(R.string.show_owner_key)

    override fun init(isEnableFingerprint: Boolean) {
        enterPasswordTitle.setText(if (isEnableFingerprint) R.string.owner_key_verification_title_with_finger else R.string.enter_your_current_password)
        verificationDescription.visibility = if (isEnableFingerprint) View.VISIBLE else View.GONE
    }

    override fun addListeners() {
        btnNext.setOnClickListener {
            presenter?.onNext()
        }

        pass.addTextChangedListener(passWatcher)
    }

    override fun clearListeners() {
        pass.removeTextChangedListener(passWatcher)
    }

    override fun getPassword(): String = pass.text?.toString() ?: ""

    override fun showEmptyPasswordError() {
        passError.visibility = View.VISIBLE
        passError.text = getString(R.string.password_can_not_be_empty)
        pass.isStateError = true
    }

    override fun showWrongPasswordError() {
        passError.visibility = View.VISIBLE
        passError.text = getString(R.string.current_password_is_incorrect)
        pass.isStateError = true
    }

    override fun clearPasswordError() {
        passError.visibility = View.GONE
        pass.isStateAccent = true
    }

    override fun showFingerprintDescription() {
        fingerprintDescription.visibility = View.VISIBLE
    }

    override fun hideFingerprintDescription() {
        fingerprintDescription.visibility = View.GONE
    }

    override fun showErrorFingerprintMessage() {
        showSnackBar(getString(R.string.owner_key_verification_fingerprint_error))
    }

    override fun showFingerprintDialog() {
        FingerprintDialog.show(childFragmentManager, { presenter?.onFingerprintSuccess() }, { presenter?.onCancelFingerprintDialog() }, { presenter?.onFingerprintError() })
    }

    override fun navigateToOwnerKey() {
        findNavController().navigate(OwnerKeyVerificationFragmentDirections.actionOwnerKeyVerificationFragmentToOwnerKeyFragment())
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return OwnerKeyVerificationPresenter(this, OwnerKeyVerificationRepository())
    }
}