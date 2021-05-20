package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO

class AddContactRepository: BaseRepository(), AddContactContract.Repository {

    override fun saveContact(address: String, name: String) {
        getResult("saveContact") {
            var identity = ""

            if(AppManager.instance.wallet?.isToken(address) == true)
            {
                val params = AppManager.instance.wallet?.getTransactionParameters(address, false)
                identity = params?.identity ?: ""
            }

            wallet?.saveAddress(WalletAddressDTO(address, name, "", System.currentTimeMillis(), 0, 0, identity, address), false)
        }
    }
}