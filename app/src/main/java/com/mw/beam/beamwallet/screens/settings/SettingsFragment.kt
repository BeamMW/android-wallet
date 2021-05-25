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
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
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
import com.mw.beam.beamwallet.core.views.addDoubleDots
import com.mw.beam.beamwallet.screens.confirm.PasswordConfirmDialog
import kotlinx.android.synthetic.main.dialog_clear_data.view.*
import kotlinx.android.synthetic.main.dialog_lock_screen_settings.view.*
import kotlinx.android.synthetic.main.dialog_node_address.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.File
import java.util.concurrent.TimeUnit
import android.graphics.*
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.activity.OnBackPressedCallback
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import com.mw.beam.beamwallet.base_screen.*
import android.os.Environment
import android.provider.Settings
import android.text.*
import android.text.style.ForegroundColorSpan
import android.widget.LinearLayout
import androidx.core.view.children
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mw.beam.beamwallet.core.OnboardManager
import kotlinx.android.synthetic.main.dialog_export_data.view.*
import kotlinx.android.synthetic.main.dialog_lock_screen_settings.view.btnCancel
import java.io.FileOutputStream
import java.io.IOException

import com.github.loadingview.LoadingDialog

import com.mw.beam.beamwallet.core.views.SettingsItemView
import kotlinx.android.synthetic.main.item_settings.view.*
import android.text.style.StyleSpan
import com.mw.beam.beamwallet.core.App
import android.os.Build
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.entities.ExchangeRate
import com.mw.beam.beamwallet.screens.wallet.NavItem
import com.mw.beam.beamwallet.screens.confirm.DoubleAuthorizationFragmentMode
import com.mw.beam.beamwallet.screens.timer_overlay_dialog.TimerOverlayDialog

/**
 *  1/21/19.
 */
class SettingsFragment : BaseFragment<SettingsPresenter>(), SettingsContract.View {

    data class SettingsItem (val icon: Int?, val text:String, var detail:String?, val mode: SettingsFragmentMode, val switch:Boolean? = null, val spannable:Spannable? = null)

    var items = mutableListOf<Array<SettingsItem>>()
    var oldItemsCount = -1

