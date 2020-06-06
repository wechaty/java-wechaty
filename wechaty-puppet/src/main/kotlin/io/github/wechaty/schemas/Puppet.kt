package io.github.wechaty.schemas

import io.github.wechaty.io.github.wechaty.schemas.EventEnum

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
    EventEnum.FRIENDSHIP to "receive a friend request",
    EventEnum.LOGIN to "puppet had logined",
    EventEnum.LOGOUT to "puppet had logouted",
    EventEnum.MESSAGE to "received a new message",
    EventEnum.ROOM_INVITE to "received a room invitation",
    EventEnum.ROOM_JOIN to "be added to a room",
    EventEnum.ROOM_LEAVE to "leave or be removed from a room",
    EventEnum.ROOM_TOPIC to "room topic had been changed",
    EventEnum.SCAN to "a QR Code scan is required"
)

val PUPPET_EVENT_DICT = mapOf(
        EventEnum.DONG to "emit this event if you received a ding() call",
        EventEnum.ERROR to "emit an Error instance when there's any Error need to report to Wechaty",
        EventEnum.READY to "emit this event after the puppet is ready(you define it)",
        EventEnum.RESET to "reset the puppet by emit this event",
        EventEnum.WATCH_DOG to "feed the watchdog by emit this event"
) + CHAT_EVENT_DICT
