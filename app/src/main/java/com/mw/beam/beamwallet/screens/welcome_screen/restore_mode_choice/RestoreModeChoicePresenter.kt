package com.mw.beam.beamwallet.screens.welcome_screen.restore_mode_choice

import com.mw.beam.beamwallet.base_screen.BasePresenter

class RestoreModeChoicePresenter(view: RestoreModeChoiceContract.View?, repository: RestoreModeChoiceContract.Repository)
    : BasePresenter<RestoreModeChoiceContract.View, RestoreModeChoiceContract.Repository>(view, repository), RestoreModeChoiceContract.Presenter {

    override fun onNextPressed(isAutomaticRestore: Boolean) {
        view?.apply {
            if (isAutomaticRestore) {
                showAutomaticProgressRestore(getPassword(), getSeed())
            } else {
                showRestoreOwnerKey(getPassword(), getSeed())
            }
        }
    }
}