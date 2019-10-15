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

package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.mw.beam.beamwallet.R
import java.util.*
import kotlin.concurrent.schedule
import android.animation.ObjectAnimator
import android.view.animation.AccelerateInterpolator
import com.mw.beam.beamwallet.core.views.*
import android.view.animation.AnimationUtils
import android.view.animation.Animation
import android.view.animation.ScaleAnimation


class SnackBarsView: FrameLayout {
    private var snackbarLifeTime: Long = 5000
    private val period: Long = 10

    private var info: SnackBarInfo? = null
    private var currentView: View? = null
    private var timer: Timer? = null
    private var currentMillis: Long = 0
    private var smoothAnimation:ObjectAnimator? = null

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    fun show(message: String, onDismiss: (() -> Unit)? = null, onUndo: (() -> Unit)? = null) {
     //   dismiss()

        smoothAnimation?.cancel()
        smoothAnimation = null

        timer?.cancel()
        timer = null

        info = null

        removeAllViews()
        removeAllViewsInLayout()

        info = SnackBarInfo(message, onDismiss, onUndo)

        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)

        val view = LayoutInflater.from(context).inflate(R.layout.snackbar_layout, this)
        view.startAnimation(animation)
        view?.setOnClickListener {  }

        view.findViewById<TextView>(R.id.contentText).text = message

        if (onUndo != null) {
            snackbarLifeTime = 5000

            view.findViewById<View>(R.id.timerView).visibility = View.VISIBLE

            val btnUndo = view.findViewById<View>(R.id.btnUndo)
            btnUndo.visibility = View.VISIBLE
            btnUndo.setOnClickListener { undo() }

            view.findViewById<TextView>(R.id.undoTime).text = millisToSecond(snackbarLifeTime)

            val progressBar = view.findViewById<ProgressBar>(R.id.progressTimer)
            progressBar.max = snackbarLifeTime.toInt()
            progressBar.progress = snackbarLifeTime.toInt()

            smoothAnimation = ObjectAnimator.ofInt(progressBar, "progress", progressBar!!.progress, progressBar.max)
            smoothAnimation?.duration = 100
            smoothAnimation?.interpolator = AccelerateInterpolator()
        }
        else{
            snackbarLifeTime = 3000
        }

        startNewTimer()

        currentView = view
    }

    fun dismiss() {
        info?.onDismiss?.invoke()
        clear()
    }

    fun undo() {
        info?.onUndo?.invoke()
        clear()
    }

    private fun clear() {
        smoothAnimation?.cancel()
        smoothAnimation = null

        timer?.cancel()
        timer = null

        info = null

        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {

            }
            override fun onAnimationRepeat(arg0: Animation) {

            }
            override fun onAnimationEnd(arg0: Animation) {
                removeAllViews()
            }
        })
        currentView?.startAnimation(animation)
    }

    private fun millisToSecond(millis: Long): String {
        return Math.ceil(millis.toDouble() / 1000).toInt().toString()
    }

    private fun startNewTimer() {
        currentMillis = snackbarLifeTime

        timer = Timer()
        timer?.schedule(0, period) {
            handler.post {
                currentMillis -= period
                if (currentMillis <= 0) {
                    dismiss()
                } else if (info?.onUndo != null) {
                    currentView?.findViewById<TextView>(R.id.undoTime)?.text = millisToSecond(currentMillis)
                    currentView?.findViewById<ProgressBar>(R.id.progressTimer)?.progress = (snackbarLifeTime - currentMillis).toInt()
                }
            }
        }

        smoothAnimation?.start()
    }

    private data class SnackBarInfo(val message: String, val onDismiss: (() -> Unit)? = null, val onUndo: (() -> Unit)? = null)
}