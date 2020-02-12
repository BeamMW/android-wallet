package com.mw.beam.beamwallet.screens.timer_overlay_dialog

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.view.animation.AccelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment

import kotlin.concurrent.schedule
import kotlin.math.ceil

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

import java.util.*
import android.graphics.LightingColorFilter


class TimerOverlayDialog: BaseDialogFragment<TimerOverlayPresenter>(), TimerOverlayContract.View {

    private var onConfirm: ((Boolean) -> Unit)? = null

    companion object {
        fun getFragmentTag(): String = TimerOverlayDialog::class.java.simpleName

        fun newInstance(onConfirm: (Boolean) -> Unit) = TimerOverlayDialog().apply {
            this.onConfirm = onConfirm
        }
    }

    private var snackbarLifeTime: Long = 4000
    private val period: Long = 10

    private var timer: Timer? = null
    private var currentMillis: Long = 0
    private var smoothAnimation: ObjectAnimator? = null

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return TimerOverlayPresenter(this, TimerOverlayRepository())
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_timer_overlay

    override fun init() {}

    override fun onStart() {
        super.onStart()

        smoothAnimation?.cancel()
        smoothAnimation = null

        timer?.cancel()
        timer = null

        view?.findViewById<TextView>(R.id.undoTime)?.text = millisToSecond(snackbarLifeTime)

        val progressBar = view?.findViewById<ProgressBar>(R.id.progressTimer)
        progressBar?.max = snackbarLifeTime.toInt()
        progressBar?.progress = snackbarLifeTime.toInt()

        smoothAnimation = ObjectAnimator.ofInt(progressBar, "progress", progressBar!!.progress, progressBar.max)
        smoothAnimation?.duration = 100
        smoothAnimation?.interpolator = AccelerateInterpolator()

        currentMillis = snackbarLifeTime

        timer = Timer()
        timer?.schedule(0, period) {
         view?.handler?.post{
             currentMillis -= period
             if (currentMillis <= 0) {

                 onConfirm?.invoke(true)

                 dismiss()

             } else  {
                 view?.findViewById<TextView>(R.id.undoTime)?.text = millisToSecond(currentMillis)
                 view?.findViewById<ProgressBar>(R.id.progressTimer)?.progress = (snackbarLifeTime - currentMillis).toInt()
             }
         }
        }

        smoothAnimation?.start()
    }

    private fun millisToSecond(millis: Long): String {
        return ceil(millis.toDouble() / 1000).toInt().toString()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        onConfirm?.invoke(false)
    }
}