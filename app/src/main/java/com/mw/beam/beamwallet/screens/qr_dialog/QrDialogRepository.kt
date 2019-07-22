package com.mw.beam.beamwallet.screens.qr_dialog

import android.graphics.Bitmap
import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.AppConfig
import java.io.File
import java.io.FileOutputStream


class QrDialogRepository: BaseRepository(), QrDialogContract.Repository {

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