package com.mw.beam.beamwallet.screens.transactions

import android.content.Context
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout

class TransactionsLockableTabLayout(context: Context, attributeSet: AttributeSet) : TabLayout(context, attributeSet) {

    fun setMode(mode: TransactionsFragment.Mode) {
       // TransitionManager.beginDelayedTransition(parent as ViewGroup?)
        when(mode){
            TransactionsFragment.Mode.NONE -> this.visibility = View.VISIBLE
            TransactionsFragment.Mode.EDIT -> this.visibility = View.GONE
        }
    }
}