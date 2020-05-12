package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.helpers.ChangeAction

data class OnNotificationDataWithAction(val action: ChangeAction, val notification: Notification)