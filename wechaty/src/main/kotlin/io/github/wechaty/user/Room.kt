package io.github.wechaty.user

import io.github.wechaty.InviteListener
import io.github.wechaty.JoinListener
import io.github.wechaty.LeaveListener
import io.github.wechaty.RoomInnerMessageListener
import io.github.wechaty.TopicListener
import io.github.wechaty.Accessory
import io.github.wechaty.Puppet
import io.github.wechaty.Wechaty
import io.github.wechaty.eventEmitter.Event
import io.github.wechaty.eventEmitter.Listener
import io.github.wechaty.filebox.FileBox
import io.github.wechaty.io.github.wechaty.schemas.EventEnum
import io.github.wechaty.schemas.RoomMemberQueryFilter
import io.github.wechaty.schemas.RoomPayload
import io.github.wechaty.type.Sayable
import io.github.wechaty.utils.QrcodeUtils
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

const val FOUR_PER_EM_SPACE = "\u2005"

class Room(wechaty: Wechaty, val id: String) : Accessory(wechaty), Sayable {

    private val puppet: Puppet = wechaty.getPuppet()
    private var payload: RoomPayload? = null

    fun sync(): Future<Void> {
        return ready(true)
    }

    fun isReady(): Boolean {
        return this.payload != null
    }

    override fun say(something: Any, contact: Contact): Future<Any> {

        var msgId: String? = null

        return CompletableFuture.supplyAsync {
            when (something) {

                is String -> {
                    msgId = puppet.messageSendText(id, something).get()

                }

            }

            if (StringUtils.isNotEmpty(msgId)) {
                val message = wechaty.messageManager.load(msgId!!)
                message.ready().get()
                return@supplyAsync message
            }

            return@supplyAsync null

        }
    }

    fun say(something: Any, vararg varList: Any): Future<Any> {

        var msgId: String?
        var text: String

        return CompletableFuture.supplyAsync {
            when (something) {
                //TODO(array)

                is String -> {
                    var mentionList = listOf<Any>()
                    if (varList.isNotEmpty()) {
                        val list = varList[0] as? List<*> ?: throw Exception("room say contact args not valid")
                        list.forEach {
                            if (it !is Contact) {
                                throw Exception("mentionList must be contact when not using String array function call.")
                            }
                        }
                        mentionList = list as List<Any>

                        val mentionAlias = mentionList.map { contact ->
                            val alias = alias(contact as Contact)
                            val concatText = if (StringUtils.isNotBlank(alias)) {
                                alias!!
                            } else {
                                contact.name()
                            }
                            return@map "@$concatText"
                        }
                        val mentionText = mentionAlias.joinToString(separator = FOUR_PER_EM_SPACE)
                        text = mentionText
                    } else {
                        text = something
                    }

                    msgId = wechaty.getPuppet().messageSendText(id, text, mentionList.map { c -> (c as Contact).id }).get()
                }
                is FileBox -> {
                    msgId = wechaty.getPuppet().messageSendFile(id, something).get()
                }

                is Contact -> {
                    msgId = wechaty.getPuppet().messageSendContact(id, something.id).get()
                }

                is UrlLink -> {
                    msgId = wechaty.getPuppet().messageSendUrl(id, something.payload).get()
                }

                is MiniProgram -> {
                    msgId = wechaty.getPuppet().messageSendMiniProgram(id, something.payload).get()
                }

                else -> {
                    throw Exception("unknown message")
                }

            }

            if (msgId != null) {
                val msg = wechaty.messageManager.load(msgId!!)
                return@supplyAsync msg
            }

            return@supplyAsync null
        }
    }

    fun ready(forceSync: Boolean = false): Future<Void> {
        return CompletableFuture.supplyAsync {
            if (!forceSync && isRead()) {
                return@supplyAsync null
            }

            if (forceSync) {
                puppet.roomPayloadDirty(id).get()
                puppet.roomMemberPayloadDirty(id).get()
            }

            this.payload = puppet.roomPayload(id).get()
            log.debug("get room payload is {} by id {}", payload, id)
            if (payload == null) {
                throw Exception("no payload")
            }

            val memberIdList = puppet.roomMemberList(id).get()

            memberIdList.map {
                wechaty.contactManager.load(it)
            }.forEach {
                it.ready()
            }
            return@supplyAsync null
        }
    }

    fun onInvite(listener: InviteListener): Room {
        return on(EventEnum.INVITE, listener)
    }

    fun onLeave(listener: LeaveListener): Room {
        return on(EventEnum.LEAVE, listener)
    }

    fun onInnerMessage(listener: RoomInnerMessageListener): Room {
        return on(EventEnum.MESSAGE, listener)
    }

    fun onJoin(listener: JoinListener): Room {
        return on(EventEnum.JOIN, listener);
    }

    fun onTopic(listener: TopicListener): Room {
        return on(EventEnum.TOPIC, listener)
    }

