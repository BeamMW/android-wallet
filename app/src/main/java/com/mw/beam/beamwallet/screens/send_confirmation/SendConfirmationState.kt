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

package com.mw.beam.beamwallet.screens.send_confirmation

import com.mw.beam.beamwallet.core.entities.WalletAddress

class SendConfirmationState {
    var contact: WalletAddress? = null
    val addresses = HashMap<String, WalletAddress>()
    var outgoingAddress: String = ""
    var token: String = ""
    var maxPrivacy = false
    var isOffline = false
    var comment: String? = null
    var amount: Long = 0
    var fee: Long = 0
    var shieldedInputsFee: Long = 0
}