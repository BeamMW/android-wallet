package com.mw.beam.beamwallet.welcomeScreen.welcomeDescription

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 10/22/18.
 */
interface WelcomeDescriptionContract {
    interface View : MvpView {
        fun generatePhrase()
    }
    interface Presenter : MvpPresenter<View> {
        fun onGeneratePhrase()
    }
    interface Repository : MvpRepository
}
