/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.views.CategoryItemView
import com.mw.beam.beamwallet.core.views.addDoubleDots
import com.mw.beam.beamwallet.screens.settings.password_dialog.PasswordConfirmDialog
import kotlinx.android.synthetic.main.dialog_clear_data.view.*
import kotlinx.android.synthetic.main.dialog_lock_screen_settings.view.*
import kotlinx.android.synthetic.main.dialog_node_address.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.File
import java.util.concurrent.TimeUnit
import android.graphics.*
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.content.res.*
import androidx.activity.OnBackPressedCallback
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import android.os.Build
import com.mw.beam.beamwallet.core.OnboardManager


/**
 *  1/21/19.
 */
class SettingsFragment : BaseFragment<SettingsPresenter>(), SettingsContract.View {
    override fun onControllerGetContentLayoutId() = R.layout.fragment_settings
    override fun getToolbarTitle(): String? = getString(R.string.settings)

    private var isShareLogs = false

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showWalletFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)
    }

    override fun init(runOnRandomNode: Boolean) {
        appVersionTitle.addDoubleDots()
        appVersionValue.text = BuildConfig.VERSION_NAME
        runRandomNodeSwitch.isChecked = runOnRandomNode
        ip.text = AppConfig.NODE_ADDRESS


        if(!runOnRandomNode)
        {
            ip.setTextColor(resources.getColor(R.color.btn_drop_down_color))
            ipTitle.setTextColor(resources.getColor(R.color.btn_drop_down_color))
            ipTitle.setPadding(0,0,0,0)

            if(this.context != null)
            {
                val typeFace: Typeface? = ResourcesCompat.getFont(this.context!!, R.font.roboto_bold)
                ipTitle.typeface = typeFace
            }

            ip.setPadding(5,0,5,0)
            ipportLayout.orientation = android.widget.LinearLayout.HORIZONTAL
            runRandomNodeSwitch.setPadding(0,30,0,0)
        }
        else{
            ip.setTextColor(resources.getColor(R.color.btn_drop_down_color))
            ipTitle.setTextColor(resources.getColor(R.color.common_text_color))
            ipTitle.setPadding(0,20,0,0)

            if(this.context != null)
            {
                val typeFace: Typeface? = ResourcesCompat.getFont(this.context!!, R.font.roboto_regular)
                ipTitle.typeface = typeFace
            }

            runRandomNodeSwitch.setPadding(0,0,0,0)

            ip.setPadding(0,0,0,0)
            ipportLayout.orientation = android.widget.LinearLayout.VERTICAL
        }

        (activity as? AppActivity)?.enableLeftMenu(true)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as? AppActivity)?.openMenu()
        }

        if (OnboardManager.instance.getSeed().isNullOrEmpty()) {
            verificationFrame.visibility = View.GONE
            seedFrame.visibility = View.GONE
        }
        else if (!OnboardManager.instance.canMakeSecure()) {
            verificationFrame.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        onBackPressedCallback.isEnabled = true
    }

    override fun onStop() {
        onBackPressedCallback.isEnabled = false
        super.onStop()
    }

    override fun onDestroy() {
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    override fun getStatusBarColor(): Int = ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)

    override fun setLogSettings(days: Long) {
        logsValue.text =  when (days) {
            0L ->  getString(R.string.all_time)
            5L ->  getString(R.string.last_5_days)
            15L ->  getString(R.string.last_15_days)
            30L ->  getString(R.string.last_30_days)
            else -> ""
        }
    }

    override fun setAllowOpenExternalLinkValue(allowOpen: Boolean) {
        allowOpenLinkSwitch.isChecked = allowOpen
    }

    override fun showFingerprintSettings(isFingerprintEnabled: Boolean) {
        enableFingerprintLayout.visibility = View.VISIBLE
        enableFingerprintSwitch.isChecked = isFingerprintEnabled
    }

    override fun navigateToAddCategory() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToEditCategoryFragment(null))
    }

    override fun navigateToCategory(categoryId: String) {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToCategoryFragment(categoryId))
    }

    override fun navigateToLanguage() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLanguageFragment())
    }

    override fun navigateToSeed() {
        PasswordConfirmDialog.newInstance(getString(R.string.enter_your_password), {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToWelcomeSeedFragment())
        }, {

        }).show(activity?.supportFragmentManager!!, PasswordConfirmDialog.getFragmentTag())
    }

    override fun showReceiveFaucet() {
        val allow = PreferencesManager.getBoolean(PreferencesManager.KEY_ALWAYS_OPEN_LINK)

        if (allow) {
            presenter?.generateFaucetAddress()
        }
        else{
            showAlert(
                    getString(R.string.common_external_link_dialog_message),
                    getString(R.string.open),
                    {  presenter?.generateFaucetAddress() },
                    getString(R.string.common_external_link_dialog_title),
                    getString(R.string.cancel)
            )
        }
    }

    override fun onFaucetAddressGenerated(link: String) {
        openExternalLink(link)
    }

    private fun isEnableFingerprint(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED) &&
                FingerprintManager.SensorState.READY == FingerprintManager.checkSensorState(view?.getContext()
                ?: return false)
    }

    override fun navigateToOwnerKeyVerification() {
        val message = if (isEnableFingerprint()) getString(R.string.owner_key_verification_pass_finger) else getString(R.string.owner_key_verification_pass)

        showAlert(message = message,
                btnConfirmText = getString(R.string.ok),
                onConfirm = { findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToOwnerKeyVerificationFragment()) },
                title = getString(R.string.owner_key),
                btnCancelText = null,
                onCancel = {  })
    }

    override fun updateCategoryList(allTag: List<Tag>) {
        categoriesList.removeAllViews()

        allTag.forEach { category ->
            categoriesList.addView(CategoryItemView(context!!).apply {
                colorResId = category.color.getAndroidColorId()
                text = category.name
                setOnClickListener { presenter?.onCategoryPressed(category.id) }
            })
        }

        when {
            allTag.count() == 0 -> categoriesList.visibility = View.GONE
            else -> categoriesList.visibility = View.VISIBLE
        }
    }

    override fun setLanguage(language: LocaleHelper.SupportedLanguage) {
        languageValue.text = language.nativeName
    }

    override fun sendMailWithLogs() {
        if (!isShareLogs)
        {
            isShareLogs = true

            doAsync {
                ZipManager.zip(AppConfig.LOG_PATH, AppConfig.ZIP_PATH);

                uiThread {

                   val subject =  when(BuildConfig.FLAVOR) {
                        AppConfig.FLAVOR_MASTERNET -> {
                            "beam wallet masternet logs"
                        }
                        AppConfig.FLAVOR_TESTNET -> {
                            "beam wallet testnet logs"
                        }
                        AppConfig.FLAVOR_MAINNET -> {
                            "beam wallet logs"
                        }
                       else -> ""
                   }

                    val shareIntent = Intent()
                    shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context!!, AppConfig.AUTHORITY, File (AppConfig.ZIP_PATH)))
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                    shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.SUPPORT_EMAIL))
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    shareIntent.type = AppConfig.SHARE_TYPE
                    shareIntent.action = Intent.ACTION_SEND;
                    startActivity(shareIntent)

                    isShareLogs = false
                }
            }
        }

    }


    override fun changePass() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToCheckOldPassFragment())
    }

    override fun addListeners() {
        changePass.setOnClickListener {
            presenter?.onChangePass()
        }

        reportProblem.setOnClickListener {
            presenter?.onReportProblem()
        }

        rateApp.setOnClickListener {
            try {
                var playstoreuri1: Uri = Uri.parse("market://details?id=" + this.activity?.packageName)
                var playstoreIntent1: Intent = Intent(Intent.ACTION_VIEW, playstoreuri1)
                startActivity(playstoreIntent1)
            }catch (exp:Exception){
                var playstoreuri2: Uri = Uri.parse("http://play.google.com/store/apps/details?id=" + this.activity?.packageName)
                var playstoreIntent2: Intent = Intent(Intent.ACTION_VIEW, playstoreuri2)
                startActivity(playstoreIntent2)
            }
        }

        lockScreenLayout.setOnClickListener {
            presenter?.onShowLockScreenSettings()
        }

        confirmTransactionSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter?.onChangeConfirmTransactionSettings(isChecked)
        }

        enableFingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter?.onChangeFingerprintSettings(isChecked)
        }

        runRandomNodeSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter?.onChangeRunOnRandomNode(isChecked)
        }

        allowOpenLinkSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter?.onChangeAllowOpenExternalLink(isChecked)
        }

        ownerKey.setOnClickListener {
            presenter?.onShowOwnerKey()
        }

        languageLayout.setOnClickListener {
            presenter?.onLanguagePressed()
        }

        clearData.setOnClickListener {
            presenter?.onClearDataPressed()
        }

        addNewCategory.setOnClickListener {
            presenter?.onAddCategoryPressed()
        }

        nodeLayout.setOnClickListener {
            presenter?.onNodeAddressPressed()
        }

        ipportLayout.setOnClickListener {
            presenter?.onNodeAddressPressed()
        }

        logsLayout.setOnClickListener {
            presenter?.onLogsPressed()
        }

        seedFrame.setOnClickListener {
            presenter?.onSeedPressed()
        }

        verificationFrame.setOnClickListener {
            presenter?.onSeedPressed()
        }

        faucetFrame.setOnClickListener {
            presenter?.onReceiveFaucet()
        }
    }

    @SuppressLint("InflateParams")

    override fun showLogsDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_log_screen_settings, null)

            val time = LockScreenManager.getCurrentValue()
            var valuesArray = mutableListOf<String>()
            valuesArray.add(getString(R.string.last_5_days))
            valuesArray.add(getString(R.string.last_15_days))
            valuesArray.add(getString(R.string.last_30_days))
            valuesArray.add(getString(R.string.all_time))

            var tag = 0L
            valuesArray.forEach { value ->
                val button = LayoutInflater.from(it).inflate(R.layout.lock_radio_button, view.radioGroupLockSettings, false)
                button.tag = tag
                (button as RadioButton).apply {
                    text = value
                    isChecked = value == logsValue.text
                    setOnClickListener {
                        presenter?.onChangeLogSettings(this.tag as Long)
                        dialog?.dismiss()
                    }
                }

                view.radioGroupLockSettings.addView(button)
                tag++
            }

            view.btnCancel.setOnClickListener { presenter?.onDialogClosePressed() }
            dialog = AlertDialog.Builder(it).setView(view).show()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    @SuppressLint("InflateParams")
    override fun showLockScreenSettingsDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_lock_screen_settings, null)

            val time = LockScreenManager.getCurrentValue()
            val valuesArray = resources.getIntArray(R.array.lock_screen_values)

            valuesArray.forEach { millisInt ->
                val value = millisInt.toLong()

                val button = LayoutInflater.from(it).inflate(R.layout.lock_radio_button, view.radioGroupLockSettings, false)

                (button as RadioButton).apply {
                    text = getLockScreenStringValue(value)
                    isChecked = value == time
                    setOnClickListener { presenter?.onChangeLockSettings(value) }
                }

                view.radioGroupLockSettings.addView(button)
            }

            view.btnCancel.setOnClickListener { presenter?.onDialogClosePressed() }
            dialog = AlertDialog.Builder(it).setView(view).show()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    @SuppressLint("InflateParams")
    override fun showNodeAddressDialog(nodeAddress: String?) {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_node_address, null)
            var okString = ""

            view.nodeBtnConfirm.setOnClickListener {
                presenter?.onSaveNodeAddress(view.dialogNodeValue.text.toString())
            }

            view.nodeBtnCancel.setOnClickListener { presenter?.onDialogClosePressed() }

            view.dialogNodeValue.addTextChangedListener(object : TextWatcher {
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
                        view.dialogNodeValue.setText(okString);
                        view.dialogNodeValue.setSelection(okString.length);
                    }
                    else{
                        okString = originalText
                    }

                    presenter?.onChangeNodeAddress()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })


            if (!nodeAddress.isNullOrBlank()) {
                view.dialogNodeValue.setText(nodeAddress)
            }

            dialog = AlertDialog.Builder(it).setView(view).show()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    @SuppressLint("InflateParams")
    override fun showClearDataDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_clear_data, null)

            view.clearDataBtnConfirm.setOnClickListener {
                presenter?.onDialogClearDataPressed(
                        view.deleteAllAddressesCheckbox.isChecked,
                        view.deleteAllContactsCheckbox.isChecked,
                        view.deleteAllTransactionsCheckbox.isChecked
                )
            }

            view.clearDataBtnCancel.setOnClickListener { presenter?.onDialogClosePressed() }

            dialog = AlertDialog.Builder(it).setView(view).show()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun showClearDataAlert(clearAddresses: Boolean, clearContacts: Boolean, clearTransactions: Boolean) {
        val clearData = arrayListOf<String>()

        if (clearAddresses) {
            clearData.add(getString(R.string.addresses).toLowerCase())
        }

        if (clearContacts) {
            clearData.add(getString(R.string.contacts).toLowerCase())
        }

        if (clearTransactions) {
            clearData.add(getString(R.string.transactions).toLowerCase())
        }

        showAlert(
                getString(R.string.settings_confirm_clear_message, clearData.joinToString(separator = ", ")),
                getString(R.string.delete),
                { presenter?.onConfirmClearDataPressed(clearAddresses, clearContacts, clearTransactions) },
                getString(R.string.settings_dialog_clear_title),
                getString(R.string.cancel)
        )
    }

    override fun showInvalidNodeAddressError() {
        val textView = dialog?.findViewById<TextView>(R.id.nodeError)
        textView?.let { it.visibility = View.VISIBLE }
    }

    override fun clearInvalidNodeAddressError() {
        val textView = dialog?.findViewById<TextView>(R.id.nodeError)
        textView?.let { it.visibility = View.GONE }
    }

    override fun showConfirmPasswordDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        PasswordConfirmDialog.newInstance(onConfirm, onDismiss)
                .show(activity?.supportFragmentManager!!, PasswordConfirmDialog.getFragmentTag())
    }

    private fun getLockScreenStringValue(millis: Long): String {
        return when {
            millis <= LockScreenManager.LOCK_SCREEN_NEVER_VALUE -> getString(R.string.never)
            millis.isLessMinute() -> "${getString(R.string.after)} ${TimeUnit.MILLISECONDS.toSeconds(millis)} ${getString(R.string.seconds)}"
            else -> "${getString(R.string.after)} ${TimeUnit.MILLISECONDS.toMinutes(millis)} ${getString(R.string.minute)}"
        }
    }

    override fun updateLockScreenValue(millis: Long) {
        lockScreenValue.text = getLockScreenStringValue(millis)
    }

    override fun updateConfirmTransactionValue(isConfirm: Boolean) {
        confirmTransactionSwitch.isChecked = isConfirm
    }

    override fun closeDialog() {
        dialog?.let {
            it.dismiss()
            dialog = null
        }
    }

    override fun clearListeners() {
        confirmTransactionSwitch.setOnCheckedChangeListener(null)
        enableFingerprintSwitch.setOnCheckedChangeListener(null)
        changePass.setOnClickListener(null)
        reportProblem.setOnClickListener(null)
        lockScreenTitle.setOnClickListener(null)
        lockScreenValue.setOnClickListener(null)
        runRandomNodeSwitch.setOnCheckedChangeListener(null)
        allowOpenLinkSwitch.setOnCheckedChangeListener(null)
        clearData.setOnClickListener(null)
        addNewCategory.setOnClickListener(null)
        ip.setOnClickListener(null)
        ipTitle.setOnClickListener(null)
        logsLayout.setOnClickListener(null)
        verificationFrame.setOnClickListener(null)
        seedFrame.setOnClickListener(null)
        faucetFrame.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SettingsPresenter(this, SettingsRepository(), SettingsState())
    }
}
