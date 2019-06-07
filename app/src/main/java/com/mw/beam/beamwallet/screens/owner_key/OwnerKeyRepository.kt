package com.mw.beam.beamwallet.screens.owner_key

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.helpers.PreferencesManager

class OwnerKeyRepository: BaseRepository(), OwnerKeyContract.Repository {
    override fun getOwnerKey(): String {
        val pass = PreferencesManager.getString(PreferencesManager.KEY_PASSWORD)

        if (pass.isNullOrBlank()) {
            return ""
        }

        return getResult("getOwnerKey") { wallet?.exportOwnerKey(pass) ?: "" }
    }
}