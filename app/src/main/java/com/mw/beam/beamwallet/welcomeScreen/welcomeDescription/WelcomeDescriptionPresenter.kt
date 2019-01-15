package com.mw.beam.beamwallet.welcomeScreen.welcomeDescription

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 10/22/18.
 */
class WelcomeDescriptionPresenter(currentView: WelcomeDescriptionContract.View, currentRepository: WelcomeDescriptionContract.Repository)
    : BasePresenter<WelcomeDescriptionContract.View, WelcomeDescriptionContract.Repository>(currentView, currentRepository),
        WelcomeDescriptionContract.Presenter {

    override fun onGeneratePhrase() {
        view?.generatePhrase()
    }
}
