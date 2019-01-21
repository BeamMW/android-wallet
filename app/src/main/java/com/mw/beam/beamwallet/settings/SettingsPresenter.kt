package com.mw.beam.beamwallet.settings

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 1/21/19.
 */
class SettingsPresenter(currentView: SettingsContract.View, currentRepository: SettingsContract.Repository)
    : BasePresenter<SettingsContract.View, SettingsContract.Repository>(currentView, currentRepository),
        SettingsContract.Presenter {

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true
}
