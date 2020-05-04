package io.github.wechaty.io.github.wechaty.schemas

data class PuppetQRCodeScanEvent(val qrcoode: String, val status: Int) {
    var data: String? = null
}

data class PuppetRoomInviteEvent(val inviterId: String, val roomId: String)

data class PuppetRoomJoinEvent(
        val inviteeNameList: List<String>,
        val inviterName: String,
        val roomId: String,
        val timestamp: Long
)

data class PuppetRoomLeaveEvent(
        val leaverNameList: List<String>,
        val removerName: String,
        val roomId: String,
        val timestamp: Long  // Unix Timestamp, in seconds
)

data class PuppetRoomTopicEvent(
        val changerName: String,
        val roomId: String,
        val topic: String,
        val timestamp: Long
)

val CHAT_EVENT_DICT = mapOf(
        "friendship" to "receive a friend request",
        "login" to "puppet had logined",
        "logout" to "puppet had logouted",
        "message" to "received a new message",
        "room-invite" to "received a room invitation",
        "room-join" to "be added to a room",
        "room-leave" to "leave or be removed from a room",
        "room-topic" to "room topic had been changed",
        "scan" to "a QR Code scan is required"
)

val PUPPET_EVENT_DICT = mapOf(
        "dong" to "emit this event if you received a ding() call",
        "error" to "emit an Error instance when there's any Error need to report to Wechaty",
        "ready" to "emit this event after the puppet is ready(you define it)",
        "reset" to "reset the puppet by emit this event",
        "watchdog" to "feed the watchdog by emit this event"
) + CHAT_EVENT_DICT