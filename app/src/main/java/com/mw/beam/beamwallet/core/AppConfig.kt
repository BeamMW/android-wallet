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

package com.mw.beam.beamwallet.core

import com.mw.beam.beamwallet.BuildConfig
import java.util.*

/**
 *  10/1/18.
 */
object AppConfig {
    const val APP_TAG = "BeamWallet"
    const val APP_VERSION = "version: ${BuildConfig.VERSION_NAME}.00" //code: ${BuildConfig.VERSION_CODE}
    // Single source for the "vX.Y" label shown on the welcome/settings screens.
    const val APP_VERSION_LABEL = "v${BuildConfig.VERSION_NAME}"
    const val FLAVOR_MAINNET = "mainnet"
    const val MAINNET_FORK_HEIGHT = 321321
    const val LOG_PATTERN = "{d yyyy-MM-dd hh:mm:ss.SSS} {l}/{t}: {m}"
    const val SHARE_TYPE = "application/zip"
    const val AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
    const val SUPPORT_EMAIL = "support@beam.mw"
    const val DB_FILE_NAME = "wallet.db"
    const val DB_FILE_NAME_RECOVER = "wallet_recover.db"
    const val NODE_DB_FILE_NAME = "node.db"
    const val NODE_JOURNAL_FILE_NAME = "node.db-journal"
    const val NODE_JOURNAL_FILE_NAME_RECOVER = "node.db-journal_recover"
    const val BEAM_SITE_LINK = "https://www.beam.mw/"
    const val BEAM_EXCHANGES_LINK = "$BEAM_SITE_LINK#exchanges"
    var EXPLORER_PREFIX = ""
    var NODE_ADDRESS = ""
    var DB_PATH = ""
    var LOG_PATH = ""
    var ZIP_PATH = ""
    var TRANSACTIONS_PATH = ""
    var CACHE_PATH = ""
    var LOCALE: Locale = Locale.ENGLISH

    val EXPLORER_LINK
        get() = "https://${EXPLORER_PREFIX}explorer.beam.mw/"

    var FORK_HEIGHT = 0

    fun buildTransactionLink(kernelId: String) = "${EXPLORER_LINK}block?kernel_id=$kernelId"
    fun buildAssetIdLink(assetId: Int) = "${EXPLORER_LINK}assets/details/$assetId"
}