    private fun on(eventName: Event, listener: InviteListener): Room {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Contact, any[1] as RoomInvitation)
            }
        })
        return this
    }

    private fun on(eventName: Event, listener: LeaveListener): Room {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as List<Contact>, any[1] as Contact, any[2] as Date)
            }
        })
        return this
    }

    private fun on(eventName: Event, listenerRoomInner: RoomInnerMessageListener): Room {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listenerRoomInner.handler(any[0] as Message, any[1] as Date)
            }
        })
        return this
    }

    private fun on(eventName: Event, listener: JoinListener): Room {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as List<Contact>, any[1] as Contact, any[2] as Date)
            }
        })
        return this
    }

    private fun on(eventName: Event, listener: TopicListener): Room {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as String, any[1] as String, any[2] as Contact, any[3] as Date)
            }
        })
        return this
    }

    fun add(contact: Contact): Future<Void> {
        return CompletableFuture.supplyAsync {
            puppet.roomAdd(this.id, contact.id).get()
            return@supplyAsync null
        }
    }

    fun del(contact: Contact): Future<Void> {
        return CompletableFuture.supplyAsync {
            puppet.roomDel(this.id, contact.id).get();
            return@supplyAsync null
        }
    }

    fun quit(): Future<Void> {
        return CompletableFuture.supplyAsync {
            puppet.roomQuit(this.id).get()
            return@supplyAsync null
        }
    }

    fun getTopic(): Future<String> {

        if (!isReady()) {
            log.warn("Room topic() room not ready")
            throw Exception("not ready")
        }

        if (payload != null && payload!!.topic != null) {
            return CompletableFuture.supplyAsync {
                return@supplyAsync payload!!.topic
            }
        } else {
            val memberIdList = puppet.roomMemberList(id).get()
            val memberList = memberIdList.filter { it != puppet.selfId() }
                .map { wechaty.contactManager.load(it) }

            var defaultTopic = ""
            if (memberList.isNotEmpty()) {
                defaultTopic = memberList[0].name()
            }

            if (memberList.size >= 2) {
                for (index in 1..2) {
                    defaultTopic += ",${memberList[index].name()}"
                }
            }
            return CompletableFuture.supplyAsync {
                return@supplyAsync defaultTopic
            }
        }
    }

    fun setTopic(newTopic: String): Future<Void> {
        if (!isReady()) {
            log.warn("Room topic() room not ready")
            throw Exception("not ready")
        }

        return CompletableFuture.supplyAsync {
            try {
                val newTop = puppet.roomTopic(id, newTopic).get()
                return@supplyAsync puppet.roomTopic(id, newTopic).get()
            } catch (e: Exception) {
                log.warn("Room topic(newTopic=$newTopic) exception:$e")
                throw Exception(e)
            }
        }

    }

    @Deprecated("this function is deprecated! see getTopic,setTopic")
    fun topic(newTopic: String?): Future<Any> {
        if (!isReady()) {
            log.warn("Room topic() room not ready")
            throw Exception("not ready")
        }

        if (newTopic == null) {
            if (payload != null && payload!!.topic != null) {
                return CompletableFuture.supplyAsync {
                    return@supplyAsync payload!!.topic
                }
            } else {
                val memberIdList = puppet.roomMemberList(id).get()
                val memberList = memberIdList.filter { it != puppet.selfId() }
                    .map { wechaty.contactManager.load(it) }

                var defaultTopic = ""
                if (memberList.isNotEmpty()) {
                    defaultTopic = memberList[0].name()
                }

                if (memberList.size >= 2) {
                    for (index in 1..2) {
                        defaultTopic += ",${memberList[index].name()}"
                    }
                }
                return CompletableFuture.supplyAsync {
                    return@supplyAsync defaultTopic
                }
            }
        }

        return CompletableFuture.supplyAsync {
            try {
                return@supplyAsync puppet.roomTopic(id, newTopic).get()
            } catch (e: Exception) {
                log.warn("Room topic(newTopic=$newTopic) exception:$e")
                throw Exception(e)
            }
        }

    }

    fun announce(text: String?): Future<Any> {
        return CompletableFuture.supplyAsync {
            if (text == null) {
                return@supplyAsync puppet.getRoomAnnounce(id).get()
            } else {
                return@supplyAsync puppet.setRoomAnnounce(id, text)
            }
        }
    }

    fun qrCode(): Future<String> {
        return CompletableFuture.supplyAsync {
            val qrCodeValue = puppet.roomQRCode(id).get()
            return@supplyAsync QrcodeUtils.guardQrCodeValue(qrCodeValue)
        }
    }

    fun memberAll(query: RoomMemberQueryFilter?): List<Contact> {

        if (query == null) {
            return memberList()
        }

        val contactIdList = wechaty.getPuppet().roomMemberSearch(this.id, query).get()
        val contactList = contactIdList.map {
            wechaty.contactManager.load(id)
        }

        return contactList

    }

    fun memberList(): List<Contact> {

        val memberIdList = wechaty.getPuppet().roomMemberList(this.id).get()

        if (CollectionUtils.isEmpty(memberIdList)) {
            return listOf()
        }

        val contactList = memberIdList.map {
            wechaty.contactManager.load(id)
        }
        return contactList

    }

    fun alias(contact: Contact): String? {

        val roomMemberPayload = wechaty.getPuppet().roomMemberPayload(this.id, contact.id).get()

        return roomMemberPayload?.roomAlias


    }

    fun has(contact: Contact): Boolean {
        val memberIdList = puppet.roomMemberList(id).get()
        if (memberIdList.isEmpty()) {
            return false
        }
        return memberIdList.any { it == contact.id }
    }

    fun isRead(): Boolean {
        return payload != null
    }

    fun owner(): Contact? {
        val ownerId = payload?.ownerId

        return if (ownerId.isNullOrBlank()) {
            null
        } else {
            return wechaty.contactManager.load(ownerId)
        }
    }

    fun avatar(): FileBox {
        log.debug("avatar:{}", avatar())
        return puppet.roomAvatar(this.id).get()
    }

    companion object {
        private val log = LoggerFactory.getLogger(Room::class.java)
    }

}

val ROOM_EVENT_DICT = mapOf(
    "invite" to "tbw",
    "join" to "tbw",
    "leave" to "tbw",
    "message" to "message that received in this room",
    "topic" to "tbw"
)
