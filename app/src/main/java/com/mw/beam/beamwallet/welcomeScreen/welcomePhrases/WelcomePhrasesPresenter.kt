package com.mw.beam.beamwallet.welcomeScreen.welcomePhrases

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 10/30/18.
 */
class WelcomePhrasesPresenter(currentView: WelcomePhrasesContract.View, private val repository: WelcomePhrasesContract.Repository)
    : BasePresenter<WelcomePhrasesContract.View>(currentView),
        WelcomePhrasesContract.Presenter {

    override fun viewIsReady() {
        view?.init()
        view?.configPhrases(repository.getPhrases())
    }

    override fun onNextPressed() {
        view?.showValidationFragment(repository.getPhrases())
    }

    override fun onCopyPressed() {
        val result = StringBuilder()

        for ((index, value) in repository.getPhrases().withIndex()) {
            result.append((index + 1).toString())
                    .append(" ")
                    .append(value)

            if (index != repository.getPhrases().lastIndex) {
                result.append("\n")
            }
        }

        view?.copyToClipboard(result.toString())
    }
}
