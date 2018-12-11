package com.mw.beam.beamwallet.welcomeScreen

import android.content.Intent
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.main.MainActivity
import com.mw.beam.beamwallet.welcomeScreen.welcomeCreate.WelcomeCreateFragment
import com.mw.beam.beamwallet.welcomeScreen.welcomeDescription.WelcomeDescriptionFragment
import com.mw.beam.beamwallet.welcomeScreen.welcomeOpen.WelcomeOpenFragment
import com.mw.beam.beamwallet.welcomeScreen.welcomePasswords.WelcomePasswordsFragment
import com.mw.beam.beamwallet.welcomeScreen.welcomePhrase.WelcomePhraseFragment
import com.mw.beam.beamwallet.welcomeScreen.welcomeRestore.WelcomeRestoreFragment
import com.mw.beam.beamwallet.welcomeScreen.welcomeConfirm.WelcomeConfirmFragment

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomeActivity : BaseActivity<WelcomePresenter>(),
        WelcomeContract.View,
        WelcomeOpenFragment.WelcomeOpenHandler,
        WelcomeCreateFragment.WelcomeCreateHandler,
        WelcomeDescriptionFragment.GeneratePhraseHandler,
        WelcomePasswordsFragment.WelcomePasswordsHandler,
        WelcomePhraseFragment.WelcomePhrasesHandler,
        WelcomeConfirmFragment.WelcomeValidationHandler {
    private lateinit var presenter: WelcomePresenter

    override fun onControllerGetContentLayoutId() = R.layout.activity_welcome
    override fun getToolbarTitle(): String? = null

    override fun showOpenFragment() = showFragment(WelcomeOpenFragment.newInstance(), WelcomeOpenFragment.getFragmentTag(), WelcomeOpenFragment.getFragmentTag(), true)
    override fun showDescriptionFragment() = showFragment(WelcomeDescriptionFragment.newInstance(), WelcomeDescriptionFragment.getFragmentTag(), null, false)
    override fun showPasswordsFragment(phrases: Array<String>) = showFragment(WelcomePasswordsFragment.newInstance(phrases), WelcomePasswordsFragment.getFragmentTag(), null, false)
    override fun showPhrasesFragment() = showFragment(WelcomePhraseFragment.newInstance(), WelcomePhraseFragment.getFragmentTag(), null, false)
    override fun showValidationFragment(phrases: Array<String>) = showFragment(WelcomeConfirmFragment.newInstance(phrases), WelcomeConfirmFragment.getFragmentTag(), null, false)
    override fun showRestoreFragment() = showFragment(WelcomeRestoreFragment.newInstance(), WelcomeRestoreFragment.getFragmentTag(), null, false)
    override fun showCreateFragment() = showFragment(WelcomeCreateFragment.newInstance(), WelcomeCreateFragment.getFragmentTag(), WelcomeCreateFragment.getFragmentTag(), true)

    override fun createWallet() = presenter.onCreateWallet()
    override fun generatePhrase() = presenter.onGeneratePhrase()
    override fun openWallet() = presenter.onOpenWallet()
    override fun restoreWallet() = presenter.onRestoreWallet()
    override fun proceedToWallet() = presenter.onOpenWallet()
    override fun changeWallet() = presenter.onChangeWallet()

    override fun proceedToPasswords(phrases: Array<String>) = presenter.onProceedToPasswords(phrases)
    override fun proceedToValidation(phrases: Array<String>) = presenter.onProceedToValidation(phrases)

    override fun showMainActivity() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = WelcomePresenter(this, WelcomeRepository())
        return presenter
    }
}
