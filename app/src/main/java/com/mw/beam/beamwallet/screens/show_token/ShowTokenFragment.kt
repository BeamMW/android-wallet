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
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.convertToBeamString

import kotlinx.android.synthetic.main.fragment_receive_show_token.*
import kotlinx.android.synthetic.main.fragment_receive_show_token.toolbarLayout

/**
 *  3/4/19.
 */
class ShowTokenFragment : BaseFragment<ShowTokenPresenter>(), ShowTokenContract.View {
    override fun onControllerGetContentLayoutId() = R.layout.fragment_receive_show_token
    override fun getToolbarTitle(): String? = getString(R.string.address_details)
    override fun getToken(): String = ShowTokenFragmentArgs.fromBundle(requireArguments()).token
    override fun getStatusBarColor(): Int {
        return if(ShowTokenFragmentArgs.fromBundle(requireArguments()).receive) {
            ContextCompat.getColor(requireContext(), R.color.received_color)
        }
        else {
            ContextCompat.getColor(requireContext(), R.color.sent_color)
        }
    }

    override fun setCount(count: Int) {
        countTokenLayout.visibility = View.VISIBLE
        countTokenValue.text = count.toString()
    }

    @SuppressLint("SetTextI18n")
    override fun init(token: String) {
        toolbarLayout.hasStatus = true

        registerForContextMenu(addressLayout)
        registerForContextMenu(identityLayout)
        registerForContextMenu(tokenLayout)

        val isReceive = ShowTokenFragmentArgs.fromBundle(requireArguments()).receive

        if(AppManager.instance.wallet?.isToken(token) == true) {
            val params = AppManager.instance.wallet?.getTransactionParameters(token, true)
            if(params != null) {
                if(params.amount > 0) {
                    amountLayout.visibility = View.VISIBLE
                    amountValue.text = """${params.amount.convertToBeamString()} BEAM"""
                }

                tokenTypeLayout.visibility = View.VISIBLE
                transactionTypeLayout.visibility = View.VISIBLE

                if(params.isPermanentAddress) {
                    tokenTypeValue.text = getString(R.string.permanent)
                }
                else {
                    tokenTypeValue.text = getString(R.string.one_time)
                }

                if(params.isMaxPrivacy) {
                    transactionTypeValue.text = getString(R.string.max_privacy)
                    tokenTypeLayout.visibility = View.GONE
                    addressLayout.visibility = View.GONE
                }
                else if(params.isOffline) {
                    transactionTypeValue.text = getString(R.string.max_privacy_title)
                    tokenTypeLayout.visibility = View.GONE
                    addressLayout.visibility = View.VISIBLE
                }
                else if(params.isPublicOffline) {
                    transactionTypeValue.text = getString(R.string.public_offline)
                    tokenTypeLayout.visibility = View.GONE
                    addressLayout.visibility = View.GONE
                }
                else if(params.isMaxPrivacy && !params.isOffline) {
                    addressLayout.visibility = View.GONE
                    countTokenLayout.visibility = View.VISIBLE
                    countTokenValue.text = getString(R.string.online)
                    transactionTypeValue.text = getString(R.string.max_privacy_title)
                }
                else if(params.isMaxPrivacy && params.isOffline) {
                    addressLayout.visibility = View.GONE
                    transactionTypeValue.text = getString(R.string.max_privacy_title)
                }
                else {
                    transactionTypeValue.text = getString(R.string.regular)
                    addressLayout.visibility = View.VISIBLE
                }

                addressValue.text = params.address

                if(params.identity.isNotEmpty()) {
                    identityLayout.visibility = View.VISIBLE
                    identityValue.text = params.identity
                }
            }
        }
        else {
            transactionTypeLayout.visibility = View.VISIBLE
            transactionTypeValue.text = resources.getString(R.string.regular) + " (" + resources.getString(R.string.for_pool).toLowerCase() + ")"
            tokenTitle.text = resources.getString(R.string.sbbs_address)

            if (isReceive)
            {
                tokenTypeLayout.visibility = View.VISIBLE

                val address = AppManager.instance.getAddress(token)
                if(address == null) {
                    val params = AppManager.instance.wallet?.getTransactionParameters(token, false)

                    if(params?.isPermanentAddress == true) {
                        tokenTypeValue.text = getString(R.string.permanent)
                    }
                    else {
                        tokenTypeValue.text = getString(R.string.one_time)
                    }
                }
                else {
                    if(address.duration == 0L) {
                        tokenTypeValue.text = getString(R.string.permanent)
                    }
                    else {
                        tokenTypeValue.text = getString(R.string.one_time)
                    }
                }
            }
        }

        tokenValue.text = token

        if (!isReceive) {
            gradientView.setBackgroundResource(R.drawable.send_toolbar_gradient)
        }
        else {
            btnShare.visibility = View.VISIBLE
        }

       // (activity as BaseActivity<*>).supportActionBar?.title = getString(R.string.show_token)
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
    }

    override fun copyToClipboard(content: String?, tag: String) {
        super.copyToClipboard(content, tag)
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
