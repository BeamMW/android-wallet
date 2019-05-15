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

package com.mw.beam.beamwallet.screens.change_password

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.screens.change_password.check_old_pass.CheckOldPassFragment
import com.mw.beam.beamwallet.screens.create_password.PasswordFragment

/**
 * Created by vain onnellinen on 3/14/19.
 */
class ChangePassActivity : BaseActivity<ChangePassPresenter>(), ChangePassContract.View,
        CheckOldPassFragment.CheckOldPassHandler,
        PasswordFragment.PassChangedHandler {
    private lateinit var presenter: ChangePassPresenter

    override fun onControllerGetContentLayoutId() = R.layout.activity_change_password
    override fun getToolbarTitle(): String? = null

    override fun showCheckOldPassFragment() = showFragment(CheckOldPassFragment.newInstance(), CheckOldPassFragment.getFragmentTag(), null, false)
    override fun showCreatePasswordFragment() = showFragment(PasswordFragment.newInstance(), PasswordFragment.getFragmentTag(), CheckOldPassFragment.getFragmentTag(), true)

    override fun onCreateNewPass() = presenter.onCreateNewPass()
    override fun onPassChanged() = presenter.onPassChanged()

    override fun finishScreen() = finish()

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
            return
        }

        super.onBackPressed()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = ChangePassPresenter(this, ChangePassRepository())
        return presenter
    }
}
