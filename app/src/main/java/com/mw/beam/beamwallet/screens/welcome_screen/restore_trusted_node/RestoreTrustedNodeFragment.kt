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

    private var okString = ""

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
                nodeAddress.setSelection(okString.length-1);
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