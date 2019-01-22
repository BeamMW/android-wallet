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

package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.entities.dto.WalletStatusDTO

/**
 * Created by vain onnellinen on 10/9/18.
 */
data class WalletStatus(private val source: WalletStatusDTO) {
    val available: Long = source.available
    val receiving: Long = source.receiving
    val sending: Long = source.sending
    val maturing: Long = source.maturing
    val updateLastTime: Long = source.updateLastTime
    val updateDone: Int = source.updateDone
    val updateTotal: Int = source.updateTotal
    val system: SystemState = SystemState(source.system)

    override fun toString(): String {
        return "available=$available receiving=$receiving sending=$sending maturing=$maturing updateLastTime=$updateLastTime updateDone=$updateDone updateTotal=$updateTotal system=$system"
    }
}
