package com.mw.beam.beamwallet.screens.language

import com.mw.beam.beamwallet.base_screen.BasePresenter

class LanguagePresenter(view: LanguageContract.View?, repository: LanguageContract.Repository)
    : BasePresenter<LanguageContract.View, LanguageContract.Repository>(view, repository), LanguageContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(repository.getLanguages(), repository.getCurrentLanguageIndex())
    }

    override fun onRestartPressed(index: Int) {
        repository.setLanguage(index)
        view?.logOut()
    }

    override fun onSelectLanguage(index: Int) {
        if (index != repository.getCurrentLanguageIndex()) {
            view?.showConfirmDialog(index)
        }
    }

}