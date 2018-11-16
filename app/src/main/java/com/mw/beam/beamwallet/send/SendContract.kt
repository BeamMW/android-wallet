package com.mw.beam.beamwallet.send

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 11/13/18.
 */
interface SendContract {

    interface View : MvpView {
        fun init()
    }

    interface Presenter : MvpPresenter<View>
    interface Repository : MvpRepository
}
