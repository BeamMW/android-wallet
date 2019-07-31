package com.mw.beam.beamwallet.screens.welcome_screen.restore_owner_key

import com.mw.beam.beamwallet.base_screen.BasePresenter

class RestoreOwnerKeyPresenter(view: RestoreOwnerKeyContract.View?, repository: RestoreOwnerKeyContract.Repository)
    : BasePresenter<RestoreOwnerKeyContract.View, RestoreOwnerKeyContract.Repository>(view, repository), RestoreOwnerKeyContract.Presenter {
}