package com.mw.beam.beamwallet.welcomeScreen.welcomePhrase

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 10/30/18.
 */
class WelcomePhrasePresenter(currentView: WelcomePhraseContract.View, private val repository: WelcomePhraseContract.Repository)
    : BasePresenter<WelcomePhraseContract.View>(currentView),
        WelcomePhraseContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.configPhrases(repository.phrases)
    }

    override fun onNextPressed() {
        view?.showValidationFragment(repository.phrases)
    }

    override fun onCopyPressed() {
        val result = StringBuilder()

        for ((index, value) in repository.phrases.withIndex()) {
            result.append((index + 1).toString())
                    .append(" ")
                    .append(value)

            if (index != repository.phrases.lastIndex) {
                result.append("\n")
            }
        }

        view?.copyToClipboard(result.toString())
        view?.showCopiedAlert()
    }
}
