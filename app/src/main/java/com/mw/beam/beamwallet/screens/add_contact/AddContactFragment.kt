package com.mw.beam.beamwallet.screens.add_contact

import android.content.Intent
import android.text.*
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.QrHelper
import com.mw.beam.beamwallet.screens.qr.ScanQrActivity
import kotlinx.android.synthetic.main.fragment_add_contact.*
import com.mw.beam.beamwallet.core.App

class AddContactFragment : BaseFragment<AddContactPresenter>(), AddContactContract.View {
    private val tokenWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter?.onTokenChanged()
            if (!s.isNullOrBlank()) {
                if(s.contains(QrHelper.BEAM_URI_PREFIX)) {
                    val validAddress = s.replace(QrHelper.tokenRegex, "")

                    if (validAddress != s.toString()) {
                        address.setText("")
                        address.append(validAddress)
                    }
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onStart() {
        super.onStart()

        toolbarLayout.hasStatus = true
    }

    override fun getAddress(): String {
        return address.text?.toString() ?: ""
    }

    override fun getName(): String {
        return name.text?.toString() ?: ""
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_add_contact

    override fun getToolbarTitle(): String? = getString(R.string.add_contact)

    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
}
else{
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
}

    override fun addListeners() {
        btnCancel.setOnClickListener {
            presenter?.onCancelPressed()
        }

        btnSave.setOnClickListener {
            presenter?.onSavePressed()
        }

        scanQR.setOnClickListener {
            presenter?.onScanPressed()
        }

        address.addTextChangedListener(tokenWatcher)
        address.imeOptions = EditorInfo.IME_ACTION_DONE
        address.setRawInputType(InputType.TYPE_CLASS_TEXT)

        address.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                presenter?.checkAddress()
            }
        }
    }

    override fun onHideKeyboard() {
        super.onHideKeyboard()

        presenter?.checkAddress()
    }

    override fun setAddress(address: String) {
        this.address.setText(address)
    }

    override fun navigateToScanQr() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.captureActivity = ScanQrActivity::class.java
        integrator.setBeepEnabled(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.onScannedQR(IntentIntegrator.parseActivityResult(resultCode, data).contents)
    }

    override fun showTokenError(address: WalletAddress?) {
        if (address != null) {
            if (address?.isContact) {
                tokenError.text = getString(R.string.address_already_exist_1)
            } else{
                tokenError.text = getString(R.string.address_already_exist_2)
            }
        }
        else{
            tokenError.text = getString(R.string.invalid_address)
        }

        tokenError.visibility = View.VISIBLE
    }

    override fun showErrorNotBeamAddress() {
        showSnackBar(getString(R.string.send_error_not_beam_address))
    }

    override fun hideTokenError() {
        tokenError.visibility = View.INVISIBLE
    }

    override fun getAddressFromArguments(): String? {
        if (arguments!=null)
        {
            return AddContactFragmentArgs.fromBundle(requireArguments()).address
        }

        return null
    }

    override fun clearListeners() {
        btnCancel.setOnClickListener(null)
        btnSave.setOnClickListener(null)
        scanQR.setOnClickListener(null)
        tagAction.setOnClickListener(null)
        address.removeTextChangedListener(tokenWatcher)
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AddContactPresenter(this, AddContactRepository(), AddContactState())
    }
}