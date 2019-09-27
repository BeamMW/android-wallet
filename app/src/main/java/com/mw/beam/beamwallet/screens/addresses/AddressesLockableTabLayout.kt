package com.mw.beam.beamwallet.screens.addresses

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_addresses.*

class AddressesLockableTabLayout(context: Context, attributeSet: AttributeSet) : TabLayout(context, attributeSet) {
    var swipable = true

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = !swipable

}