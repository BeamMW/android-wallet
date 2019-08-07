/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_create

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.LocaleHelper
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.removeDatabase
import com.mw.beam.beamwallet.core.helpers.removeNodeDatabase

/**
 * Created by vain onnellinen on 12/4/18.
 */
class WelcomeCreateRepository : BaseRepository(), WelcomeCreateContract.Repository {
    override fun isUnfinishedRestore(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_UNFINISHED_RESTORE)
    }

    override fun clearAllData() {
        App.wallet = null
        removeDatabase()
        removeNodeDatabase()
    }

    override fun getCurrentLanguage(): LocaleHelper.SupportedLanguage {
        return LocaleHelper.getCurrentLanguage()
    }

}
