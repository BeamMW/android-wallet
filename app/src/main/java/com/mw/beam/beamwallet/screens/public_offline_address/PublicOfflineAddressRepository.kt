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

package com.mw.beam.beamwallet.screens.public_offline_address

import android.graphics.Bitmap
import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.AppConfig

import java.io.File
import java.io.FileOutputStream

/**
 *  3/4/19.
 */
class PublicOfflineAddressRepository : BaseRepository(), PublicOfflineAddressContract.Repository {
    override fun saveImage(bitmap: Bitmap): File {
        val file = File(AppConfig.CACHE_PATH, "qr_" + System.currentTimeMillis() + ".png")

        if (!file.parentFile.exists()) {
            file.parentFile.mkdir()
        } else {
            file.parentFile.listFiles().forEach { it.delete() }
        }
        file.createNewFile()

        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }
}
