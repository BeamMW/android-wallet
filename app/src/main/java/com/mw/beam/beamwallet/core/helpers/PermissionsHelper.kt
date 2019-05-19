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

package com.mw.beam.beamwallet.core.helpers

import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

/**
 * Created by vain onnellinen on 3/18/19.
 */
object PermissionsHelper {
    const val REQUEST_CODE_PERMISSION = 1043
    const val PERMISSIONS_CAMERA = Manifest.permission.CAMERA

    fun requestPermissions(activity: AppCompatActivity, permission: String, requestCode: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(arrayOf(permission), requestCode)
            false
        } else {
            true
        }
    }
}

enum class PermissionStatus {
    GRANTED, DECLINED, NEVER_ASK_AGAIN
}
