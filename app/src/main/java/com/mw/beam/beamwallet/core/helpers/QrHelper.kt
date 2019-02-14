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

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel


/**
 * Created by vain onnellinen on 2/12/19.
 */
object QrHelper {

    @Throws(WriterException::class, NullPointerException::class)
    fun textToImage(text: String, width: Int, height: Int, darkColor: Int, lightColor: Int): Bitmap? {
        val bitMatrix: BitMatrix

        try {
            val hintMap = HashMap<EncodeHintType, Any>()
            hintMap[EncodeHintType.MARGIN] = 0
            hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.Q

            bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hintMap)
        } catch (e: IllegalArgumentException) {
            return null
        }

        val bitMatrixWidth = bitMatrix.width
        val bitMatrixHeight = bitMatrix.height
        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth
            for (x in 0 until bitMatrixWidth) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) lightColor else darkColor
            }
        }

        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)
        bitmap.setPixels(pixels, 0, width, 0, 0, bitMatrixWidth, bitMatrixHeight)

        return bitmap
    }
}
