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

package com.mw.beam.beamwallet.screens.show_token

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.BMAddressType
import com.mw.beam.beamwallet.core.helpers.convertToBeamString

import kotlinx.android.synthetic.main.fragment_receive_show_token.*
import kotlinx.android.synthetic.main.fragment_receive_show_token.toolbarLayout

/**
 *  3/4/19.
 */
class ShowTokenFragment : BaseFragment<ShowTokenPresenter>(), ShowTokenContract.View {
    override fun onControllerGetContentLayoutId() = R.layout.fragment_receive_show_token
    override fun getToolbarTitle(): String = getString(R.string.address_details)
    override fun getToken(): String = ShowTokenFragmentArgs.fromBundle(requireArguments()).token

    override fun getStatusBarColor(): Int {
        return if(ShowTokenFragmentArgs.fromBundle(requireArguments()).receive) {
            return if(App.isDarkMode) {
                ContextCompat.getColor(requireContext(), R.color.receive_toolbar_color_dark)
            }
            else {
                ContextCompat.getColor(requireContext(), R.color.receive_toolbar_color)
            }
        }
        else {
            return if (App.isDarkMode) {
                ContextCompat.getColor(requireContext(), R.color.send_toolbar_color_dark)
            }
            else {
                ContextCompat.getColor(requireContext(), R.color.send_toolbar_color)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun init(token: String) {
        toolbarLayout.hasStatus = true

        registerForContextMenu(addressLayout)
        registerForContextMenu(identityLayout)
       // registerForContextMenu(tokenLayout)

        val isReceive = ShowTokenFragmentArgs.fromBundle(requireArguments()).receive

        if(AppManager.instance.wallet?.isToken(token) == true) {
            val params = AppManager.instance.wallet?.getTransactionParameters(token, false)
            if(params != null) {
                if(params.amount > 0) {
                    amountLayout.visibility = View.VISIBLE
                    amountValue.text = """${params.amount.convertToBeamString()} BEAM"""
                }

                transactionTypeLayout.visibility = View.VISIBLE

                if (params.address.isEmpty()) {
                    addressLayout.visibility = View.GONE
                }
                else {
                    addressLayout.visibility = View.VISIBLE
                    addressValue.text = params.address
                }

                when {
                    params.getAddressType() == BMAddressType.BMAddressTypeMaxPrivacy -> {
                        transactionTypeValue.text = getString(R.string.max_privacy)
                    }
                    params.getAddressType() == BMAddressType.BMAddressTypeOfflinePublic -> {
                        transactionTypeValue.text = getString(R.string.public_offline)
                    }
                    else -> {
                        transactionTypeValue.text = getString(R.string.regular)
                    }
                }

                if(params.identity.isNotEmpty()) {
                    identityLayout.visibility = View.VISIBLE
                    identityValue.text = params.identity
                }
            }
        }
        else {
            transactionTypeValue.text = getString(R.string.regular)
            transactionTypeLayout.visibility = View.VISIBLE
        }

        tokenValue.text = token

        if (!isReceive) {
            btnShare.background = resources.getDrawable(R.drawable.send_button, null)
            if(App.isDarkMode) {
                gradientView.setBackgroundResource(R.drawable.send_bg_dark)
            }
             else {
                gradientView.setBackgroundResource(R.drawable.send_bg)
            }
        }
        else {
            if(App.isDarkMode) {
                gradientView.setBackgroundResource(R.drawable.receive_bg_dark)
            }
        }
    }

    override fun addListeners() {
        btnShare.setOnClickListener {
            presenter?.onCopyToken()
            showSnackBar(getString(R.string.address_copied_to_clipboard))
            val isReceive = ShowTokenFragmentArgs.fromBundle(requireArguments()).receive
            if (isReceive) {
                setFragmentResult("FragmentB_REQUEST_KEY", bundleOf("data" to "button clicked"))
            }
            findNavController().popBackStack()
        }

        btnCopy.setOnClickListener {
            presenter?.onCopyToken()
            val isReceive = ShowTokenFragmentArgs.fromBundle(requireArguments()).receive
            if (isReceive) {
                setFragmentResult("FragmentB_REQUEST_KEY", bundleOf("data" to "button clicked"))
            }
            showSnackBar(getString(R.string.address_copied_to_clipboard))
        }
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ShowTokenPresenter(this, ShowTokenRepository(), ShowTokenState())
    }

    override fun clearListeners() {
        btnShare.setOnClickListener(null)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val copy = SpannableStringBuilder()
        copy.append(getString(R.string.copy))
        copy.setSpan(ForegroundColorSpan(Color.WHITE),
                0, copy.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val share = SpannableStringBuilder()
        share.append(getString(R.string.share))
        share.setSpan(ForegroundColorSpan(Color.WHITE),
                0, share.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        menu.add(0, v.id, 0, copy)
        menu.add(0, v.id, 0, share)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {

        if (item.itemId == addressLayout.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(addressValue.text.toString(), "")
                showSnackBar(getString(R.string.copied_to_clipboard))
            } else {
                shareText(addressValue.text.toString())
            }
        } else if (item.itemId == identityLayout.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(identityValue.text.toString(), "")
                showSnackBar(getString(R.string.copied_to_clipboard))
            } else {
                shareText(identityValue.text.toString())
            }
        } else if (item.itemId == tokenLayout.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(tokenValue.text.toString(), "")
                showSnackBar(getString(R.string.copied_to_clipboard))
            } else {
                shareText(tokenValue.text.toString())
            }
        }

        return super.onContextItemSelected(item)
    }

    private fun shareText(text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.common_share_title)))
    }
}
