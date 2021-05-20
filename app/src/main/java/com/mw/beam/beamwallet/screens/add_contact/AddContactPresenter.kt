package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.QrHelper
import io.reactivex.disposables.Disposable

class AddContactPresenter(view: AddContactContract.View?, repository: AddContactContract.Repository, private val state: AddContactState):
        BasePresenter<AddContactContract.View, AddContactContract.Repository>(view, repository), AddContactContract.Presenter {


    override fun onViewCreated() {
        super.onViewCreated()

        val address: String? = view?.getAddressFromArguments()
        if (!address.isNullOrBlank()) {
            view?.setAddress(address)
        }
    }

    override fun onCancelPressed() {
        view?.close()
    }


    override fun onSavePressed() {
        val address = view?.getAddress() ?: ""
        val name = view?.getName() ?: ""

        if (AppManager.instance.isValidAddress(address)) {
            val oldAddress = AppManager.instance.getAddress(address)
            if (oldAddress!=null) {
                view?.showTokenError(oldAddress)
            }
            else{
                repository.saveContact(address, name.trim())
                view?.close()
            }
        } else {
            view?.showTokenError(null)
        }
    }


    override fun checkAddress() {
        val address = view?.getAddress() ?: ""

        if (AppManager.instance.isValidAddress(address)) {
            if (state.addresses.containsKey(address)) {
                view?.showTokenError(state.addresses[address])
            }
            else{
                view?.hideTokenError()
            }
        } else {
            view?.showTokenError(null)
        }
    }

    override fun onScannedQR(text: String?) {
        if (text == null) return

        val scannedAddress = QrHelper.getScannedAddress(text)

        if (AppManager.instance.isValidAddress(scannedAddress)) {
            view?.setAddress(scannedAddress)
        } else {
            view?.vibrate(100)
            view?.showErrorNotBeamAddress()
        }
    }

    override fun onScanPressed() {
        view?.navigateToScanQr()
    }

    override fun onTokenChanged() {
        view?.hideTokenError()
    }
}