package com.mw.beam.beamwallet.welcomeScreen.welcomeDescription

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 10/22/18.
 */
class WelcomeDescriptionPresenter(currentView: WelcomeDescriptionContract.View, private val repository: WelcomeDescriptionContract.Repository)
    : BasePresenter<WelcomeDescriptionContract.View>(currentView),
        WelcomeDescriptionContract.Presenter {

    override fun onGeneratePhrase() {
        view?.generatePhrase()
    }
}
