package com.mw.beam.beamwallet.core.helpers

import android.content.Context


class PasteManager {

    companion object {
        fun getPasteData(context: Context?):String {
            val clipboard = context?.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager

            var pasteData = "";

            if (clipboard != null)
            {
                if (clipboard?.hasPrimaryClip()!!) {
                    val item = clipboard?.getPrimaryClip()?.getItemAt(0);

                    pasteData = item?.getText().toString();
                }
            }

            return pasteData
        }
    }

}