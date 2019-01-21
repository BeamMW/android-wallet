package com.mw.beam.beamwallet.settings

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 1/21/19.
 */
interface SettingsContract {
    interface View : MvpView
    interface Presenter : MvpPresenter<View>
    interface Repository : MvpRepository
}
