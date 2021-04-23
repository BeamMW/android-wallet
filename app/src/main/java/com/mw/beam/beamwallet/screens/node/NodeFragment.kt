package com.mw.beam.beamwallet.screens.node

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE
import com.mw.beam.beamwallet.core.helpers.PreferencesManager.KEY_MOBILE_PROTOCOL
import com.mw.beam.beamwallet.core.helpers.PreferencesManager.KEY_NODE_ADDRESS
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.settings.SettingsFragmentArgs
import com.mw.beam.beamwallet.screens.settings.SettingsFragmentMode
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_progress.WelcomeProgressFragmentArgs


import kotlinx.android.synthetic.main.fragment_node.*
import kotlinx.android.synthetic.main.fragment_node.toolbarLayout

class NodeFragment: BaseFragment<NodePresenter>(), NodeContract.View {

    override fun getToolbarTitle(): String = getString(R.string.node)
    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_node
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
    }

    override fun configStatus(networkStatus: NetworkStatus) {
        super.configStatus(networkStatus)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarLayout.hasStatus = true
        toolbarLayout.changeNodeButton.alpha = 0f
        toolbarLayout.changeNodeButton.visibility = View.GONE
        toolbarLayout.changeNodeButton.isEnabled = false

        var okString = ""
        dialogNodeValue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {

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
                        allOK = when (port.toIntOrNull()) {
                            null -> false
                            in 1..65535 -> true
                            else -> false
                        }
                    }
                }

                if (!allOK) {
                    dialogNodeValue.setText(okString);
                    dialogNodeValue.setSelection(okString.length);
                }
                else{
                    okString = originalText
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        ownNodeTitle.text = getString(R.string.own_node_title).toUpperCase() + " (" + getString(R.string.fast_secure_advance).toLowerCase() + ")"
        mobileNodeTitle.text = getString(R.string.mobile_node_title).toUpperCase() + " (" + getString(R.string.slow_sync).toLowerCase() + ")"
        randomNodeTitle.text = getString(R.string.random_node_title).toUpperCase() + " (" + getString(R.string.fast_sync).toLowerCase() + ")"

        val mobile = PreferencesManager.getBoolean(KEY_MOBILE_PROTOCOL, false)
        val random = PreferencesManager.getBoolean(KEY_CONNECT_TO_RANDOM_NODE, false)
        val own = PreferencesManager.getString(KEY_NODE_ADDRESS) ?: ""
        dialogNodeValue.setText(own)

        if(mobile) {
            inputNodeLayout.visibility = View.GONE
            mobileNodeButton.isChecked = true
            ownNodeButton.isChecked = false
            randomButton.isChecked = false
        }
        else if (random) {
            inputNodeLayout.visibility = View.GONE
            randomButton.isChecked = true
            mobileNodeButton.isChecked = false
            ownNodeButton.isChecked = false
        }
        else {
            inputNodeLayout.visibility = View.VISIBLE
            ownNodeButton.isChecked = true
            mobileNodeButton.isChecked = false
            randomButton.isChecked = false
        }
    }

    override fun addListeners() {
        mobileNodeButton.setOnClickListener {
            mobileNodeButton.isChecked = true
            ownNodeButton.isChecked = false
            randomButton.isChecked = false
            inputNodeLayout.visibility = View.GONE
        }

        ownNodeButton.setOnClickListener {
            ownNodeButton.isChecked = true
            mobileNodeButton.isChecked = false
            randomButton.isChecked = false
            inputNodeLayout.visibility = View.VISIBLE
        }

        randomButton.setOnClickListener {
            randomButton.isChecked = true
            mobileNodeButton.isChecked = false
            ownNodeButton.isChecked = false
            inputNodeLayout.visibility = View.GONE
        }

        btnNext.setOnClickListener {
            hideKeyboard()
            if (randomButton.isChecked) {
                onRandomNode()
            }
            else if (mobileNodeButton.isChecked) {
                onMobileNode()
            }
            else if(ownNodeButton.isChecked) {
                val node = dialogNodeValue.text.toString()
                if(node.isEmpty() || !validateUrl()) {
                    showAlert(getString(R.string.settings_dialog_node_error),getString(R.string.ok), {

                    })
                }
                else {
                    onOwnNode()
                }
            }
        }
    }

    override fun clearListeners() {

    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return NodePresenter(this, NodeRepository(), NodeState())
    }

    private fun onOwnNode() {
        val node = dialogNodeValue.text.toString()

        PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS, node)
        PreferencesManager.putBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, false)

        AppManager.instance.wallet?.enableBodyRequests(false)

        AppManager.instance.onChangeNodeAddress()
        AppConfig.NODE_ADDRESS = node
        AppManager.instance.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)
    }

    private fun onRandomNode() {
        PreferencesManager.putBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true)

        AppManager.instance.wallet?.enableBodyRequests(false)

        AppManager.instance.onChangeNodeAddress()
        AppConfig.NODE_ADDRESS = Api.getDefaultPeers().random()
        AppManager.instance.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)
    }

    private fun onMobileNode() {
        PreferencesManager.putBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, true)
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true)

        AppManager.instance.wallet?.enableBodyRequests(true)

        AppManager.instance.onChangeNodeAddress()
        AppConfig.NODE_ADDRESS = Api.getDefaultPeers().random()
        AppManager.instance.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)
        findNavController().navigate(NodeFragmentDirections.actionNodeFragmentToWelcomeProgressFragment(null, WelcomeMode.MOBILE_CONNECT.name, null, false))

//        val destinationFragment = R.id.welcomeProgressFragment
//        val navBuilder = NavOptions.Builder()
//        val modeArg = WelcomeProgressFragmentArgs("", WelcomeMode.MOBILE_CONNECT.name,null,false)
//        val navigationOptions = navBuilder.setPopUpTo(destinationFragment, false).build()

      //  AppActivity.self.findNavController(R.id.nav_host).navigate(destinationFragment, modeArg.toBundle(), navigationOptions)
    }

    fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

    private fun validateUrl() : Boolean {
        val node = dialogNodeValue.text.toString()
        val url = "http://" + node
        val valid =  url.isValidUrl()
        return valid
    }
}