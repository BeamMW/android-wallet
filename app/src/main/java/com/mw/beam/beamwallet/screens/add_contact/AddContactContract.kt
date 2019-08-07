package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.Tag

interface AddContactContract {
    interface View : MvpView {
        fun getAddress(): String
        fun getName(): String
        fun showTokenError()
        fun hideTokenError()
        fun close()
        fun navigateToScanQr()
        fun navigateToAddNewCategory()
        fun setAddress(address: String)
        fun setupTagAction(isEmptyTags: Boolean)
        fun showTagsDialog(selectedTags: List<Tag>)
        fun showCreateTagDialog()
    }

    interface Presenter : MvpPresenter<View> {
        fun onTokenChanged()
        fun onCancelPressed()
        fun onAddNewCategoryPressed()
        fun onSavePressed()
        fun onScanPressed()
        fun onScannedQR(text: String?)
        fun onTagActionPressed()
        fun onSelectTags(tags: List<Tag>)
        fun onCreateNewTagPressed()
    }

    interface Repository: MvpRepository {
        fun saveContact(address: String, name: String, tags: List<Tag>)
        fun getTags(): List<Tag>
    }
}