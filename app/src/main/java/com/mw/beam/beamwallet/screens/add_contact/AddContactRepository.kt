package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO

class AddContactRepository: BaseRepository(), AddContactContract.Repository {

    override fun saveContact(address: String, name: String) {
        getResult("saveContact") {
            var identity = ""
            var walledId = address

            if(AppManager.instance.wallet?.isToken(address) == true)
            {
                val params = AppManager.instance.wallet?.getTransactionParameters(address, false)
                identity = params?.identity ?: ""
                if((params?.address ?: "").isNotEmpty()) {
                    walledId = (params?.address ?: "")
                }
            }

            AppManager.instance.removeIgnoredAddress(address)

            wallet?.saveAddress(WalletAddressDTO(walledId, name, "", System.currentTimeMillis(), 0, 0, identity, address), false)
            wallet?.getAddresses(false)
        }
    }
}