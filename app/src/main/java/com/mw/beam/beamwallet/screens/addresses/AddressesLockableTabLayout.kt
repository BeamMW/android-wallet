package com.mw.beam.beamwallet.screens.addresses

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout

class AddressesLockableTabLayout(context: Context, attributeSet: AttributeSet) : TabLayout(context, attributeSet) {
    private var transition: AutoTransition = AutoTransition()

    init {
        transition.duration = 525
    }

    fun setMode(mode: AddressesFragment.Mode) {
        TransitionManager.beginDelayedTransition(parent as ViewGroup?, transition)
        when(mode){
            AddressesFragment.Mode.NONE -> this.visibility = View.VISIBLE
            AddressesFragment.Mode.EDIT -> this.visibility = View.GONE
        }

    }

}