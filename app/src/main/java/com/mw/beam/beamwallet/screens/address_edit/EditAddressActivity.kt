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

package com.mw.beam.beamwallet.screens.address_edit

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.core.watchers.OnItemSelectedListener
import kotlinx.android.synthetic.main.activity_edit_address.*


/**
 * Created by vain onnellinen on 3/5/19.
 */
class EditAddressActivity : BaseActivity<EditAddressPresenter>(), EditAddressContract.View {
    private lateinit var presenter: EditAddressPresenter
    private lateinit var expireNowString: String
    private lateinit var activateString: String

    private val commentTextWatcher: TextWatcher = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter.onChangeComment(s?.toString() ?: "")
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val expireListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            presenter.onExpirePeriodChanged(when (position) {
                ExpirePeriod.DAY.ordinal -> ExpirePeriod.DAY
                else -> ExpirePeriod.NEVER
            })
        }
    }

    companion object {
        const val EXTRA_ADDRESS_FOR_EDIT = "EXTRA_ADDRESS_FOR_EDIT"
    }

    override fun onControllerGetContentLayoutId() = R.layout.activity_edit_address
    override fun getToolbarTitle(): String? = getString(R.string.edit_address_title)
    override fun getAddress(): WalletAddress = intent.getParcelableExtra(EXTRA_ADDRESS_FOR_EDIT)

    override fun init(address: WalletAddress) {
        expireNowString = getString(R.string.edit_address_expire_now)
        activateString = getString(R.string.edit_address_expire_activate)

        id.text = address.walletID
        expiresSwitchTitle.text = if (address.isExpired) activateString else expireNowString
        comment.setText(address.label)
        btnSave.isEnabled = false

        if (address.isExpired) {
            expiredTitle.visibility = View.VISIBLE
            expiredTime.visibility = View.VISIBLE
            expiredTime.text = CalendarUtils.fromTimestamp(address.createTime + address.duration)
            expiresTitle.visibility = View.GONE
            expiresSpinner.visibility = View.GONE
        }

        ArrayAdapter.createFromResource(
                this,
                R.array.receive_expires_periods,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            expiresSpinner.adapter = adapter
            expiresSpinner.setSelection(if (address.duration == 0L) 1 else 0)
        }
    }

    override fun configCategory(categories: List<Category>) {


    }

    override fun addListeners() {
        expiresSpinner.onItemSelectedListener = expireListener

        expiresSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.onSwitchCheckedChange(isChecked)
        }

        btnSave.setOnClickListener {
            presenter.onSavePressed()
        }

        comment.addTextChangedListener(commentTextWatcher)
    }

    override fun configExpireSpinnerVisibility(shouldShow: Boolean) {
        if (shouldShow) {
            View.VISIBLE.apply {
                expiresTitle.visibility = this
                expiresSpinner.visibility = this
            }
        } else {
            View.GONE.apply {
                expiresTitle.visibility = this
                expiresSpinner.visibility = this
            }
        }
    }

    override fun configExpireSpinnerTime(shouldExpireNow: Boolean) {
        if (shouldExpireNow) {
            expiresSpinner.visibility = View.GONE
            expiresNow.visibility = View.VISIBLE
            timestamp.visibility = View.VISIBLE
            timestamp.text = CalendarUtils.fromTimestamp(System.currentTimeMillis() / 1000)
        } else {
            expiresSpinner.visibility = View.VISIBLE
            expiresNow.visibility = View.GONE
            timestamp.visibility = View.GONE
        }
    }

    override fun configSaveButton(shouldEnable: Boolean) {
        btnSave.isEnabled = shouldEnable
    }

    override fun finishScreen() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun clearListeners() {
        expiresSwitch.setOnCheckedChangeListener(null)
        btnSave.setOnClickListener(null)
        comment.removeTextChangedListener(commentTextWatcher)
        expiresSpinner.onItemSelectedListener = null
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = EditAddressPresenter(this, EditAddressRepository(), EditAddressState())
        return presenter
    }
}

