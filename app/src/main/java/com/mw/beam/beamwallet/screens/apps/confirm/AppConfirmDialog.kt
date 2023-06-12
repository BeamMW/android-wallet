package com.mw.beam.beamwallet.screens.apps.confirm

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.entities.DAOAmount
import com.mw.beam.beamwallet.core.entities.DAOApp
import com.mw.beam.beamwallet.core.entities.DAOInfo
import com.mw.beam.beamwallet.core.entities.dto.ContractConsentDTO
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.convertToAssetStringWithId
import com.mw.beam.beamwallet.core.helpers.convertToGroth
import com.mw.beam.beamwallet.core.helpers.exchangeValueAsset
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import com.mw.beam.beamwallet.screens.app_activity.AppActivity

import kotlinx.android.synthetic.main.dialog_app_confirm.*


class AppConfirmDialog: BaseDialogFragment<AppConfirmPresenter>(), AppConfirmContract.View {

    private var onConfirm: ((Boolean, String) -> Unit)? = null
    private lateinit var info:ContractConsentDTO
    private lateinit var app:DAOApp
    private var request = ""
    private var assetId = 0
    private var sendAmount = 0L
    private var sendFee = 0L
    private var isSpend = false
    private var amountsList = listOf<DAOAmount>()

    override var isMatchParent: Boolean
        get() = false
        set(value) {}

    companion object {
        fun getFragmentTag(): String = AppConfirmDialog::class.java.simpleName

        fun newInstance(info: ContractConsentDTO, app:DAOApp, onConfirm: (Boolean, String) -> Unit) = AppConfirmDialog().apply {
            this.onConfirm = onConfirm
            this.app = app
            this.info = info
            this.request = info.request
        }
    }

