package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import com.mw.beam.beamwallet.base_screen.BasePresenter

class RestoreTustedNodePresenter(view: RestoreTustedNodeContract.View?, repository: RestoreTustedNodeContract.Repository)
    : BasePresenter<RestoreTustedNodeContract.View, RestoreTustedNodeContract.Repository>(view, repository), RestoreTustedNodeContract.Presenter {
}