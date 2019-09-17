package com.mw.beam.beamwallet.screens.welcome_screen.restore_mode_choice

import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import kotlinx.android.synthetic.main.fragment_restore_mode_choice.*
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionRequest
import android.content.Intent
import android.net.Uri
import android.provider.Settings


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

            PreferencesManager.putBoolean(PreferencesManager.KEY_RESTORED_FROM_TRUSTED, !automaticRestore.isChecked)

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
                title = getString(R.string.restore_wallet),
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
        Dexter.withActivity(activity)
                .withPermission(
                     android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        findNavController().navigate(RestoreModeChoiceFragmentDirections.actionRestoreModeChoiceFragmentToWelcomeProgressFragment(pass, WelcomeMode.RESTORE_AUTOMATIC.name, seed))
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
                                        intent.data = Uri.fromParts("package", context?.getPackageName(), null)
                                        startActivity(intent)

                                    },
                                    title = getString(R.string.send_permission_required_title),
                                    btnCancelText = getString(R.string.cancel))
                        }
                    }
                }).check()

    }

    override fun showRestoreOwnerKey(pass: String, seed: Array<String>) {
        findNavController().navigate(RestoreModeChoiceFragmentDirections.actionRestoreModeChoiceFragmentToRestoreOwnerKeyFragment(pass, seed))
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return RestoreModeChoicePresenter(this, RestoreModeChoiceRepository())
    }
}