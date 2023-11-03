package com.mw.beam.beamwallet.screens.node

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import androidx.core.content.ContextCompat
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


import kotlinx.android.synthetic.main.fragment_node.*
import kotlinx.android.synthetic.main.fragment_node.toolbarLayout

class NodeFragment: BaseFragment<NodePresenter>(), NodeContract.View {


    var isNeedDisconnect = false

    private fun isCreate(): Boolean? = arguments?.let { NodeFragmentArgs.fromBundle(it).iscreate }
    private fun password(): String? = arguments?.let { NodeFragmentArgs.fromBundle(it).password }
    private fun seed(): Array<String>? = arguments?.let { NodeFragmentArgs.fromBundle(it).seed }

    override fun getToolbarTitle(): String = getString(R.string.node)
    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_node
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
    }

    override fun configStatus(networkStatus: NetworkStatus) {
        toolbarLayout.canShowChangeButton = false
        super.configStatus(networkStatus)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarLayout.hasStatus = isCreate() != true
        toolbarLayout.changeNodeButton.alpha = 0f
        toolbarLayout.changeNodeButton.visibility = View.GONE
        toolbarLayout.changeNodeButton.isEnabled = false
        toolbarLayout.canShowChangeButton = false

        if (isCreate() == true) {
            btnNext.textResId = R.string.pass_proceed_to_wallet
            btnNext.iconResId = R.drawable.ic_btn_proceed
        }
        else {
            btnNext.textResId = R.string.proceed
            btnNext.iconResId = R.drawable.ic_btn_proceed
            btnNext.visibility = View.GONE
        }

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


        if (isCreate() == true) {
            dialogNodeValue.setText("")
        }
        else {
            isNeedDisconnect = true

            val own = PreferencesManager.getString(KEY_NODE_ADDRESS) ?: ""
            dialogNodeValue.setText(own)
        }

        checkCurrentNode()
        checkEnables()
    }

    private fun checkCurrentNode() {
        val mobile = PreferencesManager.getBoolean(KEY_MOBILE_PROTOCOL, false)
        val random = PreferencesManager.getBoolean(KEY_CONNECT_TO_RANDOM_NODE, false)

        when {
            mobile -> {
                if (isCreate() == false) {
                    btnNext.visibility = View.GONE
                }
                inputNodeLayout.visibility = View.GONE
                mobileNodeButton.isChecked = true
                ownNodeButton.isChecked = false
                randomButton.isChecked = false
            }
            random -> {
                if (isCreate() == false) {
                    btnNext.visibility = View.GONE
                }
                inputNodeLayout.visibility = View.GONE
                randomButton.isChecked = true
                mobileNodeButton.isChecked = false
                ownNodeButton.isChecked = false
            }
            else -> {
                btnNext.visibility = View.VISIBLE
                inputNodeLayout.visibility = View.VISIBLE
                ownNodeButton.isChecked = true
                mobileNodeButton.isChecked = false
                randomButton.isChecked = false
            }
        }

        ownNodeButton.jumpDrawablesToCurrentState()
        mobileNodeButton.jumpDrawablesToCurrentState()
        randomButton.jumpDrawablesToCurrentState()
    }

    private fun checkEnables() {
        ownLayout.background = null
        mobileLayout.background = null
        randomLayout.background = null

        randomLayout.alpha = 1f
        ownLayout.alpha = 1f
        mobileLayout.alpha = 1f

        randomButton.isEnabled = true
        ownNodeButton.isEnabled = true
        mobileNodeButton.isEnabled = true
        dialogNodeValue.isEnabled = true
        dialogNodeValue.setTextColor(requireContext().getColor(R.color.white_100))

        inputNodeLayout.background =  ContextCompat.getDrawable(requireContext(), R.drawable.wallet_state_card_backgroud)
        inputNodeLayout.alpha = 1f

        when {
            mobileNodeButton.isChecked -> {
                ownLayout.background = null
                mobileLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.wallet_state_card_plain)
                randomLayout.background = null
            }
            randomButton.isChecked  -> {
                ownLayout.background = null
                mobileLayout.background = null
                randomLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.wallet_state_card_plain)
            }
            else -> {
                ownLayout.background =  ContextCompat.getDrawable(requireContext(), R.drawable.wallet_state_card_plain)
                mobileLayout.background = null
                randomLayout.background = null
            }
        }
    }

    override fun addListeners() {
        mobileLayout.setOnClickListener {
            clickMobile()
        }

        ownLayout.setOnClickListener {
           clickOwn()
        }

        randomLayout.setOnClickListener {
            clickRandom()
        }

        mobileNodeButton.setOnClickListener {
            clickMobile()
        }

        ownNodeButton.setOnClickListener {
            clickOwn()
        }

        randomButton.setOnClickListener {
            clickRandom()
        }

        btnNext.setOnClickListener {
            onNext()
        }
    }

    override fun clearListeners() {

    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return NodePresenter(this, NodeRepository(), NodeState())
    }

    private fun onNext() {
        hideKeyboard()

        if (randomButton.isChecked) {
            onRandomNode()
        }
        else if (mobileNodeButton.isChecked) {
            onMobileNode()
        }
        else if(ownNodeButton.isChecked) {
            val node = dialogNodeValue.text.toString()
            if(node.isEmpty()) {
                showAlert(getString(R.string.please_enter_node_address),getString(R.string.ok), {

                })
            }
            else if(!validateUrl()) {
                showAlert(getString(R.string.settings_dialog_node_error),getString(R.string.ok), {

                })
            }
            else {
                if (isCreate() == true) {
                    onOwnNode()
                }
                else {
                    val mobile = PreferencesManager.getBoolean(KEY_MOBILE_PROTOCOL, false)
                    val random = PreferencesManager.getBoolean(KEY_CONNECT_TO_RANDOM_NODE, false)

                    when {
                        mobile -> {
                            showAlert(getString(R.string.switch_to_own_from_mobile, node), getString(R.string.sw), {
                                mobileNodeButton.isChecked = false
                                ownNodeButton.isChecked = true
                                randomButton.isChecked = false
                                onOwnNode()
                            }, getString(R.string.switch_to_own), getString(R.string.cancel), {
                                checkCurrentNode()
                                checkEnables()
                            })
                        }
                        random -> {
                            showAlert(getString(R.string.switch_to_own_from_random, node), getString(R.string.sw), {
                                mobileNodeButton.isChecked = false
                                ownNodeButton.isChecked = true
                                randomButton.isChecked = false
                                onOwnNode()
                            }, getString(R.string.switch_to_own), getString(R.string.cancel), {
                                checkCurrentNode()
                                checkEnables()
                            })
                        }
                        node != AppConfig.NODE_ADDRESS -> {
                            showAlert(getString(R.string.switch_to_own_from_own, AppConfig.NODE_ADDRESS, node), getString(R.string.sw), {
                                mobileNodeButton.isChecked = false
                                ownNodeButton.isChecked = true
                                randomButton.isChecked = false
                                onOwnNode()
                            }, getString(R.string.switch_to_own), getString(R.string.cancel), {
                                checkCurrentNode()
                                checkEnables()
                            })
                        }
                        else -> {
                            mobileNodeButton.isChecked = false
                            ownNodeButton.isChecked = true
                            randomButton.isChecked = false
                            onOwnNode()
                        }
                    }
                }
            }
        }
    }

    private fun clickMobile() {
        if (isCreate() == false) {
            val own = PreferencesManager.getString(KEY_NODE_ADDRESS) ?: ""
            val random = PreferencesManager.getBoolean(KEY_CONNECT_TO_RANDOM_NODE, false)
            val isMobile = PreferencesManager.getBoolean(KEY_MOBILE_PROTOCOL, false)

            if (random && !isMobile) {
                showAlert(getString(R.string.switch_to_mobile_from_random), getString(R.string.sw), {
                    mobileNodeButton.isChecked = true
                    ownNodeButton.isChecked = false
                    randomButton.isChecked = false
                    onNext()
                }, getString(R.string.switch_to_mobile), getString(R.string.cancel), {
                    checkCurrentNode()
                    checkEnables()
                })
            }
            else if (ownNodeButton.isChecked && !random && own.isNotEmpty()) {
                showAlert(getString(R.string.switch_to_mobile_from_own, own), getString(R.string.sw), {
                    mobileNodeButton.isChecked = true
                    ownNodeButton.isChecked = false
                    randomButton.isChecked = false
                    onNext()
                }, getString(R.string.switch_to_mobile), getString(R.string.cancel), {
                    checkCurrentNode()
                    checkEnables()
                })
            }
            else if (!mobileNodeButton.isChecked) {
                mobileNodeButton.isChecked = true
                ownNodeButton.isChecked = false
                randomButton.isChecked = false
                checkCurrentNode()
                checkEnables()
            }
        }
        else {
            mobileNodeButton.isChecked = true
            ownNodeButton.isChecked = false
            randomButton.isChecked = false
            checkEnables()
        }
    }

    private fun clickOwn() {
        ownNodeButton.isChecked = true
        mobileNodeButton.isChecked = false
        randomButton.isChecked = false
        inputNodeLayout.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        checkEnables()
    }

    private fun clickRandom() {
        if (isNeedDisconnect) {
            val own = PreferencesManager.getString(KEY_NODE_ADDRESS) ?: ""
            val random = PreferencesManager.getBoolean(KEY_CONNECT_TO_RANDOM_NODE, false)
            val mobile = PreferencesManager.getBoolean(KEY_MOBILE_PROTOCOL, false)

            if (mobile) {
                showAlert(getString(R.string.switch_to_random_from_mobile), getString(R.string.sw), {
                    mobileNodeButton.isChecked = false
                    ownNodeButton.isChecked = false
                    randomButton.isChecked = true
                    onNext()
                    checkEnables()
                }, getString(R.string.switch_to_random), getString(R.string.cancel), {
                    checkCurrentNode()
                    checkEnables()
                })
            }
            else if (ownNodeButton.isChecked && !random && own.isNotEmpty()) {
                showAlert(getString(R.string.switch_to_random_from_own, own), getString(R.string.sw), {
                    mobileNodeButton.isChecked = false
                    ownNodeButton.isChecked = false
                    randomButton.isChecked = true
                    inputNodeLayout.visibility = View.GONE
                    btnNext.visibility = View.GONE
                    onNext()
                    checkEnables()
                }, getString(R.string.switch_to_random), getString(R.string.cancel), {
                    checkCurrentNode()
                    checkEnables()
                })
            }
            else if (!randomButton.isChecked) {
                mobileNodeButton.isChecked = false
                ownNodeButton.isChecked = false
                randomButton.isChecked = true
                inputNodeLayout.visibility = View.GONE
                btnNext.visibility = View.GONE
                onNext()
                checkEnables()
            }
        }
        else {
            mobileNodeButton.isChecked = false
            ownNodeButton.isChecked = false
            randomButton.isChecked = true
            checkEnables()
        }
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

        if (isCreate() == true) {
            findNavController().navigate(NodeFragmentDirections.actionNodeFragmentToWelcomeProgressFragment(password(),WelcomeMode.CREATE.name, seed(), false))
        }
        else {
            findNavController().navigate(NodeFragmentDirections.actionNodeFragmentToWelcomeProgressFragment(null, WelcomeMode.MOBILE_CONNECT.name, null, false))
        }
    }

    private fun onRandomNode() {
        PreferencesManager.putBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true)

        AppManager.instance.wallet?.enableBodyRequests(false)
        AppManager.instance.onChangeNodeAddress()
        AppConfig.NODE_ADDRESS = AppManager.getNode()
        AppManager.instance.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)

        if (isCreate() == true) {
            PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS, "")

            findNavController().navigate(NodeFragmentDirections.actionNodeFragmentToWelcomeProgressFragment(password(),WelcomeMode.CREATE.name, seed(), false))
        }
    }

    private fun onMobileNode() {
        PreferencesManager.putBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, true)
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true)

        AppManager.instance.wallet?.enableBodyRequests(true)

        AppManager.instance.onChangeNodeAddress()
        AppConfig.NODE_ADDRESS = AppManager.getNode()
        AppManager.instance.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)

        if (isCreate() == true) {
            PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS, "")
            findNavController().navigate(NodeFragmentDirections.actionNodeFragmentToWelcomeProgressFragment(password(),WelcomeMode.CREATE.name, seed(), false))
        }
        else {
            findNavController().navigate(NodeFragmentDirections.actionNodeFragmentToWelcomeProgressFragment(null, WelcomeMode.MOBILE_CONNECT.name, null, false))
        }
    }

    private fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

    private fun validateUrl() : Boolean {
        val node = dialogNodeValue.text.toString()
        val url = "http://$node"
        return url.isValidUrl()
    }
}