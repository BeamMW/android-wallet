package com.mw.beam.beamwallet.screens.asset_info

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.helpers.convertToAssetString
import com.mw.beam.beamwallet.core.helpers.exchangeValueAsset

import kotlinx.android.synthetic.main.fragment_asset_balance.*


private const val ID = "id"

class AssetBalanceFragment : Fragment() {
    private var assetId = 0
    private var shouldExpandDetails = true
    private var shouldExpandLocked = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            assetId = it.getInt(ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_asset_balance, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(id: Int) =
                AssetBalanceFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ID, id)
                    }
                }
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val asset = AssetManager.instance.getAsset(assetId)
        if (asset != null) {
            assetNameLabel.text = asset.unitName.toUpperCase()
            assetIcon.setImageResource(asset.image)


            availableLabel.text = asset.available.convertToAssetString(asset.unitName)
            availableSecondLabel.text = asset.available.exchangeValueAsset(assetId)


            regularLabel.text = (asset.available - asset.shielded).convertToAssetString(asset.unitName)
            regularSecondLabel.text = (asset.available - asset.shielded).exchangeValueAsset(assetId)


            maxPrivacyLabel.text = asset.shielded.convertToAssetString(asset.unitName)
            maxPrivacySecondLabel.text = asset.shielded.exchangeValueAsset(assetId)

            val lockedBalance = asset.maxPrivacy + asset.maturing + asset.sending + asset.receiving
            val changeBalance = asset.sending + asset.receiving

            lockedLabel.text = lockedBalance.convertToAssetString(asset.unitName)
            lockedSecondLabel.text = lockedBalance.exchangeValueAsset(assetId)


            maturingLabel.text = asset.maturing.convertToAssetString(asset.unitName)
            maturingSecondLabel.text = asset.maturing.exchangeValueAsset(assetId)

            changeLabel.text = changeBalance.convertToAssetString(asset.unitName)
            changeSecondLabel.text = changeBalance.exchangeValueAsset(assetId)

            maxPrivacyLabel2.text = asset.maxPrivacy.convertToAssetString(asset.unitName)
            maxPrivacySecondLabel2.text = asset.maxPrivacy.exchangeValueAsset(assetId)

            if (asset.maxPrivacy > 0) {
                morePrivacyButton.visibility = View.VISIBLE
            }
            else {
                morePrivacyButton.visibility = View.GONE
            }
        }

        detailsExpandLayout.setOnClickListener {
            shouldExpandDetails = !shouldExpandDetails
            animateDropDownIcon(detailsArrowView, !shouldExpandDetails)
            beginTransition()

            val contentVisibility = if (shouldExpandDetails) View.VISIBLE else View.GONE
            availableLayout.visibility = contentVisibility
            regularLayout.visibility = contentVisibility
            maxPrivacyLayout.visibility = contentVisibility
        }

        lockedExpandLayout.setOnClickListener {
            shouldExpandLocked = !shouldExpandLocked
            animateDropDownIcon(detailsArrowView2, !shouldExpandLocked)
            beginTransition()

            val contentVisibility = if (shouldExpandLocked) View.VISIBLE else View.GONE
            lockedLabel.visibility = contentVisibility
            lockedSecondLabel.visibility = contentVisibility
            maturingLayout.visibility = contentVisibility
            changeLayout.visibility = contentVisibility
            maxPrivacyLayout2.visibility = contentVisibility
        }
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 360f else 180f
        val angleTo = if (shouldExpand) 180f else 360f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }

    private fun beginTransition() {
        TransitionManager.beginDelayedTransition(mainConstraintLayout, AutoTransition().apply {
        })
    }
}