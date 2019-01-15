package com.mw.beam.beamwallet.welcomeScreen.welcomeConfirm

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeConfirmPresenter(currentView: WelcomeConfirmContract.View, currentRepository: WelcomeConfirmContract.Repository)
    : BasePresenter<WelcomeConfirmContract.View, WelcomeConfirmContract.Repository>(currentView, currentRepository),
        WelcomeConfirmContract.Presenter {

    override fun onCreate() {
        super.onCreate()
        repository.phrases = view?.getData()
    }

    override fun onViewCreated() {
        super.onViewCreated()

        if (repository.phrases != null) {
            view?.configPhrases(repository.getPhrasesToValidate(), repository.phrases!!)
        }
    }

    override fun onPhraseChanged() {
        view?.handleNextButton()
    }

    override fun onNextPressed() {
        view?.showPasswordsFragment(repository.phrases ?: return)
    }
}
