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
import android.view.View
import androidx.core.content.ContextCompat
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
    override fun getToolbarTitle(): String? = null
    override fun getToken(): String = ShowTokenFragmentArgs.fromBundle(arguments!!).token
    override fun getStatusBarColor(): Int {
        return if(ShowTokenFragmentArgs.fromBundle(arguments!!).receive) {
            ContextCompat.getColor(context!!, R.color.received_color)
        }
        else {
            ContextCompat.getColor(context!!, R.color.sent_color)
        }
    }

    override fun setCount(count: Int) {
        countTokenLayout.visibility = View.VISIBLE
        countTokenValue.text = getString(R.string.offline) + ": " +  count.toString() + "/" + AppManager.instance.maxOfflineCount.toString()
    }

    @SuppressLint("SetTextI18n")
    override fun init(token: String) {
        toolbarLayout.hasStatus = true

        if(AppManager.instance.wallet?.isToken(token) == true) {
            val params = AppManager.instance.wallet?.getTransactionParameters(token, true)
            if(params != null) {
                if(params.amount > 0) {
                    amountLayout.visibility = View.VISIBLE
                    amountValue.text = """${params.amount.convertToBeamString()} BEAM"""
                }

                tokenTypeLayout.visibility = View.VISIBLE
                transactionTypeLayout.visibility = View.VISIBLE
                addressLayout.visibility = View.VISIBLE

                if(params.isPermanentAddress) {
                    tokenTypeValue.text = getString(R.string.permanent)
                }
                else {
                    tokenTypeValue.text = getString(R.string.one_time)
                }

                if(params.isMaxPrivacy && !params.isOffline) {
                    transactionTypeValue.text = getString(R.string.max_privacy_title)
                }
                else if(params.isMaxPrivacy && params.isOffline) {
                    transactionTypeValue.text = """${getString(R.string.max_privacy_title)}, ${getString(R.string.offline).toLowerCase()}"""
                }
                else {
                    transactionTypeValue.text = getString(R.string.regular)
                }

                addressValue.text = params.address

                if(params.identity.isNotEmpty()) {
                    identityLayout.visibility = View.VISIBLE
                    identityValue.text = params.identity
                }
            }
        }

        tokenValue.text = token

        val isReceive = ShowTokenFragmentArgs.fromBundle(arguments!!).receive
        if (!isReceive) {
            gradientView.setBackgroundResource(R.drawable.send_toolbar_gradient)
        }
        else {
            btnShare.visibility = View.VISIBLE
        }

        (activity as BaseActivity<*>).supportActionBar?.title = getString(R.string.show_token)
    }

    override fun addListeners() {
        btnShare.setOnClickListener {
            presenter?.onCopyToken()
        }
    }

    override fun copyToClipboard(content: String?, tag: String) {
        super.copyToClipboard(content, tag)

        showSnackBar(getString(R.string.address_copied_to_clipboard))

        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ShowTokenPresenter(this, ShowTokenRepository(), ShowTokenState())
    }

    override fun clearListeners() {
        btnShare.setOnClickListener(null)
    }
}