    override fun mode(): SettingsFragmentMode {
        return SettingsFragmentArgs.fromBundle(requireArguments()).mode
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_settings

    override fun getToolbarTitle(): String? {
        return when {
            mode() == SettingsFragmentMode.All -> getString(R.string.settings)
            mode() == SettingsFragmentMode.General -> getString(R.string.settings_general_settings)
            mode() == SettingsFragmentMode.Node -> getString(R.string.node)
            mode() == SettingsFragmentMode.Privacy -> getString(R.string.privacy)
            mode() == SettingsFragmentMode.Utilities -> getString(R.string.utilities)
            mode() == SettingsFragmentMode.Notifications -> getString(R.string.notifications)
            else -> ""
        }
    }

    //shareDb
    private var isShareLogs = false

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(!isShareLogs) {
                if(mode() == SettingsFragmentMode.All) {
                    showWalletFragment()
                }
                else{
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onNeedAddedViews() {
        mainLayout.removeAllViews()
        items.clear()

        when {
            mode() == SettingsFragmentMode.All -> {

                var s1 = mutableListOf<SettingsItem>()
                s1.add(SettingsItem(R.drawable.ic_icon_settings_general,getString(R.string.settings_general_settings),null, SettingsFragmentMode.General))
                s1.add(SettingsItem(R.drawable.ic_notification,getString(R.string.notifications),null, SettingsFragmentMode.Notifications))
                s1.add(SettingsItem(R.drawable.ic_icon_node,getString(R.string.node),null, SettingsFragmentMode.Node))
                s1.add(SettingsItem(R.drawable.ic_icon_settings_privacy,getString(R.string.privacy),null, SettingsFragmentMode.Privacy))
                s1.add(SettingsItem(R.drawable.ic_icon_settings_utilities,getString(R.string.utilities),null, SettingsFragmentMode.Utilities))

           //     var s2 = mutableListOf<SettingsItem>()
           //     s2.add(SettingsItem(R.drawable.ic_icon_settings_tags,getString(R.string.tags),null, SettingsFragmentMode.Tags))

                var s3 = mutableListOf<SettingsItem>()
                s3.add(SettingsItem(R.drawable.ic_icon_settings_rate,getString(R.string.rate_app),null, SettingsFragmentMode.Rate))
                s3.add(SettingsItem(R.drawable.ic_icon_settings_report,getString(R.string.settings_report),null, SettingsFragmentMode.Report))

                var s4 = mutableListOf<SettingsItem>()
                s4.add(SettingsItem(R.drawable.ic_icon_settings_remove,getString(R.string.clear_wallet),null, SettingsFragmentMode.RemoveWallet))

                items.add(s1.toTypedArray())
              //  items.add(s2.toTypedArray())
                items.add(s3.toTypedArray())
                items.add(s4.toTypedArray())

                if(BuildConfig.FLAVOR == AppConfig.FLAVOR_MASTERNET ||  BuildConfig.FLAVOR == AppConfig.FLAVOR_TESTNET)
                {
                    var s5 = mutableListOf<SettingsItem>()
                    s5.add(SettingsItem(R.drawable.ic_icon_settings_general,"Share DB",null, SettingsFragmentMode.ShareDB))
                    items.add(s5.toTypedArray())
                }
            }

            mode()== SettingsFragmentMode.General -> {
                var s1 = mutableListOf<SettingsItem>()
                s1.add(SettingsItem(null, getString(R.string.settings_allow_open_link),null, SettingsFragmentMode.Allow, switch = true))
                s1.add(SettingsItem(null, getString(R.string.background_mode_title),null, SettingsFragmentMode.BackgroundMode, switch = true))
                s1.add(SettingsItem(null, getString(R.string.lock_screen),null, SettingsFragmentMode.Lock))
              //  s1.add(SettingsItem(null, getString(R.string.save_wallet_logs),null, SettingsFragmentMode.Logs))
                s1.add(SettingsItem(null, getString(R.string.show_amounts),null, SettingsFragmentMode.Currency))
                s1.add(SettingsItem(null, getString(R.string.clear_local_data),null, SettingsFragmentMode.ClearLocal))

                var s2 = mutableListOf<SettingsItem>()
                s2.add(SettingsItem(null, getString(R.string.language),null, SettingsFragmentMode.Language))
                s2.add(SettingsItem(null, getString(R.string.dark_mode),null, SettingsFragmentMode.DarkMode, switch = App.isDarkMode))

                items.add(s1.toTypedArray())
                items.add(s2.toTypedArray())
            }
            mode() == SettingsFragmentMode.Notifications -> {
                var s1 = mutableListOf<SettingsItem>()
                s1.add(SettingsItem(null, getString(R.string.wallet_updates),null, SettingsFragmentMode.WalletUpdates, switch = true))
               // s1.add(SettingsItem(null, getString(R.string.news),null, SettingsFragmentMode.News, switch = true))
               // s1.add(SettingsItem(null, getString(R.string.address_expiration),null, SettingsFragmentMode.AddressExpiration, switch = true))
                s1.add(SettingsItem(null, getString(R.string.transaction_status),null, SettingsFragmentMode.TransactionStatus, switch = true))
                items.add(s1.toTypedArray())
            }
            mode() == SettingsFragmentMode.Node -> {
                var s1 = mutableListOf<SettingsItem>()
                s1.add(SettingsItem(null, getString(R.string.settings_run_random_node),AppConfig.NODE_ADDRESS, SettingsFragmentMode.ConnectNode, switch = true, spannable = createNodeSpannableString()))
                items.add(s1.toTypedArray())

                var s2 = mutableListOf<SettingsItem>()
                s2.add(SettingsItem(null, getString(R.string.mobile_node_title),getString(R.string.mobile_node_text), SettingsFragmentMode.MobileNode, switch = true))
                items.add(s2.toTypedArray())

                toolbarLayout.changeNodeButton.alpha = 0f
                toolbarLayout.changeNodeButton.visibility = View.GONE
                toolbarLayout.changeNodeButton.isEnabled = false
            }
            mode() == SettingsFragmentMode.Privacy -> {
                var s1 = mutableListOf<SettingsItem>()
                s1.add(SettingsItem(null, getString(R.string.settings_ask_password_on_send),null, SettingsFragmentMode.AskPassword, switch = true))
                s1.add(SettingsItem(null, getString(R.string.settings_enable_fingerprint),null, SettingsFragmentMode.FingerPrint, switch = true))
                s1.add(SettingsItem(null, getString(R.string.max_privacy_lock_time),null, SettingsFragmentMode.MaxPrivacyLimit))

                if (OnboardManager.instance.isSkipedSeed()) {
                    s1.add(SettingsItem(null, getString(R.string.complete_seed_verification),null, SettingsFragmentMode.Verification))
                }
                s1.add(SettingsItem(null, getString(R.string.show_owner_key),null, SettingsFragmentMode.OwnerKey))
                s1.add(SettingsItem(null, getString(R.string.change_password),null, SettingsFragmentMode.ChangePassword))
                items.add(s1.toTypedArray())
            }
            mode() == SettingsFragmentMode.Utilities -> {
                var s1 = mutableListOf<SettingsItem>()
                s1.add(SettingsItem(null, getString(R.string.show_public_offline),null, SettingsFragmentMode.ShowPublicOfflineAddress))
                s1.add(SettingsItem(null, getString(R.string.get_beam_faucet),null, SettingsFragmentMode.Faucet))

                if (AppManager.instance.isMaxPrivacyEnabled())
                {
                    s1.add(SettingsItem(null, getString(R.string.rescan),null, SettingsFragmentMode.Rescan))
                }

                s1.add(SettingsItem(null, getString(R.string.payment_proof),null, SettingsFragmentMode.Proof))
                s1.add(SettingsItem(null, getString(R.string.export_wallet_data),null, SettingsFragmentMode.Export))
                s1.add(SettingsItem(null, getString(R.string.import_wallet_data),null, SettingsFragmentMode.Import))
                items.add(s1.toTypedArray())
            }
        }


        addItems()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarLayout.centerTitle = mode() == SettingsFragmentMode.All

        appVersionTitle.text = ""
        appVersionValue.text = "v " + BuildConfig.VERSION_NAME

        onBackPressedCallback.isEnabled = true

        initToolbar(getToolbarTitle(), hasBackArrow = mode() != SettingsFragmentMode.All, hasStatus = true)

        if(mode() == SettingsFragmentMode.All) {
            requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)

            (activity as? AppActivity)?.enableLeftMenu(true)
            toolbar.setNavigationIcon(R.drawable.ic_menu)
            toolbar.setNavigationOnClickListener {
                (activity as? AppActivity)?.openMenu()
            }
        }
    }


    private fun addItems() {
        mainLayout.removeAllViews()

        for (subItems in items.reversed()) {
            var param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            param.setMargins(ScreenHelper.dpToPx(context, 10),
                    ScreenHelper.dpToPx(context, 10),
                    ScreenHelper.dpToPx(context, 10),
                    ScreenHelper.dpToPx(context, 10))

            val section = LinearLayout(requireContext())
            section.orientation = LinearLayout.VERTICAL
            section.background = requireContext().getDrawable(R.drawable.wallet_state_card_backgroud)
            section.layoutParams = param

            for ((index, item) in subItems.withIndex()) {
                val item = SettingsItemView(requireContext()).apply {
                    text = item.text
                    detail = item.detail
                    iconResId = item.icon
                    mode = item.mode
                    switch = item.switch
                    spannable = item.spannable
                }

                if(item.mode == SettingsFragmentMode.RemoveWallet)
                {
                    item.textLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.remove))
                    item.iconView.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.remove))
                }

                item.setOnClickListener {
                    val item: SettingsItemView = if (it is androidx.appcompat.widget.SwitchCompat) {
                        it.parent.parent as SettingsItemView
                    } else {
                        it.parent as SettingsItemView
                    }

                    if(item.mode == SettingsFragmentMode.Node) {
                        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToNodeFragment())
                    }
                    else if (item.mode == SettingsFragmentMode.General || item.mode == SettingsFragmentMode.Privacy
                            || item.mode == SettingsFragmentMode.Utilities || item.mode == SettingsFragmentMode.Notifications) {
                                findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentSelf((item.mode)))
                    } else if (item.mode == SettingsFragmentMode.Lock) {
                        presenter?.onShowLockScreenSettings()
                    }
                    else if (item.mode == SettingsFragmentMode.MaxPrivacyLimit) {
                        presenter?.onShowMaxPrivacySettings()
                    }
                    else if (item.mode == SettingsFragmentMode.Logs) {
                        presenter?.onLogsPressed()
                    }
                    else if (item.mode == SettingsFragmentMode.Currency) {
                        presenter?.onCurrencyPressed()
                    }
                    else if (item.mode == SettingsFragmentMode.ClearLocal) {
                        presenter?.onClearDataPressed()
                    } else if (item.mode == SettingsFragmentMode.Language) {
                        presenter?.onLanguagePressed()
                    } else if (item.mode == SettingsFragmentMode.Allow) {
                        val allow = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onChangeAllowOpenExternalLink(allow)
                    }
                    else if (item.mode == SettingsFragmentMode.News) {
                        val allow = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onChangeAllowNews(allow)
                    }
                    else if (item.mode == SettingsFragmentMode.TransactionStatus) {
                        val allow = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onChangeAllowTransactionStatus(allow)
                    }
                    else if (item.mode == SettingsFragmentMode.WalletUpdates) {
                        val allow = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onChangeAllowWalletUpdates(allow)
                    }
                    else if (item.mode == SettingsFragmentMode.AddressExpiration) {
                        val allow = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onChangeAllowAddressExpiration(allow)
                    }
                    else if (item.mode == SettingsFragmentMode.Rate) {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.activity?.packageName)))
                        } catch (exp: Exception) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + this.activity?.packageName)))
                        }
                    } else if (item.mode == SettingsFragmentMode.Report) {
                        presenter?.onReportProblem()
                    } else if (item.mode == SettingsFragmentMode.RemoveWallet) {
                        presenter?.onRemoveWalletPressed()
                    } else if (item.mode == SettingsFragmentMode.ConnectNode) {
                        val allow = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onChangeRunOnRandomNode(allow)
                    }
                    else if (item.mode == SettingsFragmentMode.BackgroundMode) {
                        val allow = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onChangeRunOnBackground(allow)
                    }
                    else if (item.mode == SettingsFragmentMode.MobileNode) {
                        val enabled = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onEnableMobileNode(enabled)
                    }
                    else if (item.mode == SettingsFragmentMode.AskPassword) {
                        val allow = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onChangeConfirmTransactionSettings(allow)
                    } else if (item.mode == SettingsFragmentMode.FingerPrint) {
                        val allow = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        presenter?.onChangeFingerprintSettings(allow)
                    } else if (item.mode == SettingsFragmentMode.OwnerKey) {
                        presenter?.onShowOwnerKey()
                    } else if (item.mode == SettingsFragmentMode.SeedPhrase) {
                        presenter?.onSeedPressed()
                    } else if (item.mode == SettingsFragmentMode.ChangePassword) {
                        presenter?.onChangePass()
                    } else if (item.mode == SettingsFragmentMode.Faucet) {
                        presenter?.onReceiveFaucet()
                    } else if (item.mode == SettingsFragmentMode.Proof) {
                        presenter?.onProofPressed()
                    } else if (item.mode == SettingsFragmentMode.Verification) {
                        presenter?.onSeedVerificationPressed()
                    } else if (item.mode == SettingsFragmentMode.Export) {
                        presenter?.onExportPressed()
                    } else if (item.mode == SettingsFragmentMode.Import) {
                        presenter?.omImportPressed()
                    }  else if (item.mode == SettingsFragmentMode.DarkMode) {
                        val isDark = (it as androidx.appcompat.widget.SwitchCompat).isChecked
                        App.isDarkMode = isDark
                        PreferencesManager.putBoolean(PreferencesManager.DARK_MODE, isDark)
                        (activity as? AppActivity)?.changeTheme()
                        (activity as? AppActivity)?.selectItem(NavItem.ID.SETTINGS)

                        val ft = requireFragmentManager().beginTransaction()
                        ft.detach(this).attach(this).commit()
                    }
                    else  if (item.mode == SettingsFragmentMode.ShareDB) {
                        ZipManager.zip(AppConfig.DB_PATH, AppConfig.ZIP_PATH);
                        val subject = ""
                        val shareIntent = Intent()
                        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), AppConfig.AUTHORITY, File (AppConfig.ZIP_PATH)))
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                        shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.SUPPORT_EMAIL))
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        shareIntent.type = AppConfig.SHARE_TYPE
                        shareIntent.action = Intent.ACTION_SEND;
                        startActivity(shareIntent)
                    }
                    else if (item.mode == SettingsFragmentMode.ShowPublicOfflineAddress) {
                        presenter?.onShowPublicOfflineAddressPressed()
                    }
                    else if (item.mode == SettingsFragmentMode.Rescan) {
                        presenter?.onRescanPressed()
                    }
                }

                if (mode() == SettingsFragmentMode.Node) {
                    item.cardItem.setOnClickListener {
                        presenter?.onNodeAddressPressed()
                    }
                }

                if (item.mode == SettingsFragmentMode.FingerPrint) {
                    item.visibility = View.GONE
                }

                if (item.mode == SettingsFragmentMode.News || item.mode == SettingsFragmentMode.WalletUpdates ||
                        item.mode == SettingsFragmentMode.AddressExpiration || item.mode == SettingsFragmentMode.TransactionStatus) {
                    item.setPadding(0, 4, 0, 25)
                }
                else  if (item.mode == SettingsFragmentMode.CreateTag) {
                    item.setPadding(0, 12, 0, 12)
                } else if (subItems.count() == 1 && item.iconResId != null) {
                    item.setPadding(0, 10, 0, 10)
                } else if (item.switch != null && index == 0) {
                    item.setPadding(0, 4, 0, 25)
                } else {
                    item.setPadding(0, 4, 0, 4)
                }

                section.addView(item)

            }

            if (subItems.count() > 0) {
                if (subItems.last().icon == null) {
                    section.setPadding(0, 12, 0, 12)
                }
            }

            mainLayout.addView(section, 0)
        }
    }

    override fun setRunOnRandomNode(runOnRandomNode: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.ConnectNode) {
                    item.switch = runOnRandomNode
                    item.spannable = createNodeSpannableString()
                }
            }
        }
    }


    override fun setMobileNodeEnabled(enabled: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.MobileNode) {
                    item.switch = enabled
                }
            }
        }
    }

    override fun setRunOnBackground(allow: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.BackgroundMode) {
                    item.switch = allow
                }
            }
        }
    }

    override fun onReconnected() {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.ConnectNode) {
                    item.spannable = createNodeSpannableString()
                }
            }
        }
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

    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
    }

    override fun showRescanDialog() {
        showAlert(
                getString(R.string.rescan_text),
                getString(R.string.rescan),
                {
                    AppManager.instance.wallet?.rescan()
                },
                getString(R.string.rescan),
                getString(R.string.cancel)
        )
    }

    override fun setLogSettings(days: Long) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.Logs) {
                    item.detail = when (days) {
                        0L ->  getString(R.string.all_time)
                        5L ->  getString(R.string.last_5_days)
                        15L ->  getString(R.string.last_15_days)
                        30L ->  getString(R.string.last_30_days)
                        else -> ""
                    }
                }
            }
        }
    }

    override fun setCurrencySettings(currency: Currency) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.Currency) {
                    item.detail = currency.name(requireContext())
                }
            }
        }
    }

    override fun setAllowOpenExternalLinkValue(allowOpen: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.Allow) {
                    item.switch = allowOpen
                }
            }
        }
    }

    override fun setAllowNews(allow: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.News) {
                    item.switch = allow
                }
            }
        }
    }

    override fun setAllowTransaction(allow: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.TransactionStatus) {
                    item.switch = allow
                }
            }
        }
    }

    override fun setAllowWalletUpdates(allow: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.WalletUpdates) {
                    item.switch = allow
                }
            }
        }
    }

    override fun setAllowAddressExpiration(allow: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.AddressExpiration) {
                    item.switch = allow
                }
            }
        }
    }

    override fun showFingerprintSettings(isFingerprintEnabled: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                item.visibility = View.VISIBLE
                if (item.mode == SettingsFragmentMode.FingerPrint) {
                    item.switch = isFingerprintEnabled
                }
            }
        }
    }


    override fun navigateToCurrency() {
        context?.let {
            val valueId = PreferencesManager.getLong(PreferencesManager.KEY_CURRENCY, 0)
            val currency = Currency.fromValue(valueId.toInt())

            val view = LayoutInflater.from(it).inflate(R.layout.dialog_currency_settings, null)

            val valuesArray = resources.getStringArray(R.array.currency_values)

            var index = 0
            valuesArray.forEach { string ->
                val button = LayoutInflater.from(it).inflate(R.layout.lock_radio_button, view.radioGroupLockSettings, false)

                (button as RadioButton).apply {
                    text = string
                    isChecked = (index == 0 && currency == Currency.Usd) || (index == 1 && currency == Currency.Bitcoin)
                    setOnClickListener {sender->
                        val btn = sender as RadioButton
                        if (btn.text == getString(R.string.usd)) {
                            PreferencesManager.putLong(PreferencesManager.KEY_CURRENCY, Currency.Usd.value.toLong())
                            setCurrencySettings(Currency.Usd)
                        }
                        else {
                            PreferencesManager.putLong(PreferencesManager.KEY_CURRENCY, Currency.Bitcoin.value.toLong())
                            setCurrencySettings(Currency.Bitcoin)
                        }
                        AppManager.instance.updateCurrentCurrency()
                        presenter?.onDialogClosePressed()
                    }
                }

                view.radioGroupLockSettings.addView(button)

                index += 1
            }

            view.btnCancel.setOnClickListener { presenter?.onDialogClosePressed() }
            dialog = AlertDialog.Builder(it).setView(view).show()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

       // findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToCurrencyFragment())
    }

    override fun navigateToLanguage() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLanguageFragment())
    }

    override fun navigateToSeed() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToDoubleAuthorizationFragment(DoubleAuthorizationFragmentMode.DisplaySeed))
    }

    override fun navigateToSeedVerification() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToDoubleAuthorizationFragment(DoubleAuthorizationFragmentMode.VerificationSeed))
    }

    override fun navigateToPaymentProof() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToProofVerificationFragment())
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
        blurView.visibility = View.VISIBLE

        jp.wasabeef.blurry.Blurry.with(context).capture(view).into(blurView)

        val dialog = TimerOverlayDialog.newInstance {
            blurView.visibility = View.GONE
            if(it) {
                openExternalLink(link)
            }
        }
        dialog.show(activity?.supportFragmentManager!!, TimerOverlayDialog.getFragmentTag())
    }

    private fun isEnableFingerprint(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED) &&
                FingerprintManager.SensorState.READY == FingerprintManager.checkSensorState(view?.getContext()
                ?: return false)
    }

    private fun isEnableFaceID(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED) &&
                FaceIDManager.isManagerAvailable()
    }

    override fun navigateToOwnerKeyVerification() {
        val message = when {
            isEnableFaceID() -> getString(R.string.owner_key_verification_pass_face)
            isEnableFingerprint() -> getString(R.string.owner_key_verification_pass_finger)
            else -> getString(R.string.owner_key_verification_pass)
        }

        showAlert(message = message,
                btnConfirmText = getString(R.string.ok),
                onConfirm = {
                    findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToDoubleAuthorizationFragment(DoubleAuthorizationFragmentMode.OwnerKey))
                },
                title = getString(R.string.owner_key),
                btnCancelText = null,
                onCancel = {  })
    }


    override fun setLanguage(language: LocaleHelper.SupportedLanguage) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.Language) {
                    item.detail = language.nativeName
                }
            }
        }
    }

    override fun sendMailWithLogs() {
        if (!isShareLogs)
        {
            isShareLogs = true

            val dialog = LoadingDialog.get(requireActivity()).show()

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
                    shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), AppConfig.AUTHORITY, File (AppConfig.ZIP_PATH)))
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                    shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.SUPPORT_EMAIL))
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    shareIntent.type = AppConfig.SHARE_TYPE
                    shareIntent.action = Intent.ACTION_SEND;
                    startActivity(shareIntent)

                    dialog?.hide()

                    isShareLogs = false
                }
            }
        }

    }


    override fun changePass() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToCheckOldPassFragment())
    }

    override fun addListeners() {

    }

    private fun createNodeSpannableString(): Spannable {
        val enabled = PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true)
        val node = AppConfig.NODE_ADDRESS

        val text = if (enabled) {
            "\n" + getString(R.string.ip_port) + "\n" + node
        } else {
            getString(R.string.ip_port) + " " + node
        }

        val colorId = if (enabled) {
            resources.getColor(R.color.common_text_color)
        } else {
            resources.getColor(R.color.btn_drop_down_color)
        }

        val spannableContent = SpannableString(text)
        spannableContent.setSpan(StyleSpan(Typeface.BOLD), 0, getString(R.string.ip_port).length + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannableContent.setSpan(ForegroundColorSpan(colorId), 0, getString(R.string.ip_port).length + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        return spannableContent
    }

    @SuppressLint("InflateParams")
    override fun showLogsDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_log_screen_settings, null)

            var valuesArray = mutableListOf<String>()
            valuesArray.add(getString(R.string.last_5_days))
            valuesArray.add(getString(R.string.last_15_days))
            valuesArray.add(getString(R.string.last_30_days))
            valuesArray.add(getString(R.string.all_time))

            val current = when (PreferencesManager.getLong(PreferencesManager.KEY_LOGS)) {
                0L ->  getString(R.string.all_time)
                5L ->  getString(R.string.last_5_days)
                15L ->  getString(R.string.last_15_days)
                30L ->  getString(R.string.last_30_days)
                else -> ""
            }

            var tag = 0L
            valuesArray.forEach { value ->
                val button = LayoutInflater.from(it).inflate(R.layout.lock_radio_button, view.radioGroupLockSettings, false)
                button.tag = tag
                (button as RadioButton).apply {
                    text = value
                    isChecked = value == current
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
    override fun showMaxPrivacySettingsDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_max_privacy_settings, null)

            val time = AppManager.instance?.wallet?.getMaxPrivacyLockTimeLimitHours() ?: 0L
            val valuesArray = resources.getIntArray(R.array.max_privacy_values)

            valuesArray.forEach { millisInt ->
                val value = millisInt.toLong()

                val button = LayoutInflater.from(it).inflate(R.layout.lock_radio_button, view.radioGroupLockSettings, false)

                (button as RadioButton).apply {
                    text = getMaxPrivacyStringValue(value)
                    isChecked = value == time
                    setOnClickListener { presenter?.onChangeMaxPrivacySettings(value) }
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

        this.passwordDialog = PasswordConfirmDialog.newInstance(PasswordConfirmDialog.Mode.ChangeNode, {
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
                                allOK = when (port?.toIntOrNull()) {
                                    null -> false
                                    in 1..65535 -> true
                                    else -> false
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
        }, {})
        this.passwordDialog?.show(activity?.supportFragmentManager!!, PasswordConfirmDialog.getFragmentTag())
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

    @SuppressLint("StringFormatInvalid")
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

    override fun showExportDialog() {

        val viewExportOptions = LayoutInflater.from(context).inflate(R.layout.dialog_export_data, null)
        viewExportOptions.findViewById<TextView>(R.id.btnCancel).setOnClickListener {
            dialog?.dismiss()
        }
        viewExportOptions.contactsCheckbox.isChecked = true
        viewExportOptions.addressesCheckbox.isChecked = true
        viewExportOptions.transactionCheckbox.isChecked = true

        viewExportOptions.findViewById<TextView>(R.id.btnConfirm).setOnClickListener {
            dialog?.dismiss()

            val contacts = viewExportOptions.contactsCheckbox.isChecked
            val addresses = viewExportOptions.addressesCheckbox.isChecked
            val transactions = viewExportOptions.transactionCheckbox.isChecked

            val excludeParameters = mutableListOf<String>()
            if (!contacts) excludeParameters.add("Contacts")
            if (!addresses) excludeParameters.add("OwnAddresses")
            if (!transactions) excludeParameters.add("TransactionParameters")

            presenter?.onExportWithExclude(excludeParameters.toTypedArray())
        }

        dialog = AlertDialog.Builder(requireContext()).setView(viewExportOptions).show().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun showExportSaveDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_share, null)

        view.findViewById<TextView>(R.id.dialogTitle).text = getString(R.string.export_wallet_data)
        view.findViewById<TextView>(R.id.dialogTitle2).text = getString(R.string.export_wallet_data_text)

        view.findViewById<TextView>(R.id.cancelBtn).setOnClickListener {
            dialog?.dismiss()
        }

        view.findViewById<TextView>(R.id.shareBtn).setOnClickListener {
            dialog?.dismiss()
            presenter?.onExportShare()
        }

        view.findViewById<TextView>(R.id.saveBtn).setOnClickListener {
            dialog?.dismiss()
            presenter?.onExportSave()
        }

        dialog = AlertDialog.Builder(requireContext()).setView(view).show().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
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
        this.passwordDialog =  PasswordConfirmDialog.newInstance(PasswordConfirmDialog.Mode.ChangeSettings, onConfirm, onDismiss)
        this.passwordDialog?.show(activity?.supportFragmentManager!!, PasswordConfirmDialog.getFragmentTag())
    }

    private fun getLockScreenStringValue(millis: Long): String {
        return when {
            millis <= LockScreenManager.LOCK_SCREEN_NEVER_VALUE -> getString(R.string.never)
            millis.isLessMinute() -> "${getString(R.string.after)} ${TimeUnit.MILLISECONDS.toSeconds(millis)} ${getString(R.string.seconds)}"
            else -> "${getString(R.string.after)} ${TimeUnit.MILLISECONDS.toMinutes(millis)} ${getString(R.string.minute)}"
        }
    }


    private fun getMaxPrivacyStringValue(hours: Long): String {
        when (hours) {
            24L -> {
                return getString(R.string.h24)
            }
            36L -> {
                return getString(R.string.h36)
            }
            48L -> {
                return getString(R.string.h48)
            }
            60L -> {
                return getString(R.string.h60)
            }
            72L -> {
                return getString(R.string.h72)
            }
            else -> return getString(R.string.no_limit)
        }
    }


    override fun updateLockScreenValue(millis: Long) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.Lock) {
                    item.detail = getLockScreenStringValue(millis)
                }
            }
        }
    }

    override fun updateConfirmTransactionValue(isConfirm: Boolean) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.AskPassword) {
                    item.switch = isConfirm
                }
            }
        }
    }

    override fun updateMaxPrivacyValue(hours: Long) {
        for (view in mainLayout.children) {
            for (group in (view as LinearLayout).children) {
                var item = group as SettingsItemView
                if (item.mode == SettingsFragmentMode.MaxPrivacyLimit) {
                    item.detail = getMaxPrivacyStringValue(hours)
                }
            }
        }
    }

    override fun exportSave(content: String) {
        val fileName = "wallet_data_" + System.currentTimeMillis() + ".dat"

        Dexter.withActivity(activity)
                .withPermission(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : PermissionListener {

                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        val outputStream: FileOutputStream
                        try {
                            var dir2 =  Environment.getDownloadCacheDirectory().getPath()
                            val file2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                            outputStream = FileOutputStream(file2)
                            outputStream.write(content.toByteArray())
                            outputStream.close()

                            showSnackBar(getString(R.string.wallet_data_saved))

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        if (response?.isPermanentlyDenied == true){
                            showAlert(message = getString(R.string.storage_permission_required_message_small),
                                    btnConfirmText = getString(R.string.settings),
                                    onConfirm = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intent.data = Uri.fromParts("package", context?.packageName, null)
                                        startActivity(intent)
                                    },
                                    title = getString(R.string.send_permission_required_title),
                                    btnCancelText = getString(R.string.cancel))
                        }
                    }

                }).check()
    }

    override fun exportShare(file: File) {
        val context = context ?: return

        val uri = FileProvider.getUriForFile(context, AppConfig.AUTHORITY, file)

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/dat"
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.export_wallet_data)))
    }

    override fun showImportDialog() {
        showAlert(message = getString(R.string.import_data_warning),
                btnConfirmText = getString(R.string.ok),
                onConfirm = {
                    val intent = Intent()
                            .setType("*/*")
                            .setAction(Intent.ACTION_GET_CONTENT)
                            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false)
                    activity?.startActivityForResult(Intent.createChooser(intent, getString(R.string.import_wallet_data)), AppActivity.IMPORT_FILE_REQUEST)
                },
                title = getString(R.string.import_wallet_data))
    }

    override fun closeDialog() {
        dialog?.let {
            it.dismiss()
            dialog = null
        }
    }

    override fun showConfirmRemoveWallet() {
        if(AppManager.instance.hasActiveTransactions())
        {
            showAlert(getString(R.string.clear_wallet_transactions_text),getString(R.string.ok),{
            },getString(R.string.clear_wallet))
        }
        else{
            showAlert(message = getString(R.string.clear_wallet_text),
                    btnConfirmText = getString(R.string.remove_wallet),
                    onConfirm = {
                        this.passwordDialog =  PasswordConfirmDialog.newInstance(PasswordConfirmDialog.Mode.RemoveWallet, {
                            presenter?.onConfirmRemoveWallet()
                        }, {})
                        this.passwordDialog?.show(activity?.supportFragmentManager!!, PasswordConfirmDialog.getFragmentTag())
                    },
                    title = getString(R.string.clear_wallet),
                    btnCancelText = getString(R.string.cancel))
        }

    }

    override fun showPublicOfflineAddress() {
        if(AppManager.instance.isMaxPrivacyEnabled()) {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToPublicOfflineAddressFragment())
        }
        else {
            showAlert(message = getString(R.string.connect_node_offline_public),
                    btnConfirmText = getString(R.string.ok),
                    onConfirm = {
                    },
                    title = getString(R.string.show_public_offline),
                    btnCancelText = null,
                    onCancel = {  })
        }
    }

    override fun walletRemoved() {
        (activity as BaseActivity<*>).logOut()
    }

    override fun clearListeners() {

    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SettingsPresenter(this, SettingsRepository(), SettingsState())
    }

    override fun exportError() {
        activity?.runOnUiThread {
            showAlert(getString(R.string.incorrect_file_text), getString(R.string.ok), {

            }, getString(R.string.incorrect_file_title))
        }
    }
}
