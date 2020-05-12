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

package com.mw.beam.beamwallet.screens.wallet

/**
 *  2/22/19.
 */
class NavItem(val id: ID, val iconResId: Int, var text: String, var isSelected : Boolean = false, var unreadCount: Int = 0) {
    enum class ID {
        WALLET, ADDRESS_BOOK, UTXO, DASHBOARD, NOTIFICATIONS, HELP, SETTINGS
    }
}
