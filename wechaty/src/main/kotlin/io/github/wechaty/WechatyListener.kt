package io.github.wechaty

import io.github.wechaty.schemas.ScanStatus
import io.github.wechaty.user.*
import java.util.*

@FunctionalInterface
interface DongListener {
    fun handler(data: String?)
}

@FunctionalInterface
interface ErrorListener {
    fun handler(error: String)
}

@FunctionalInterface
interface FriendshipListener {
    fun handler(friendship: Friendship)
}

@FunctionalInterface
interface HeartbeatListener{
    fun handler(data:Any)

}

@FunctionalInterface
interface ScanListener {
    fun handler(qrcode: String?, statusScanStatus: ScanStatus, data: String?)

}

@FunctionalInterface
interface LoginListener {
    fun handler(self: ContactSelf)
}

@FunctionalInterface
interface LogoutListener {
    fun handler(contactId: String, reason: String?)
}

@FunctionalInterface
interface ResetListerner {
    fun handler(reason: String)
}

@FunctionalInterface
interface RoomJoinListener {
    fun handler(room: Room, inviteeList: List<Contact>, inviter: Contact, date: Date)
}

@FunctionalInterface
interface RoomLeaveListener {
    fun handler(room: Room, leaverList: List<Contact>, remover: Contact, date: Date)
}

@FunctionalInterface
interface RoomTopicListener {
    fun handler(room: Room, newTopic: String, oldTopic: String, changer: Contact, date: Date)
}

@FunctionalInterface
interface RoomInviteListener {
    fun handler(roomInvitation : RoomInvitation)
}

@FunctionalInterface
interface ReadyListener {
    fun handler()
}

@FunctionalInterface
interface MessageListener {
    fun handler(message: Message)
}

