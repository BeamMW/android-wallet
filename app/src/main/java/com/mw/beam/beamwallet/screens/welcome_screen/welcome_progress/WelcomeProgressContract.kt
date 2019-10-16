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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_progress

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.helpers.NodeConnectionError
import com.mw.beam.beamwallet.core.helpers.Status
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import io.reactivex.subjects.Subject
import java.io.File

/**
 *  1/24/19.
 */
interface WelcomeProgressContract {
    interface View : MvpView {
        var enableOnBackPress: Boolean
        fun init(mode: WelcomeMode)
        fun updateProgress(progressData: OnSyncProgressData, mode: WelcomeMode, isDownloadProgress: Boolean = false)
        fun getMode(): WelcomeMode?
        fun getIsTrustedRestore(): Boolean?
        fun getPassword(): String?
        fun getSeed(): Array<String>?
        fun showWallet()
        fun showNoInternetMessage()
        fun showIncorrectNodeMessage()
        fun showCancelRestoreAlert()
        fun showCancelCreateAlert()
        fun showFailedRestoreAlert()
        fun showFailedDownloadRestoreFileAlert()
        fun getLifecycleOwner(): LifecycleOwner
        fun navigateToCreateFragment()
        fun changeCancelButtonVisibility(visible: Boolean)
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun onTryAgain()
        fun onCancel()
        fun onOkToCancelRestore()
        fun onCancelToCancelRestore()
        fun onBackPressed()
    }

    interface Repository : MvpRepository {
        fun getNodeProgressUpdated(): Subject<OnSyncProgressData>
        fun getNodeConnectionFailed(): Subject<NodeConnectionError>
        fun getSyncProgressUpdated(): Subject<OnSyncProgressData>
        fun getNodeStopped(): Subject<Any>
        fun getNodeThreadFinished(): Subject<Any>
        fun getFailedNodeStart(): Subject<Any>
        fun removeNode()
        fun removeWallet()
        fun getImportRecoveryState(pass: String?, seed: String?, file: File): Subject<OnSyncProgressData>
        fun createWallet(pass: String?, seed: String?): Status
        fun createRestoreFile(): File
        fun removeRestoreFile()
        fun setContext(c: Context)
    }
}
