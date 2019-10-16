package com.mw.beam.beamwallet.screens.addresses

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.mw.beam.beamwallet.core.helpers.ScreenHelper

class AddressesLockableViewPager(context: Context, attributeSet: AttributeSet) : ViewPager(context, attributeSet) {

    private var locked = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (locked) {
            true -> false
            false -> super.onTouchEvent(event)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return when (locked) {
            true -> false
            false -> super.onInterceptTouchEvent(event)
        }
    }

    fun setMode(mode: AddressesFragment.Mode) {
        when(mode){
            AddressesFragment.Mode.EDIT -> {
                val p = ScreenHelper.dpToPx(context,20)
                setPadding(0,p,0,p)
                this.locked = true
            }
            else -> {
                setPadding(0,0,0,0)
                this.locked = false
            }
        }
    }

}