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

package com.mw.beam.beamwallet.screens.welcome_screen

import android.content.Intent
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import com.mw.beam.beamwallet.screens.main.MainActivity
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_confirm.WelcomeConfirmFragment
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_create.WelcomeCreateFragment
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_description.WelcomeDescriptionFragment
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_open.WelcomeOpenFragment
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_passwords.WelcomePasswordsFragment
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_progress.WelcomeProgressFragment
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_restore.WelcomeRestoreFragment
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_seed.WelcomeSeedFragment

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomeActivity : BaseActivity<WelcomePresenter>(),
        WelcomeContract.View,
        WelcomeOpenFragment.OpenHandler,
        WelcomeCreateFragment.CreateHandler,
        WelcomeDescriptionFragment.DescriptionHandler,
        WelcomePasswordsFragment.PasswordsHandler,
        WelcomeSeedFragment.SeedHandler,
        WelcomeConfirmFragment.ConfirmHandler,
        WelcomeRestoreFragment.RestoreHandler,
        WelcomeProgressFragment.ProgressHandler {
    private lateinit var presenter: WelcomePresenter

    override fun onControllerGetContentLayoutId() = R.layout.activity_welcome
    override fun getToolbarTitle(): String? = null

    override fun showOpenFragment() = showFragment(WelcomeOpenFragment.newInstance(), WelcomeOpenFragment.getFragmentTag(), WelcomeOpenFragment.getFragmentTag(), true)
    override fun showDescriptionFragment() = showFragment(WelcomeDescriptionFragment.newInstance(), WelcomeDescriptionFragment.getFragmentTag(), null, false)
    override fun showPasswordsFragment(phrases: Array<String>) = showFragment(WelcomePasswordsFragment.newInstance(phrases), WelcomePasswordsFragment.getFragmentTag(), null, false)
    override fun showSeedFragment() = showFragment(WelcomeSeedFragment.newInstance(), WelcomeSeedFragment.getFragmentTag(), WelcomeSeedFragment.getFragmentTag(), true)
    override fun showValidationFragment(phrases: Array<String>) = showFragment(WelcomeConfirmFragment.newInstance(phrases), WelcomeConfirmFragment.getFragmentTag(), null, false)
    override fun showRestoreFragment() = showFragment(WelcomeRestoreFragment.newInstance(), WelcomeRestoreFragment.getFragmentTag(), null, false)
    override fun showCreateFragment() = showFragment(WelcomeCreateFragment.newInstance(), WelcomeCreateFragment.getFragmentTag(), WelcomeCreateFragment.getFragmentTag(), true)
    override fun showProgressFragment(mode: WelcomeMode) = showFragment(WelcomeProgressFragment.newInstance(mode), WelcomeProgressFragment.getFragmentTag(), null, true)

    override fun createWallet() = presenter.onCreateWallet() //calls from WelcomeCreateFragment
    override fun generateSeed() = presenter.onGenerateSeed()
    override fun openWallet() = presenter.onOpenWallet(WelcomeMode.OPEN) //calls from WelcomeOpenFragment
    override fun restoreWallet() = presenter.onRestoreWallet()
    override fun proceedToWallet() = presenter.onOpenWallet(WelcomeMode.CREATE) //calls from WelcomePasswordsFragment
    override fun changeWallet() = presenter.onChangeWallet()
    override fun showWallet() = presenter.onShowWallet()

    override fun proceedToPasswords(seed: Array<String>) = presenter.onProceedToPasswords(seed)
    override fun proceedToValidation(seed: Array<String>) = presenter.onProceedToValidation(seed)

    override fun showMainActivity() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onBackPressed() {
        val fragment = this.supportFragmentManager.findFragmentById(R.id.container)

        if (fragment != null && fragment is OnBackPressedHandler) {
            (fragment as? OnBackPressedHandler)?.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun finishNotRootTask() {
        // solves issue with extra tasks on some custom launchers https://issuetracker.google.com/issues/36907463
        if (!isTaskRoot) {
            finish()
            return
        }
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomePresenter(this, WelcomeRepository())
        return presenter
    }
}

// outstanding interface is needed to prevent a cycle in the inheritance hierarchy for this type
interface OnBackPressedHandler {
    fun onBackPressed()
}
