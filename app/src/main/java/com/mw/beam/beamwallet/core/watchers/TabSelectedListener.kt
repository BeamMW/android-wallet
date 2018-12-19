package com.mw.beam.beamwallet.core.watchers

import android.support.design.widget.TabLayout

/**
 * Created by vain onnellinen on 12/18/18.
 */
interface TabSelectedListener : TabLayout.OnTabSelectedListener {
    override fun onTabReselected(p0: TabLayout.Tab?) {}
    override fun onTabUnselected(p0: TabLayout.Tab?) {}
}
