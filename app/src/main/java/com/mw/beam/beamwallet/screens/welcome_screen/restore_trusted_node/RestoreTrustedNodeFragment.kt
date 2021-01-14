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
import android.util.Patterns

class RestoreTrustedNodeFragment : BaseFragment<RestoreTrustedNodePresenter>(), RestoreTrustedNodeContract.View {

    private var okString = ""
    private var isAlreadyNavigated = false

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            nodeAddress.isStateAccent = true
            errorText.visibility = View.GONE

            val originalText = editable.toString()

            var allOK = true;

            val array = originalText.toCharArray().filter {
                it.equals(':',true)
            }

            if (array.count() > 1) {
                allOK = false
            }
            else if (array.count()==1) {
                val port = originalText.split(":").lastOrNull()
                if (!port.isNullOrEmpty())
                {
                    val num = port?.toIntOrNull()
                    if (num==null) {
                        allOK = false
                    }
                    else if (num in 1..65535){
                        allOK = true
                    }
                    else{
                        allOK = false
                    }
                }
            }

            if (!allOK) {
                nodeAddress.setText(okString);
                nodeAddress.setSelection(okString.length);
            }
            else{
                okString = originalText
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_restore_trusted_node

    override fun getToolbarTitle(): String? = getString(R.string.restore_wallet)

    override fun init() {
        nodeAddress.requestFocus()
    }

    override fun showLoading() {
        nodeAddress.clearFocus()
        hideKeyboard()
        content.visibility = View.GONE
        loading.visibility = View.VISIBLE
    }

    override fun dismissLoading() {
        content.visibility = View.VISIBLE
        loading.visibility = View.GONE
    }

    override fun showError() {
        if (getNodeAddress().isNullOrEmpty() || !validateUrl()) {
            errorText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            errorText.text = getString(R.string.settings_dialog_node_error)
        }
        else{
            errorText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            errorText.text = getString(R.string.node_isn_t_connectable_try_connect_wallet_to_different_node_or_use_automatic_restore)
        }

        nodeAddress.isStateError = true
        errorText.visibility = View.VISIBLE
    }

    fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

    private fun validateUrl() : Boolean {
        val url = "http://" + getNodeAddress()
        val valid =  url.isValidUrl()
        return valid
    }

    override fun addListeners() {
        btnNext.setOnClickListener {
            if (getNodeAddress().isNullOrEmpty()) {
                showError()
            }
            else if (!validateUrl()) {
                showError()
            }
            else{
                presenter?.onNextPressed()
            }
        }

        nodeAddress.addTextChangedListener(textWatcher)
    }

    override fun clearListeners() {
        btnNext.setOnClickListener(null)
        nodeAddress.removeTextChangedListener(textWatcher)
    }

    override fun navigateToProgress() {
//        val fragment = WelcomeProgressFragment()
//        activity?.supportFragmentManager?.beginTransaction()
//                ?.replace(R.id.nav_host, fragment)
//                ?.commit()
        if (!isAlreadyNavigated) {
            isAlreadyNavigated = true
            findNavController().navigate(RestoreTrustedNodeFragmentDirections.actionRestoreTustedNodeFragmentToWelcomeProgressFragment(null, WelcomeMode.OPEN.name, null, true))
        }
    }

    override fun getNodeAddress(): String {
        return nodeAddress.text?.toString() ?: ""
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return RestoreTrustedNodePresenter(this, RestoreTrustedNodeRepository())
    }
}