    var currentPassValue = ""
    private val currentPassWatcher = object : TextWatcher {
        override fun afterTextChanged(password: Editable?) {
            if (password.toString() != currentPassValue) {
                currentPassValue = password.toString()
                btnConfirm.isEnabled = true
                currentPass.isStateError = false
                currentPassError.visibility = View.GONE

                if(currentPass.isFocused) {
                    currentPass.isStateAccent = true
                }
                else if(currentPass.isFocused) {
                    currentPass.isStateNormal = true
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val color = if (App.isDarkMode) {
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark_dark)
        }
        else{
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        }
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        dialog.window?.statusBarColor = color
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AppConfirmPresenter(this, AppConfirmRepository())
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_app_confirm

    override fun init() {}

    override fun getToolbarTitle(): String {
        return getString(R.string.confirmation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarLayout.centerTitle = false
        toolbarLayout.leftTitleView.text = getToolbarTitle()
        toolbarLayout.canShowChangeButton = false
        toolbarLayout.changeNodeButton.alpha = 0f
        toolbarLayout.toolbar.setNavigationOnClickListener {
            dismiss()
            onConfirm?.invoke(false, request)
        }

        assetIdLayout1.setOnClickListener {
            openAssetId(amountsList[0].assetID ?: 0)
        }

        assetIdLayout2.setOnClickListener {
            openAssetId(amountsList[1].assetID ?: 0)
        }

        assetIdLayout3.setOnClickListener {
            openAssetId(amountsList[2].assetID ?: 0)
        }

        val gson = Gson()
        val amountToken: TypeToken<List<DAOAmount>> = object : TypeToken<List<DAOAmount>>() {}
        val infoToken: TypeToken<DAOInfo> = object : TypeToken<DAOInfo>() {}
        val amounts = gson.fromJson(info.amounts, amountToken.type) as List<DAOAmount>
        val details = gson.fromJson(info.info, infoToken.type) as DAOInfo

        val fee = (details.fee ?: "0").toDouble()
        val amount = (amounts[0].amount ?: "0").toDouble()
        val asset = amounts[0].assetID ?: 0
        val amountLong = amount.convertToGroth()
        val feeLong = fee.convertToGroth()
        val secondFeeValue = feeLong.exchangeValueAsset(0)

        assetId = asset
        sendAmount = amountLong
        sendFee = feeLong
        isSpend = details.isSpend ?: false

        amountsList = amounts
        setAmounts()


        feeLabel.text = feeLong.convertToAssetStringWithId(0)
        secondFeeLabel.text = secondFeeValue
        if (secondFeeValue.isEmpty()) {
            secondFeeLabel.visibility = View.GONE
        }

        if (details.isSpend == false) {
            btnConfirm.iconResId = R.drawable.ic_btn_receive
            btnConfirm.background = requireContext().getDrawable(R.drawable.receive_button)
            titleLabel.text = getString(R.string.withdraw_to_the_wallet)
            hintLabel.text = getString(R.string.will_send_funds, app.name ?: "")
        }
        else {
            btnConfirm.iconResId = R.drawable.ic_btn_send
            btnConfirm.background = requireContext().getDrawable(R.drawable.send_button)
            titleLabel.text = getString(R.string.deposit_to_the_wallet)
            hintLabel.text = getString(R.string.will_take_funds, app.name ?: "")
        }

        val isConfirm =  PreferencesManager.getBoolean(PreferencesManager.KEY_IS_SENDING_CONFIRM_ENABLED)

        btnCancel.setOnClickListener {
            dismiss()
            onConfirm?.invoke(false, request)
        }

        btnConfirm.setOnClickListener {
            if (isConfirm) {
                if (canSend()) {
                    if (checkAmount()) {
                        dismiss()
                        onConfirm?.invoke(true, request)
                    }
                }
            }
            else {
               if (checkAmount()) {
                    dismiss()
                    onConfirm?.invoke(true, request)
               }
            }
        }


        if (isConfirm) {
            btnConfirm.isEnabled = false
            passLayout.visibility = View.VISIBLE
            currentPass.setShowNeedPassEye()
            currentPass.addTextChangedListener(currentPassWatcher)
        }
        else {
            passLayout.visibility = View.GONE
        }

        if(isSpend) {
            checkAmount()
        }
    }

    private fun checkAmount():Boolean {
        if (isSpend) {
            var isOK = true
            amountsList.forEach { a->
                val assetId = a.assetID ?: 0
                val available = AssetManager.instance.getAvailable(assetId)
                val availableFee = AssetManager.instance.getAvailable(0)
                val amount = (a.amount ?: "0").toDouble()
                val amountLong = amount.convertToGroth()

                if (assetId == 0) {
                    val totalSend = sendFee + amountLong
                    if (available < totalSend) {
                        hintLabel.visibility = View.GONE
                        errorLabel.visibility = View.VISIBLE
                        btnConfirm.isEnabled = false
                        isOK = false
                    }
                }
                else {
                    when {
                        available < amountLong -> {
                            hintLabel.visibility = View.GONE
                            errorLabel.visibility = View.VISIBLE
                            btnConfirm.isEnabled = false
                            isOK = false
                        }
                        availableFee < sendFee -> {
                            hintLabel.visibility = View.GONE
                            errorLabel.visibility = View.VISIBLE
                            btnConfirm.isEnabled = false
                            isOK = false
                        }
                    }
                }
            }
            return isOK
        }
        else {
            return true
        }
    }

    override fun onControllerStart() {
        super.onControllerStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        if (App.isDarkMode)
        {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorPrimaryDark_dark, requireContext().theme)))
            view?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark_dark, requireContext().theme))
        }
        else{
            dialog?.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorPrimaryDark, requireContext().theme)))
            view?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark, requireContext().theme))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setAmounts() {
        var index = 0
        amountsList.forEach {
            val amount = (it.amount ?: "0").toDouble()
            val asset = it.assetID ?: 0
            val amountLong = amount.convertToGroth()
            val secondValue = amountLong.exchangeValueAsset(asset)
            var suffix = ""
            var color = requireContext().getColor(R.color.sent_color)
            if (it.spend == true) {
                color = requireContext().getColor(R.color.sent_color)
                suffix = "-"
            }
            else {
                color = requireContext().getColor(R.color.received_color)
                suffix = "+"
            }

            var amountLabel: TextView? = null
            var amountSecondLabel: TextView? = null
            var amountIdLabel: TextView? = null
            var assetIcon: ImageView? = null
            val assetValue = Asset(assetId)

            if (index == 0) {
                assetLayout1.visibility = View.VISIBLE
                amountLabel = amountLabel1
                amountSecondLabel = secondAmountLabel1
                amountIdLabel = assetIdLabel1
                assetIcon = assetIcon1
            }
            else if (index == 1) {
                assetLayout2.visibility = View.VISIBLE
                amountLabel = amountLabel2
                amountSecondLabel = secondAmountLabel2
                amountIdLabel = assetIdLabel2
                assetIcon = assetIcon2
            }
            else if (index == 2) {
                assetLayout3.visibility = View.VISIBLE
                amountLabel = amountLabel3
                amountSecondLabel = secondAmountLabel3
                amountIdLabel = assetIdLabel3
                assetIcon = assetIcon3
            }

            assetIcon?.setImageResource(AssetManager.instance.getImage(assetValue))
            amountIdLabel?.text = "#${asset}"
            amountLabel?.text = suffix + amountLong.convertToAssetStringWithId(asset)
            amountLabel?.setTextColor(color)
            amountSecondLabel?.text = secondValue
            if (secondValue.isEmpty()) {
                amountSecondLabel?.visibility = View.GONE
            }

            index += 1
        }

    }

    private fun canSend():Boolean {
        val isValid = AppManager.instance.wallet?.checkWalletPassword(currentPass.text.toString()) ?: false

        if (!isValid) {
            currentPassError.visibility = View.VISIBLE
            if (currentPass.text.isNullOrBlank()) {
                currentPassError.text = getString(R.string.password_can_not_be_empty)
            }
            else {
                currentPassError.text = getString(R.string.pass_wrong)
            }
            currentPass.isStateError = true
            btnConfirm.isEnabled = false
            return false
        }

        return true
    }

    private fun openAssetId(id: Int) {
        if (PreferencesManager.getBoolean(PreferencesManager.KEY_ALWAYS_OPEN_LINK)) {
            AppActivity.self.openExternalLink(AppConfig.buildAssetIdLink(id))
        } else {
            showAlert(
                getString(R.string.common_external_link_dialog_message),
                getString(R.string.open),
                {
                    AppActivity.self.openExternalLink(AppConfig.buildAssetIdLink(id))
                },
                getString(R.string.common_external_link_dialog_title),
                getString(R.string.cancel)
            )
        }
    }
}