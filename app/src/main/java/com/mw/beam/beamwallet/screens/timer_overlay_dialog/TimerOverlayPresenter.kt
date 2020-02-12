package com.mw.beam.beamwallet.screens.timer_overlay_dialog

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.DelayedTask

class TimerOverlayPresenter(view: TimerOverlayContract.View?, repository: TimerOverlayContract.Repository)
    : BasePresenter<TimerOverlayContract.View, TimerOverlayContract.Repository>(view, repository), TimerOverlayContract.Presenter {


    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }
}