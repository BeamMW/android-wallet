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

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.content.ContextCompat

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*

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
        return ContextCompat.getColor(context!!, R.color.received_color)
    }

    override fun init(token: String) {
        toolbarLayout.hasStatus = true

        tokenView.text = token

        (activity as BaseActivity<*>).supportActionBar?.title = getString(R.string.show_token)
    }

    override fun addListeners() {
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                presenter?.onCopyToken()
            }
        })

        tokenView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    override fun copyToClipboard(content: String?, tag: String) {
        super.copyToClipboard(content, tag)

        showSnackBar(getString(R.string.address_copied_to_clipboard))
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ShowTokenPresenter(this, ShowTokenRepository(), ShowTokenState())
    }

    override fun clearListeners() {
        tokenView.setOnTouchListener(null)
    }
}
