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
import android.text.InputFilter
import android.text.Spanned
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
import com.google.android.material.navigation.NavigationView
import com.mw.beam.beamwallet.screens.wallet.NavItem
import kotlinx.android.synthetic.main.fragment_settings.drawerLayout
import kotlinx.android.synthetic.main.fragment_settings.navView
import androidx.activity.OnBackPressedCallback
import java.util.regex.Pattern

/**
 * Created by vain onnellinen on 1/21/19.
 */
class SettingsFragment : BaseFragment<SettingsPresenter>(), SettingsContract.View {
    private var dialog: AlertDialog? = null

    override fun onControllerGetContentLayoutId() = R.layout.fragment_settings
    override fun getToolbarTitle(): String? = getString(R.string.settings)

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
        appVersion.text = BuildConfig.VERSION_NAME
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
            nodeLayout.setPadding(0,20,0,20)
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

            nodeLayout.setPadding(0,0,0,0)
            runRandomNodeSwitch.setPadding(0,0,0,0)

            ip.setPadding(0,0,0,0)
            ipportLayout.orientation = android.widget.LinearLayout.VERTICAL
        }

        configNavView(toolbarLayout, navView as NavigationView, drawerLayout, NavItem.ID.SETTINGS);
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


    override fun setAllowOpenExternalLinkValue(allowOpen: Boolean) {
        allowOpenLinkSwitch.isChecked = allowOpen
    }

    override fun showFingerprintSettings(isFingerprintEnabled: Boolean) {
        enableFingerprintTitle.visibility = View.VISIBLE
        enableFingerprintSwitch.visibility = View.VISIBLE
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
                setPadding(0,0,0,20)
            })
        }
    }

    override fun setLanguage(language: LocaleHelper.SupportedLanguage) {
        languageValue.text = language.nativeName
    }

    override fun sendMailWithLogs() {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.type = AppConfig.SHARE_TYPE
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, AppConfig.SHARE_VALUE)
        shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.SUPPORT_EMAIL))

        val uris = ArrayList<Uri>()
        val files = File(AppConfig.LOG_PATH).listFiles()

        files.asIterable().forEach {
            uris.add(FileProvider.getUriForFile(context
                    ?: return, AppConfig.AUTHORITY, File(AppConfig.LOG_PATH, it.name)))
        }

        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(Intent.createChooser(shareIntent, getString(R.string.settings_send_logs_description)))
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

            view.nodeBtnConfirm.setOnClickListener {
                presenter?.onSaveNodeAddress(view.dialogNodeValue.text.toString())
            }

            view.nodeBtnCancel.setOnClickListener { presenter?.onDialogClosePressed() }

            view.dialogNodeValue.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    presenter?.onChangeNodeAddress()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

//            view.dialogNodeValue.filters = Array<InputFilter>(1) {
//                object : InputFilter {
//                    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
////
//                        if (end>start)
//                        {
//                            val destTxt = dest.toString()
//                            val resultingTxt = destTxt.substring(0, dstart) +
//                            source.subSequence(start, end) +  destTxt.substring(dend)
//
//                            val regex = Pattern.compile("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.:");
//
//                            if (regex.matcher(resultingTxt).find()) {
//                                return "";
//                            }
//                        }
//
//                        return null
//                    }
//                }
//            }

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
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SettingsPresenter(this, SettingsRepository(), SettingsState())
    }
}
