package com.mw.beam.beamwallet.screens.welcome_screen.restore_mode_choice

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import java.io.File

class RestoreModeChoicePresenter(view: RestoreModeChoiceContract.View?, repository: RestoreModeChoiceContract.Repository)
    : BasePresenter<RestoreModeChoiceContract.View, RestoreModeChoiceContract.Repository>(view, repository), RestoreModeChoiceContract.Presenter {

    override fun onStart() {
        super.onStart()

        repository.removeWallet()
    }

    override fun onNextPressed(isAutomaticRestore: Boolean) {
        PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS,"")
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE,true)

        if (isAutomaticRestore) {
            view?.showAutoRestoreWarning()
        }
        else {
            view?.showNodeRestoreWarning()
        }
    }

    override fun onConfirmRestorePressed(isAutomaticRestore: Boolean) {
        view?.apply {
            repository.saveStartRestoreFlag()
            if (isAutomaticRestore) {
                showAutomaticProgressRestore(getPassword(), getSeed())
            } else {
                showRestoreOwnerKey(getPassword(), getSeed())
            }
        }
    }
}