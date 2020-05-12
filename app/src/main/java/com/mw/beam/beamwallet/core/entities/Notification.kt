package com.mw.beam.beamwallet.core.entities

enum class NotificationType(val value: Int) {
    Transaction(0), News(1), Address(2), Version(3);

    companion object {
        private val map: HashMap<Int, NotificationType> = HashMap()

        init {
            values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int): NotificationType {
            return map[type] ?: Version
        }
    }
}

class Notification(val type: NotificationType, val id: String, val objId: String, var isRead: Boolean, var isSent: Boolean, val createdTime: Long, val text: String) {

}
