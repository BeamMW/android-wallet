package com.mw.beam.beamwallet.core.watchers

import android.text.TextWatcher

/**
 * Created by vain onnellinen on 10/24/18.
 */
interface TextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
}
