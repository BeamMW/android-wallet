package com.mw.beam.beamwallet.welcomeScreen.welcomeValidation

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeValidationPresenter(currentView: WelcomeValidationContract.View, private val repository: WelcomeValidationContract.Repository)
    : BasePresenter<WelcomeValidationContract.View>(currentView),
        WelcomeValidationContract.Presenter {

    override fun viewIsReady() {
        view?.init()
        repository.phrases = view?.getData()

        if (repository.phrases != null) {
            view?.configPhrases(repository.getPhrasesToValidate(), repository.phrases!!)
        }
    }

    override fun onPhraseChanged() {
        view?.handleNextButton()
    }

    override fun onNextPressed() {
        view?.showPasswordsFragment()
    }
}
