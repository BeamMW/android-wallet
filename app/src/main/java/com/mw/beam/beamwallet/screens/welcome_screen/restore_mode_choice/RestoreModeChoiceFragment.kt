package com.mw.beam.beamwallet.screens.welcome_screen.restore_mode_choice

import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import kotlinx.android.synthetic.main.fragment_restore_mode_choice.*

class RestoreModeChoiceFragment : BaseFragment<RestoreModeChoicePresenter>(), RestoreModeChoiceContract.View {
    private val args by lazy {
        RestoreModeChoiceFragmentArgs.fromBundle(arguments!!)
    }

    override fun getPassword(): String = args.pass

    override fun getSeed(): Array<String> = args.seed

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_restore_mode_choice

    override fun getToolbarTitle(): String? = getString(R.string.restore_wallet)

    override fun addListeners() {
        btnNext.setOnClickListener {
            presenter?.onNextPressed(automaticRestore.isChecked)
        }
    }

    override fun showAutoRestoreWarning() {
        showWarning(true)
    }

    override fun showNodeRestoreWarning() {
        showWarning(false)
    }

    private fun showWarning(isAutomaticRestore: Boolean) {
        val message = getString(if (isAutomaticRestore) R.string.automatic_restore_warning else R.string.node_restore_warning)
        showAlert(
                message = message,
                btnConfirmText = getString(R.string.understand),
                onConfirm = { presenter?.onConfirmRestorePressed(isAutomaticRestore) },
                btnCancelText = getString(R.string.cancel)
        )
    }

    override fun clearListeners() {
        btnNext.setOnClickListener(null)
    }

    override fun showAutomaticProgressRestore(pass: String, seed: Array<String>) {
        findNavController().navigate(RestoreModeChoiceFragmentDirections.actionRestoreModeChoiceFragmentToWelcomeProgressFragment(pass, WelcomeMode.RESTORE_AUTOMATIC.name, seed))
    }

    override fun showRestoreOwnerKey(pass: String, seed: Array<String>) {
        findNavController().navigate(RestoreModeChoiceFragmentDirections.actionRestoreModeChoiceFragmentToRestoreOwnerKeyFragment(pass, seed))
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return RestoreModeChoicePresenter(this, RestoreModeChoiceRepository())
    }
}