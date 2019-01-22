// Copyright 2018 Beam Development
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.mw.beam.beamwallet.welcomeScreen

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.utils.LogUtils

/**
 * Created by vain onnellinen on 10/19/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class WelcomeRepository : BaseRepository(), WelcomeContract.Repository {

    override fun isWalletInitialized(): Boolean {
        val result = Api.isWalletInitialized(AppConfig.DB_PATH)
        LogUtils.logResponse(result, object {}.javaClass.enclosingMethod.name)
        return result
    }
}
